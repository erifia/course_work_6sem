package com.example.coursework6sem.application.usecase.recommendation;

import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RecommendationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RecommendationRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.recommendation.RecommendationResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListRecommendationsUseCase {

    private final RecommendationRepository recommendations;
    private final SecurityUtils securityUtils;

    public ListRecommendationsUseCase(RecommendationRepository recommendations, SecurityUtils securityUtils) {
        this.recommendations = recommendations;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> execute(int limit) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));

        var page = recommendations.findByUser_IdOrderByScoreDescCreatedAtDesc(userId, PageRequest.of(0, Math.max(1, limit)));
        return page.stream().map(this::toResponse).toList();
    }

    private RecommendationResponse toResponse(RecommendationEntity rec) {
        var e = rec.getEstate();
        int score = rec.getScore() == null ? 0 : rec.getScore().intValue();
        int maxScore = 12;
        int percent = (int) Math.round((score / (double) maxScore) * 100.0);

        return new RecommendationResponse(
                rec.getId(),
                e.getId(),
                e.getAddress(),
                e.getDistrict().getId(),
                e.getDistrict().getName(),
                e.getRooms(),
                e.getArea(),
                e.getFloor(),
                e.getTotalFloors(),
                ConditionCodeMapper.toRussianLabel(e.getConditionCode()),
                e.getPropertyType(),
                e.getPrice(),
                e.getImagePath(),
                rec.getScore(),
                percent,
                rec.getCreatedAt()
        );
    }
}

