package com.example.coursework6sem.application.service.recommendation;

import com.example.coursework6sem.application.usecase.recommendation.GenerateRecommendationsUseCase;
import com.example.coursework6sem.web.dto.recommendation.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RecommendationGenerationService {

    private final GenerateRecommendationsUseCase delegate;

    public RecommendationGenerationService(GenerateRecommendationsUseCase delegate) {
        this.delegate = delegate;
    }

    public List<RecommendationResponse> generate(Set<Long> districtIds, int limit) {
        return delegate.execute(districtIds, limit);
    }
}

