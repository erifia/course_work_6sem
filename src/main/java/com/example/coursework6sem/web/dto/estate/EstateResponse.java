package com.example.coursework6sem.web.dto.estate;

import java.math.BigDecimal;
import java.time.Instant;

public record EstateResponse(
        Long id,
        String address,
        Long districtId,
        String districtName,
        Integer rooms,
        BigDecimal area,
        String condition,
        String propertyType,
        BigDecimal price,
        Integer floor,
        Integer totalFloors,
        String description,
        String imagePath,
        Instant createdAt
) {
}

