package com.example.coursework6sem.web.dto.recommendation;

import java.math.BigDecimal;
import java.time.Instant;

public record RecommendationResponse(
        Long recommendationId,
        Long estateId,
        String address,
        Long districtId,
        String districtName,
        Integer rooms,
        BigDecimal area,
        Integer floor,
        Integer totalFloors,
        String condition,
        String propertyType,
        BigDecimal price,
        String imagePath,
        BigDecimal score,
        Integer scorePercent,
        Instant createdAt
) {
}

