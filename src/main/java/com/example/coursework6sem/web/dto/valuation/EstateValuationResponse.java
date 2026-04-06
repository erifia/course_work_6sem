package com.example.coursework6sem.web.dto.valuation;

import java.math.BigDecimal;

public record EstateValuationResponse(
        BigDecimal currentDistrictAvgPrice,
        BigDecimal baseValue,
        double roomMultiplier,
        double demandMultiplier,
        double conditionMultiplier,
        double floorMultiplier,
        BigDecimal estimatedValue
) {
}

