package com.example.coursework6sem.application.service.evaluation;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.application.valuation.AutoValuationStrategy;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
public class ManualEvaluationService {

    private final DistrictRepository districts;
    private final UserAccountRepository users;
    private final EstateRepository estates;
    private final EvaluationRepository evaluations;
    private final SecurityUtils securityUtils;
    private final AutoValuationStrategy autoValuationStrategy;

    public ManualEvaluationService(
            DistrictRepository districts,
            UserAccountRepository users,
            EstateRepository estates,
            EvaluationRepository evaluations,
            SecurityUtils securityUtils,
            AutoValuationStrategy autoValuationStrategy
    ) {
        this.districts = districts;
        this.users = users;
        this.estates = estates;
        this.evaluations = evaluations;
        this.securityUtils = securityUtils;
        this.autoValuationStrategy = autoValuationStrategy;
    }

    @Transactional
    public Map<String, Object> evaluateAndSave(EvaluationRequests.ManualCreateRequest request) {
        Long userId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Требуется авторизация"));

        UserAccountEntity me = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        DistrictEntity district = districts.findById(request.districtId())
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        ConditionCode conditionCode = ConditionCodeMapper.fromRussianLabel(request.condition())
                .orElseThrow(() -> new IllegalArgumentException("Некорректное состояние"));

        Instant now = Instant.now();
        EstateEntity draft = new EstateEntity(
                district,
                me,
                request.address(),
                "MANUAL_DRAFT",
                request.rooms(),
                request.area(),
                BigDecimal.ZERO,
                request.floor(),
                request.totalFloors(),
                conditionCode,
                request.description(),
                null,
                now,
                now
        );

        EstateValuationResponse valuation = autoValuationStrategy.calculate(draft, district);

        draft.setPrice(valuation.estimatedValue());
        EstateEntity savedEstate = estates.save(draft);

        EvaluationEntity savedEval = evaluations.save(new EvaluationEntity(
                savedEstate,
                me,
                valuation.estimatedValue(),
                "Автоматический расчет (ручной ввод)",
                "Оценка сохранена из формы пользователя",
                now,
                now
        ));

        EvaluationResponse evaluationResponse = new EvaluationResponse(
                savedEval.getId(),
                savedEstate.getId(),
                savedEstate.getAddress(),
                me.getId(),
                me.getUsername(),
                savedEval.getEstimatedValue(),
                savedEval.getEvaluationMethod(),
                savedEval.getNotes(),
                savedEval.getCreatedAt()
        );

        return Map.of(
                "valuation", valuation,
                "evaluation", evaluationResponse
        );
    }
}

