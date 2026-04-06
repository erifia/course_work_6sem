package com.example.coursework6sem.application.usecase.recommendation;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserPreferenceEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserPreferenceRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceRequests;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UpdateUserPreferenceUseCase {

    private final UserPreferenceRepository preferences;
    private final UserAccountRepository users;
    private final SecurityUtils securityUtils;

    public UpdateUserPreferenceUseCase(UserPreferenceRepository preferences, UserAccountRepository users, SecurityUtils securityUtils) {
        this.preferences = preferences;
        this.users = users;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public UserPreferenceResponse execute(UserPreferenceRequests request) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));

        UserAccountEntity user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        ConditionCode conditionCode = ConditionCodeMapper.fromRussianLabel(request.condition()).orElse(null);

        UserPreferenceEntity pref = preferences.findByUser_Id(userId).orElseGet(() ->
                preferences.save(new UserPreferenceEntity(user,
                        request.minPrice(),
                        request.maxPrice(),
                        request.minArea(),
                        request.maxArea(),
                        request.minRooms(),
                        request.maxRooms(),
                        request.minFloor(),
                        request.maxFloor(),
                        conditionCode,
                        Instant.now()
                ))
        );

        // update existing
        pref.setMinPrice(request.minPrice());
        pref.setMaxPrice(request.maxPrice());
        pref.setMinArea(request.minArea());
        pref.setMaxArea(request.maxArea());
        pref.setMinRooms(request.minRooms());
        pref.setMaxRooms(request.maxRooms());
        pref.setMinFloor(request.minFloor());
        pref.setMaxFloor(request.maxFloor());
        pref.setConditionCode(conditionCode);

        UserPreferenceEntity saved = preferences.save(pref);

        return new UserPreferenceResponse(
                saved.getId(),
                saved.getMinPrice(),
                saved.getMaxPrice(),
                saved.getMinArea(),
                saved.getMaxArea(),
                saved.getMinRooms(),
                saved.getMaxRooms(),
                saved.getMinFloor(),
                saved.getMaxFloor(),
                saved.getConditionCode() == null ? null : saved.getConditionCode().name()
        );
    }
}

