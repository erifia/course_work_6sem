package com.example.coursework6sem.web.dto.recommendation;

import com.example.coursework6sem.domain.ConditionCode;

import java.math.BigDecimal;
import java.time.Instant;

public record UserPreferenceResponse(
        Long preferenceId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal minArea,
        BigDecimal maxArea,
        Integer minRooms,
        Integer maxRooms,
        Integer minFloor,
        Integer maxFloor,
        String condition
) {
}

