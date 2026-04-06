package com.example.coursework6sem.web.dto.evaluation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class EvaluationRequests {
    private EvaluationRequests() {
    }

    public record CreateRequest(
            @NotNull Long estateId,
            BigDecimal estimatedValue,
            String evaluationMethod,
            String notes
    ) {
    }

    public record UpdateRequest(
            @NotNull BigDecimal estimatedValue,
            String evaluationMethod,
            String notes
    ) {
    }

    public record ManualCreateRequest(
            @NotNull Long districtId,
            @NotBlank String address,
            @NotNull Integer rooms,
            @NotNull BigDecimal area,
            @NotNull Integer floor,
            @NotNull Integer totalFloors,
            @NotBlank String condition,
            String description
    ) {
    }
}

