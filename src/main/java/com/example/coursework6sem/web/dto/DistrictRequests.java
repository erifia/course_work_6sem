package com.example.coursework6sem.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DistrictRequests(
        @NotBlank @Size(max = 100) String districtName,
        BigDecimal avgPrice,
        @Min(1) @Max(10) Integer demandLevel
) {
}

