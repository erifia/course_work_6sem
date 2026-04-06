package com.example.coursework6sem.web.dto.auth;

import com.example.coursework6sem.domain.RoleName;

import java.time.Instant;

public final class AuthResponses {
    private AuthResponses() {
    }

    public record UserView(
            Long userAccountId,
            String username,
            String email,
            RoleName role
    ) {
    }

    public record TokenPair(
            String accessToken,
            String refreshToken
    ) {
    }

    public record AuthResponse(
            TokenPair tokens,
            UserView user
    ) {
    }

    public record ProfileResponse(
            Long userAccountId,
            String username,
            String email,
            RoleName role,
            Instant createdAt
    ) {
    }
}

