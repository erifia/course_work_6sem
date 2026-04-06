package com.example.coursework6sem.web.dto.evaluation;

import java.math.BigDecimal;
import java.time.Instant;

public record EvaluationResponse(
        Long id,
        Long estateId,
        String address,
        Long appraiserId,
        String appraiserName,
        BigDecimal estimatedValue,
        String evaluationMethod,
        String notes,
        Instant createdAt
) {
}

