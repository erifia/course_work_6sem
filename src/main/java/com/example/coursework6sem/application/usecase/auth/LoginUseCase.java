package com.example.coursework6sem.application.usecase.auth;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RefreshTokenEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RoleEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RefreshTokenRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RoleRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.JwtProperties;
import com.example.coursework6sem.security.JwtService;
import com.example.coursework6sem.security.TokenHasher;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LoginUseCase {

    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHasher tokenHasher;

    public LoginUseCase(UserAccountRepository users,
                         RefreshTokenRepository refreshTokens,
                         PasswordEncoder passwordEncoder,
                         JwtService jwtService,
                         JwtProperties jwtProperties,
                         TokenHasher tokenHasher) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenHasher = tokenHasher;
    }

    @Transactional
    public AuthResponses.AuthResponse execute(AuthRequests.LoginRequest request) {
        UserAccountEntity user = users.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Неверные логин/пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверные логин/пароль");
        }

        Instant now = Instant.now();

        // Ротация refresh-токена: создаём новый и помечаем старые как revoked (простая стратегия).
        // Для этого на данном этапе очистим только по hash после валидации из refresh endpoint.
        String refreshToken = java.util.UUID.randomUUID().toString();
        String refreshHash = tokenHasher.sha256Hex(refreshToken);

        refreshTokens.save(new RefreshTokenEntity(
                user,
                refreshHash,
                now.plus(jwtProperties.getRefreshTokenTtl()),
                null,
                now
        ));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRoleName().name());

        return new AuthResponses.AuthResponse(
                new AuthResponses.TokenPair(accessToken, refreshToken),
                new AuthResponses.UserView(user.getId(), user.getUsername(), user.getEmail(), user.getRoleName())
        );
    }
}

