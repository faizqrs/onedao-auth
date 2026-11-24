package com.onedao.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @GetMapping("/dashboard")
    public Map<String, String> dashboard(Authentication auth) {
        return Map.of("message", "Welcome " + auth.getName());
    }

    /** ADMIN only (enforced in SecurityConfig). */
    @GetMapping("/settings")
    public Map<String, String> settings(Authentication auth) {
        return Map.of("message", "Settings page - Administrator access granted to " + auth.getName());
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> error() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Login Failed"));
    }
}
