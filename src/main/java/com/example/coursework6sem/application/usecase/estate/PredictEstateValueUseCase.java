package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.web.dto.valuation.EstatePredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PredictEstateValueUseCase {
    private final EstateRepository estates;

    public PredictEstateValueUseCase(EstateRepository estates) {
        this.estates = estates;
    }

    @Transactional(readOnly = true)
    public EstatePredictionResponse execute(long estateId, int months) {
        EstateEntity estate = estates.findById(estateId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));

        DistrictEntity district = estate.getDistrict();
        if (district.getDemandLevel() == null) {
            throw new IllegalArgumentException("Рыночные данные не найдены");
        }

        double growthRate = (district.getDemandLevel() - 5) * 0.01;
        double monthlyGrowth = growthRate / 12.0;

        double predicted = estate.getPrice().doubleValue() * Math.pow(1 + monthlyGrowth, months);

        BigDecimal current = estate.getPrice();
        BigDecimal predictedValue = BigDecimal.valueOf(Math.round(predicted));

        return new EstatePredictionResponse(current, predictedValue, growthRate * 100.0, months);
    }
}

