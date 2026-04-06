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
import com.example.coursework6sem.security.PasswordConfig;
import com.example.coursework6sem.security.TokenHasher;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegisterUseCase {

    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHasher tokenHasher;

    public RegisterUseCase(UserAccountRepository users,
                            RoleRepository roles,
                            RefreshTokenRepository refreshTokens,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService,
                            JwtProperties jwtProperties,
                            TokenHasher tokenHasher) {
        this.users = users;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenHasher = tokenHasher;
    }

    public AuthResponses.AuthResponse execute(AuthRequests.RegisterRequest request) {
        if (users.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        }
        if (users.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        RoleName roleName = request.role() == null ? RoleName.CLIENT : request.role();
        if (roleName == RoleName.ADMIN) {
            throw new IllegalArgumentException("Самостоятельная регистрация администратора запрещена");
        }
        RoleEntity role = roles.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));

        String passwordHash = passwordEncoder.encode(request.password());
        Instant now = Instant.now();

        UserAccountEntity user = users.save(new UserAccountEntity(
                request.username(),
                passwordHash,
                request.email(),
                role,
                now
        ));

        String refreshToken = java.util.UUID.randomUUID().toString();
        String refreshHash = tokenHasher.sha256Hex(refreshToken);

        RefreshTokenEntity refresh = new RefreshTokenEntity(
                user,
                refreshHash,
                now.plus(jwtProperties.getRefreshTokenTtl()),
                null,
                now
        );
        refreshTokens.save(refresh);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRoleName().name());

        return new AuthResponses.AuthResponse(
                new AuthResponses.TokenPair(accessToken, refreshToken),
                new AuthResponses.UserView(user.getId(), user.getUsername(), user.getEmail(), user.getRoleName())
        );
    }
}

