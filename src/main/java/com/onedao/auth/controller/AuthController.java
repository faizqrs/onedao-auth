package com.onedao.auth.controller;

import com.onedao.auth.dto.*;
import com.onedao.auth.entity.User;
import com.onedao.auth.repository.UserRepository;
import com.onedao.auth.security.JwtUtil;
import com.onedao.auth.security.TokenBlacklist;
import com.onedao.auth.service.GeoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist blacklist;
    private final GeoService geo;
    private final boolean otpRequired;
    private final long otpExpiryMin;
    private final SecureRandom random = new SecureRandom();

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil, TokenBlacklist blacklist,
                          GeoService geo, @Value("${app.otp.required}") boolean otpRequired,
                          @Value("${app.otp.expiry-minutes}") long otpExpiryMin) {
        this.users = users; this.encoder = encoder; this.jwtUtil = jwtUtil; this.blacklist = blacklist;
        this.geo = geo; this.otpRequired = otpRequired; this.otpExpiryMin = otpExpiryMin;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest req, HttpServletRequest http) {
        // 1. country check
        String ip = clientIp(http);
        String country = geo.resolveCountry(ip);
        if (geo.isBlocked(country)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sign-up is not allowed from your country (" + country + ")");
        }
        // 2. uniqueness
        if (users.existsByUsername(req.username())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        if (users.existsByEmail(req.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));   // BCrypt
        u.setUserType(req.userType());
        u.setCountry(country);
        String otp = String.format("%06d", random.nextInt(1_000_000));
        u.setOtp(otp);
        u.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMin));
        u.setVerified(false);
        users.save(u);

        log.info("OTP for {} = {} (simulated email/SMS)", u.getUsername(), otp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered. OTP sent (check server console)."));
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@Valid @RequestBody OtpRequest req) {
        User u = users.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // already verified -> clear message instead of "Invalid or expired OTP"
        if (u.isVerified()) {
            return Map.of("message", "User already verified");
        }

        if (u.getOtp() == null || !u.getOtp().equals(req.otp())
                || u.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        u.setVerified(true);
        u.setOtp(null);
        u.setOtpExpiry(null);
        users.save(u);
        return Map.of("message", "OTP verified");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User u = users.findByUsername(req.username()).orElse(null);
        boolean ok = u != null && encoder.matches(req.password(), u.getPassword()) && (!otpRequired || u.isVerified());
        if (!ok) {
            // invalid login -> redirect to Error endpoint (303 so clients follow with GET)
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("/api/error")).build();
        }
        String role = u.getUserType().equals("Administrator") ? "ADMIN" : "USER";
        String token = jwtUtil.generateToken(u.getUsername(), role);
        return ResponseEntity.ok(Map.of("token", token, "type", "Bearer",
                "message", "Login successful. Use the token to call /api/dashboard"));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(Authentication auth, HttpServletRequest http) {
        blacklist.revoke((String) http.getAttribute("jti"));
        return Map.of("message", auth.getName() + " Logged Out");
    }

    private String clientIp(HttpServletRequest r) {
        // Use the real socket address only; X-Forwarded-For is client-controlled and spoofable.
        return r.getRemoteAddr();
    }
}
