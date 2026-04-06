package com.example.coursework6sem.application.valuation;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ValuationStrategyFactory {

    private final Map<String, ValuationStrategy> strategies;

    public ValuationStrategyFactory(AutoValuationStrategy auto) {
        this.strategies = Map.of(
                "AUTO", auto
        );
    }

    public ValuationStrategy getStrategy(String key) {
        if (key == null || key.isBlank()) {
            return strategies.get("AUTO");
        }
        return strategies.getOrDefault(key.trim().toUpperCase(), strategies.get("AUTO"));
    }
}

