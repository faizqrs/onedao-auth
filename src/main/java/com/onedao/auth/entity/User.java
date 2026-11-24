package com.onedao.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;          // BCrypt hash only
    @Column(nullable = false)
    private String userType;          // "Standard User" | "Administrator"
    private String country;
    private boolean verified;
    private String otp;
    private LocalDateTime otpExpiry;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public String getUserType() { return userType; }
    public void setUserType(String v) { this.userType = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean v) { this.verified = v; }
    public String getOtp() { return otp; }
    public void setOtp(String v) { this.otp = v; }
    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime v) { this.otpExpiry = v; }
}
