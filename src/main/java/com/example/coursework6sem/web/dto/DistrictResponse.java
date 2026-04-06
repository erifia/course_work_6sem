package com.example.coursework6sem.web.dto;

import java.math.BigDecimal;

public record DistrictResponse(
        Long id,
        String districtName,
        BigDecimal avgPrice,
        Integer demandLevel
) {
}

