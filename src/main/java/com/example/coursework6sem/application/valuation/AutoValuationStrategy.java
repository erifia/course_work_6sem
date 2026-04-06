package com.example.coursework6sem.application.valuation;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.domain.exceptions.DomainException;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AutoValuationStrategy implements ValuationStrategy {

    @Override
    public EstateValuationResponse calculate(EstateEntity estate, DistrictEntity district) {
        if (estate == null) throw new DomainException("estate is null");
        if (district == null) throw new DomainException("district is null");
        if (district.getAvgPrice() == null) {
            throw new DomainException("Рыночные данные для района не найдены");
        }

        double area = estate.getArea().doubleValue();
        double avgPrice = district.getAvgPrice().doubleValue();

        // base = площадь * средняя цена за квадрат
        double base = area * avgPrice;

        double roomMultiplier = switch (estate.getRooms()) {
            case 1 -> 0.9;
            case 2 -> 1.0;
            case 3 -> 1.1;
            case 4 -> 1.2;
            case 5 -> 1.3;
            default -> 1.0;
        };

        // demand multiplier
        double demandMultiplier = 1.0;
        if (district.getDemandLevel() != null) {
            demandMultiplier = 1 + (district.getDemandLevel() - 5) * 0.05;
        }

        // condition multiplier
        double conditionMultiplier = switch (estate.getConditionCode()) {
            case REQUIRES_REPAIR -> 1.0;
            case AVERAGE -> 1.15;
            case GOOD -> 1.30;
            case EXCELLENT -> 1.50;
        };

        // floor multiplier
        double floorMultiplier = 1.0;
        if (estate.getFloor() != null && estate.getTotalFloors() != null) {
            if (estate.getFloor() == 1 || estate.getFloor().equals(estate.getTotalFloors())) {
                floorMultiplier = 0.95;
            } else {
                int middleFloors = (int) Math.floor(estate.getTotalFloors() / 2.0);
                if (estate.getFloor() >= middleFloors - 1 && estate.getFloor() <= middleFloors + 1) {
                    floorMultiplier = 1.02;
                }
            }
        }

        double estimated = base * roomMultiplier * demandMultiplier * conditionMultiplier * floorMultiplier;
        BigDecimal estimatedValue = BigDecimal.valueOf(Math.round(estimated));

        return new EstateValuationResponse(
                district.getAvgPrice(),
                BigDecimal.valueOf(Math.round(base)),
                roomMultiplier,
                demandMultiplier,
                conditionMultiplier,
                floorMultiplier,
                estimatedValue
        );
    }
}

