package com.example.coursework6sem.web.dto.external;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrencyResponse(
        String base,
        String target,
        BigDecimal rate,
        Instant fetchedAt
) {
}

