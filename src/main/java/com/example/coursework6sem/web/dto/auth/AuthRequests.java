package com.example.coursework6sem.web.dto.auth;

import com.example.coursework6sem.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthRequests {
    private AuthRequests() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 6, max = 72) String password,
            @NotBlank @Email @Size(max = 100) String email,
            RoleName role
    ) {
    }

    public record LoginRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {
    }
}

