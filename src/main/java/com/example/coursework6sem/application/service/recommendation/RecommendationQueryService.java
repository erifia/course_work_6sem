package com.example.coursework6sem.application.service.recommendation;

import com.example.coursework6sem.application.usecase.recommendation.ListRecommendationsUseCase;
import com.example.coursework6sem.web.dto.recommendation.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationQueryService {

    private final ListRecommendationsUseCase delegate;

    public RecommendationQueryService(ListRecommendationsUseCase delegate) {
        this.delegate = delegate;
    }

    public List<RecommendationResponse> list(int limit) {
        return delegate.execute(limit);
    }
}

