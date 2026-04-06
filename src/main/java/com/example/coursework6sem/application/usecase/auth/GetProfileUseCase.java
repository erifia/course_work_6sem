package com.example.coursework6sem.application.usecase.auth;

import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProfileUseCase {

    private final UserAccountRepository users;
    private final SecurityUtils securityUtils;

    public GetProfileUseCase(UserAccountRepository users, SecurityUtils securityUtils) {
        this.users = users;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public AuthResponses.ProfileResponse execute() {
        Long userId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Не авторизован"));

        var user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return new AuthResponses.ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleName(),
                user.getCreatedAt()
        );
    }
}

