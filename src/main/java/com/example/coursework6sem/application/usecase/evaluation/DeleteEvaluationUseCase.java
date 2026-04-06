package com.example.coursework6sem.application.usecase.evaluation;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteEvaluationUseCase {

    private final EvaluationRepository evaluations;
    private final SecurityUtils securityUtils;

    public DeleteEvaluationUseCase(EvaluationRepository evaluations, SecurityUtils securityUtils) {
        this.evaluations = evaluations;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public void execute(long evaluationId) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        EvaluationEntity evaluation = evaluations.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        boolean isAdmin = role.equalsIgnoreCase(RoleName.ADMIN.name());
        boolean isAppraiser = evaluation.getAppraiser().getId().equals(userId);
        if (!isAdmin && !isAppraiser) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        evaluations.delete(evaluation);
    }
}

