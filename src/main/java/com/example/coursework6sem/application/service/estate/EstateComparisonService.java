package com.example.coursework6sem.application.service.estate;

import com.example.coursework6sem.application.usecase.estate.CompareEstatesUseCase;
import com.example.coursework6sem.web.dto.valuation.EstateComparisonResponse;
import org.springframework.stereotype.Service;

@Service
public class EstateComparisonService {

    private final CompareEstatesUseCase delegate;

    public EstateComparisonService(CompareEstatesUseCase delegate) {
        this.delegate = delegate;
    }

    public EstateComparisonResponse execute(long id1, long id2) {
        return delegate.execute(id1, id2);
    }
}

