package com.example.coursework6sem.application.service.evaluation;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ManualEvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.ManualEvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.application.valuation.AutoValuationStrategy;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.ManualEvaluationResponse;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ManualEvaluationService {

    private final DistrictRepository districts;
    private final UserAccountRepository users;
    private final ManualEvaluationRepository manualEvaluations;
    private final SecurityUtils securityUtils;
    private final AutoValuationStrategy autoValuationStrategy;

    public ManualEvaluationService(
            DistrictRepository districts,
            UserAccountRepository users,
            ManualEvaluationRepository manualEvaluations,
            SecurityUtils securityUtils,
            AutoValuationStrategy autoValuationStrategy
    ) {
        this.districts = districts;
        this.users = users;
        this.manualEvaluations = manualEvaluations;
        this.securityUtils = securityUtils;
        this.autoValuationStrategy = autoValuationStrategy;
    }

    @Transactional
    public ManualEvaluationResponse evaluateAndSave(EvaluationRequests.ManualCreateRequest request) {
        Long userId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Требуется авторизация"));

        UserAccountEntity me = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        DistrictEntity district = districts.findById(request.districtId())
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        ConditionCode conditionCode = ConditionCodeMapper.fromRussianLabel(request.condition())
                .orElseThrow(() -> new IllegalArgumentException("Некорректное состояние"));

        Instant now = Instant.now();
        // Объект для продажи НЕ создается. Используем только временный расчет.
        var draftEstate = new com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity(
            district, me, request.address(), "MANUAL_INPUT", request.rooms(), request.area(), BigDecimal.ZERO,
            request.floor(), request.totalFloors(), conditionCode, request.description(), null, now, now
        );

        EstateValuationResponse valuation = autoValuationStrategy.calculate(draftEstate, district);

        ManualEvaluationEntity saved = manualEvaluations.save(new ManualEvaluationEntity(
                district,
                me,
                request.address(),
                request.rooms(),
                request.area(),
                request.floor(),
                request.totalFloors(),
                conditionCode,
                request.description(),
                valuation.estimatedValue(),
                now,
                now
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ManualEvaluationResponse> list(Pageable pageable) {
        Long userId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElse(RoleName.CLIENT.name());

        if (RoleName.CLIENT.name().equalsIgnoreCase(role)) {
            return manualEvaluations.findAll((root, query, cb) ->
                    cb.equal(root.get("appraiser").get("id"), userId), pageable).map(this::toResponse);
        }
        return manualEvaluations.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ManualEvaluationResponse getById(Long id) {
        Long userId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElse(RoleName.CLIENT.name());
        ManualEvaluationEntity evaluation = manualEvaluations.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));
        if (RoleName.CLIENT.name().equalsIgnoreCase(role) && !evaluation.getAppraiser().getId().equals(userId)) {
            throw new IllegalArgumentException("Недостаточно прав для просмотра этой оценки");
        }
        return toResponse(evaluation);
    }

    private ManualEvaluationResponse toResponse(ManualEvaluationEntity e) {
        return new ManualEvaluationResponse(
                e.getId(),
                e.getDistrict().getId(),
                e.getDistrict().getName(),
                e.getAddress(),
                e.getRooms(),
                e.getArea(),
                e.getFloor(),
                e.getTotalFloors(),
                ConditionCodeMapper.toRussianLabel(e.getConditionCode()),
                e.getDescription(),
                e.getAppraiser().getId(),
                e.getAppraiser().getUsername(),
                e.getEstimatedValue(),
                e.getCreatedAt()
        );
    }
}

