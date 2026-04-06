package com.example.coursework6sem.web.dto.estate;

import jakarta.validation.constraints.*;

public final class EstateRequests {
    private EstateRequests() {
    }

    public record CreateRequest(
            @NotBlank @Size(max = 255) String address,
            @Min(1) Integer rooms,
            @DecimalMin("0.01") java.math.BigDecimal area,
            @NotNull Long districtId,
            @DecimalMin("0") java.math.BigDecimal price,
            @Min(1) Integer floor,
            @Min(1) Integer totalFloors,
            @NotBlank String condition,
            String description,
            String propertyType,
            String imagePath
    ) {
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String address,
            @Min(1) Integer rooms,
            @DecimalMin("0.01") java.math.BigDecimal area,
            @NotNull Long districtId,
            @DecimalMin("0") java.math.BigDecimal price,
            @Min(1) Integer floor,
            @Min(1) Integer totalFloors,
            @NotBlank String condition,
            String description,
            String propertyType,
            String imagePath
    ) {
    }
}

