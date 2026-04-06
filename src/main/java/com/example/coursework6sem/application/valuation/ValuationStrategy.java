package com.example.coursework6sem.application.valuation;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;

public interface ValuationStrategy {
    EstateValuationResponse calculate(EstateEntity estate, DistrictEntity district);
}

