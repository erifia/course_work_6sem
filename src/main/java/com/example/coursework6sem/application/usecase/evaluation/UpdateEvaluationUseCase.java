package com.example.coursework6sem.application.usecase.evaluation;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UpdateEvaluationUseCase {

    private final EvaluationRepository evaluations;
    private final SecurityUtils securityUtils;

    public UpdateEvaluationUseCase(EvaluationRepository evaluations, SecurityUtils securityUtils) {
        this.evaluations = evaluations;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public EvaluationResponse execute(long evaluationId, EvaluationRequests.UpdateRequest request) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        EvaluationEntity evaluation = evaluations.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        boolean isAdmin = role.equalsIgnoreCase(RoleName.ADMIN.name());
        boolean isAppraiser = evaluation.getAppraiser().getId().equals(userId);
        if (!isAdmin && !isAppraiser) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        evaluation.setEstimatedValue(request.estimatedValue());
        evaluation.setEvaluationMethod(request.evaluationMethod());
        evaluation.setNotes(request.notes());
        evaluation.setEvaluationMethod(request.evaluationMethod());
        // updated_at поддерживается триггером в БД, но проставим для консистентности
        // (не критично, если триггер уже обновляет)
        // evaluation.setUpdatedAt(Instant.now()); // нет сеттера

        EvaluationEntity saved = evaluations.save(evaluation);

        return new EvaluationResponse(
                saved.getId(),
                saved.getEstate().getId(),
                saved.getEstate().getAddress(),
                saved.getAppraiser().getId(),
                saved.getAppraiser().getUsername(),
                saved.getEstimatedValue(),
                saved.getEvaluationMethod(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }
}

