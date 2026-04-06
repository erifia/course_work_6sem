package com.example.coursework6sem.application.service.estate;

import com.example.coursework6sem.application.usecase.estate.PredictEstateValueUseCase;
import com.example.coursework6sem.web.dto.valuation.EstatePredictionResponse;
import org.springframework.stereotype.Service;

@Service
public class EstatePredictionService {

    private final PredictEstateValueUseCase delegate;

    public EstatePredictionService(PredictEstateValueUseCase delegate) {
        this.delegate = delegate;
    }

    public EstatePredictionResponse predict(long estateId, int months) {
        return delegate.execute(estateId, months);
    }
}

