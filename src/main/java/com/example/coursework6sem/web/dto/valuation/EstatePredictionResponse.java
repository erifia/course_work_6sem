package com.example.coursework6sem.web.dto.valuation;

import java.math.BigDecimal;

public record EstatePredictionResponse(
        BigDecimal currentValue,
        BigDecimal predictedValue,
        double growthRate,
        int months
) {
}

