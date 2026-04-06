package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteEstateUseCase {

    private final EstateRepository estates;
    private final SecurityUtils securityUtils;

    public DeleteEstateUseCase(EstateRepository estates, SecurityUtils securityUtils) {
        this.estates = estates;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public void execute(long estateId) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        EstateEntity estate = estates.findById(estateId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));

        boolean isAdmin = role.equalsIgnoreCase(RoleName.ADMIN.name());
        boolean isOwner = estate.getCreatedBy() != null && estate.getCreatedBy().getId().equals(userId);
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        estates.delete(estate);
    }
}

