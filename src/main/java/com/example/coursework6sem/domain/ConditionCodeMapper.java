package com.example.coursework6sem.domain;

import java.util.Map;
import java.util.Optional;

public final class ConditionCodeMapper {
    private static final Map<String, ConditionCode> BY_RUS_LABEL = Map.of(
            "требует ремонта", ConditionCode.REQUIRES_REPAIR,
            "среднее", ConditionCode.AVERAGE,
            "хорошее", ConditionCode.GOOD,
            "отличное", ConditionCode.EXCELLENT
    );

    private static final Map<ConditionCode, String> TO_RUS_LABEL = Map.of(
            ConditionCode.REQUIRES_REPAIR, "требует ремонта",
            ConditionCode.AVERAGE, "среднее",
            ConditionCode.GOOD, "хорошее",
            ConditionCode.EXCELLENT, "отличное"
    );

    private ConditionCodeMapper() {
    }

    public static Optional<ConditionCode> fromRussianLabel(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_RUS_LABEL.get(value.trim().toLowerCase()));
    }

    public static String toRussianLabel(ConditionCode code) {
        return TO_RUS_LABEL.getOrDefault(code, code.name());
    }
}

