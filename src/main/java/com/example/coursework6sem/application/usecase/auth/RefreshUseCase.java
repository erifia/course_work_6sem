package com.example.coursework6sem.application.usecase.auth;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RefreshTokenEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RefreshTokenRepository;
import com.example.coursework6sem.security.JwtProperties;
import com.example.coursework6sem.security.JwtService;
import com.example.coursework6sem.security.TokenHasher;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshUseCase {

    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHasher tokenHasher;

    public RefreshUseCase(RefreshTokenRepository refreshTokens, JwtService jwtService, JwtProperties jwtProperties, TokenHasher tokenHasher) {
        this.refreshTokens = refreshTokens;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenHasher = tokenHasher;
    }

    public AuthResponses.AuthResponse execute(AuthRequests.RefreshRequest request) {
        String refreshToken = request.refreshToken();
        String refreshHash = tokenHasher.sha256Hex(refreshToken);

        RefreshTokenEntity token = refreshTokens.findByTokenHash(refreshHash)
                .orElseThrow(() -> new IllegalArgumentException("Недействительный refresh token"));

        if (token.getRevokedAt() != null) {
            throw new IllegalArgumentException("Refresh token уже отозван");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token просрочен");
        }

        UserAccountEntity user = token.getUser();

        // revoke old token and create new one (rotation)
        token.revoke(Instant.now());
        refreshTokens.save(token);

        String newRefreshToken = java.util.UUID.randomUUID().toString();
        String newHash = tokenHasher.sha256Hex(newRefreshToken);

        refreshTokens.save(new RefreshTokenEntity(
                user,
                newHash,
                Instant.now().plus(jwtProperties.getRefreshTokenTtl()),
                null,
                Instant.now()
        ));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRoleName().name());

        return new AuthResponses.AuthResponse(
                new AuthResponses.TokenPair(accessToken, newRefreshToken),
                new AuthResponses.UserView(user.getId(), user.getUsername(), user.getEmail(), user.getRoleName())
        );
    }
}

