package com.onedao.auth.dto;

import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank @Size(min = 8, max = 50, message = "Username must be 8 to 50 characters")
        String username,

        @NotBlank
        @Pattern(regexp = "^(?=.*_)[^@\\s]+@[^@\\s]+$", message = "Email must contain @ and _")
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,12}$",
                message = "Password must be 8-12 chars with 1 capital, 1 small, 1 number and 1 special character")
        String password,

        @NotBlank
        @Pattern(regexp = "Standard User|Administrator", message = "User Type must be 'Standard User' or 'Administrator'")
        String userType) {}
