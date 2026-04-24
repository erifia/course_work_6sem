package com.example.coursework6sem.application.usecase.auth;

import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import com.example.coursework6sem.web.dto.auth.ProfileRequests;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProfileUseCase {

    private final UserAccountRepository users;
    private final SecurityUtils securityUtils;

    public UpdateProfileUseCase(UserAccountRepository users, SecurityUtils securityUtils) {
        this.users = users;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public AuthResponses.ProfileResponse execute(ProfileRequests.UpdateProfileRequest request) {
        Long userId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Не авторизован"));

        var user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        String nextUsername = request.username().trim();
        String nextEmail = request.email().trim();

        users.findByUsername(nextUsername)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw new IllegalArgumentException("Логин уже занят"); });

        users.findByEmail(nextEmail)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw new IllegalArgumentException("Email уже занят"); });

        user.setUsername(nextUsername);
        user.setEmail(nextEmail);

        return new AuthResponses.ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleName(),
                user.getCreatedAt()
        );
    }
}

