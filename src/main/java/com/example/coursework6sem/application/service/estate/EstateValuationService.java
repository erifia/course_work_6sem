package com.example.coursework6sem.application.service.estate;

import com.example.coursework6sem.application.usecase.estate.CalculateEstateValueUseCase;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import org.springframework.stereotype.Service;

@Service
public class EstateValuationService {

    private final CalculateEstateValueUseCase delegate;

    public EstateValuationService(CalculateEstateValueUseCase delegate) {
        this.delegate = delegate;
    }

    public EstateValuationResponse calculate(long estateId, String methodKey) {
        return delegate.execute(estateId, methodKey);
    }
}

