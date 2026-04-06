package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.application.valuation.ValuationStrategy;
import com.example.coursework6sem.application.valuation.ValuationStrategyFactory;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import org.springframework.stereotype.Service;

@Service
public class CalculateEstateValueUseCase {

    private final EstateRepository estates;
    private final ValuationStrategyFactory strategyFactory;

    public CalculateEstateValueUseCase(EstateRepository estates, ValuationStrategyFactory strategyFactory) {
        this.estates = estates;
        this.strategyFactory = strategyFactory;
    }

    public EstateValuationResponse execute(long estateId, String methodKey) {
        EstateEntity estate = estates.findById(estateId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));
        DistrictEntity district = estate.getDistrict();

        ValuationStrategy strategy = strategyFactory.getStrategy(methodKey);
        return strategy.calculate(estate, district);
    }
}

