package com.example.coursework6sem.web.dto.recommendation;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UserPreferenceRequests(
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

