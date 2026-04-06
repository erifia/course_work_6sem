package com.example.coursework6sem.web.dto.valuation;

import java.math.BigDecimal;

public record EstateComparisonResponse(
        EstateSummary estate1,
        EstateSummary estate2,
        Differences differences
) {
    public record EstateSummary(
            Long id,
            String address,
            BigDecimal price,
            BigDecimal area,
            Integer rooms,
            BigDecimal pricePerSquare
    ) {
    }

    public record Differences(
            BigDecimal priceDiff,
            BigDecimal areaDiff,
            Integer roomsDiff,
            BigDecimal pricePerSquareDiff
    ) {
    }
}

