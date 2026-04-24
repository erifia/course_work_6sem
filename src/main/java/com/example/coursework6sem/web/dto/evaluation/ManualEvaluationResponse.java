package com.example.coursework6sem.web.dto.evaluation;

import java.math.BigDecimal;
import java.time.Instant;

public record ManualEvaluationResponse(
        Long id,
        Long districtId,
        String districtName,
        String address,
        Integer rooms,
        BigDecimal area,
        Integer floor,
        Integer totalFloors,
        String condition,
        String description,
        Long appraiserId,
        String appraiserName,
        BigDecimal estimatedValue,
        Instant createdAt
) {
}
