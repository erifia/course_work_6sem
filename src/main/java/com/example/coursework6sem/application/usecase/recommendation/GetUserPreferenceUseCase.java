package com.example.coursework6sem.application.usecase.recommendation;

import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserPreferenceRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserPreferenceUseCase {

    private final UserPreferenceRepository preferences;
    private final SecurityUtils securityUtils;

    public GetUserPreferenceUseCase(UserPreferenceRepository preferences, SecurityUtils securityUtils) {
        this.preferences = preferences;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse execute() {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        var pref = preferences.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Предпочтения не установлены"));

        String condition = pref.getConditionCode() == null ? null : ConditionCodeMapper.toRussianLabel(pref.getConditionCode());

        return new UserPreferenceResponse(
                pref.getId(),
                pref.getMinPrice(),
                pref.getMaxPrice(),
                pref.getMinArea(),
                pref.getMaxArea(),
                pref.getMinRooms(),
                pref.getMaxRooms(),
                pref.getMinFloor(),
                pref.getMaxFloor(),
                condition
        );
    }
}

