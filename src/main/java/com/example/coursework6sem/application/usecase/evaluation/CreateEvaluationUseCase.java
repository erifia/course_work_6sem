package com.example.coursework6sem.application.usecase.evaluation;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.application.usecase.estate.CalculateEstateValueUseCase;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class CreateEvaluationUseCase {

    private final EvaluationRepository evaluations;
    private final EstateRepository estates;
    private final UserAccountRepository users;
    private final SecurityUtils securityUtils;
    private final CalculateEstateValueUseCase calculateEstateValueUseCase;

    public CreateEvaluationUseCase(EvaluationRepository evaluations,
                                    EstateRepository estates,
                                    UserAccountRepository users,
                                    SecurityUtils securityUtils,
                                    CalculateEstateValueUseCase calculateEstateValueUseCase) {
        this.evaluations = evaluations;
        this.estates = estates;
        this.users = users;
        this.securityUtils = securityUtils;
        this.calculateEstateValueUseCase = calculateEstateValueUseCase;
    }

    @Transactional
    public EvaluationResponse execute(EvaluationRequests.CreateRequest request) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElseThrow(() -> new IllegalStateException("Роль не найдена"));
        boolean allowed = role.equalsIgnoreCase(RoleName.ADMIN.name()) || role.equalsIgnoreCase(RoleName.APPRAISER.name());
        if (!allowed) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        EstateEntity estate = estates.findById(request.estateId())
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));

        UserAccountEntity appraiser = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        BigDecimal estimatedValue;
        String method = request.evaluationMethod();
        if (request.estimatedValue() != null) {
            estimatedValue = request.estimatedValue();
            if (method == null || method.isBlank()) {
                method = "Ручной ввод";
            }
        } else {
            var calc = calculateEstateValueUseCase.execute(estate.getId(), "AUTO");
            estimatedValue = calc.estimatedValue();
            if (method == null || method.isBlank()) {
                method = "Автоматический расчет";
            }
        }

        Instant now = Instant.now();

        EvaluationEntity created = evaluations.save(new EvaluationEntity(
                estate,
                appraiser,
                estimatedValue,
                method,
                request.notes(),
                now,
                now
        ));

        // Маппинг ответа
        return new EvaluationResponse(
                created.getId(),
                created.getEstate().getId(),
                created.getEstate().getAddress(),
                created.getAppraiser().getId(),
                created.getAppraiser().getUsername(),
                created.getEstimatedValue(),
                created.getEvaluationMethod(),
                created.getNotes(),
                created.getCreatedAt()
        );
    }
}

