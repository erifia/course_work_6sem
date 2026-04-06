package com.example.coursework6sem.application.usecase.recommendation;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RecommendationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserPreferenceEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RecommendationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserPreferenceRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.spec.EstateSpecifications;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.recommendation.RecommendationResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenerateRecommendationsUseCase {

    private final UserPreferenceRepository preferences;
    private final UserAccountRepository users;
    private final EstateRepository estates;
    private final RecommendationRepository recommendations;
    private final SecurityUtils securityUtils;

    public GenerateRecommendationsUseCase(UserPreferenceRepository preferences,
                                          UserAccountRepository users,
                                          EstateRepository estates,
                                          RecommendationRepository recommendations,
                                          SecurityUtils securityUtils) {
        this.preferences = preferences;
        this.users = users;
        this.estates = estates;
        this.recommendations = recommendations;
        this.securityUtils = securityUtils;
    }

    private record Scored(EstateEntity estate, int score, int scorePercent) {
    }

    @Transactional
    public List<RecommendationResponse> execute(Set<Long> districtIds, int limit) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        UserAccountEntity user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        UserPreferenceEntity pref = preferences.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Предпочтения не установлены"));

        ConditionCode cond = pref.getConditionCode();

        Specification<EstateEntity> spec = Specification
                .where(EstateSpecifications.districtIdsIn(districtIds))
                .and(EstateSpecifications.conditionIs(cond))
                .and(EstateSpecifications.floorBetween(pref.getMinFloor(), pref.getMaxFloor()));

        List<EstateEntity> all = estates.findAll(spec);

        // score maxScore = 12 (3 points for 4 criteria)
        int maxScore = 12;

        List<Scored> scored = all.stream()
                .map(e -> {
                    int score = 0;
                    score += calculateScore(e.getPrice(), pref.getMinPrice(), pref.getMaxPrice());
                    score += calculateScore(e.getArea(), pref.getMinArea(), pref.getMaxArea());
                    score += calculateScore(e.getRooms(), pref.getMinRooms(), pref.getMaxRooms());

                    if (districtIds != null && !districtIds.isEmpty() && districtIds.contains(e.getDistrict().getId())) {
                        score += 3;
                    }

                    int percent = (int) Math.round((score / (double) maxScore) * 100.0);
                    return new Scored(e, score, percent);
                })
                .sorted(Comparator
                        .comparingInt(Scored::score).reversed()
                        .thenComparingLong(s -> s.estate().getId()))
                .collect(Collectors.toList());

        List<Scored> top = scored.stream().limit(Math.max(1, limit)).toList();

        Instant now = Instant.now();
        var saved = top.stream()
                .map(s -> {
                    var existing = recommendations.findByUser_IdAndEstate_Id(userId, s.estate().getId());
                    if (existing.isPresent()) {
                        RecommendationEntity rec = existing.get();
                        rec.setScore(BigDecimal.valueOf(s.score()));
                        rec.setCreatedAt(now);
                        return recommendations.save(rec);
                    }
                    return recommendations.save(new RecommendationEntity(user, s.estate(), BigDecimal.valueOf(s.score()), now));
                })
                .toList();

        return saved.stream().map(this::toResponse).toList();
    }

    private RecommendationResponse toResponse(RecommendationEntity rec) {
        EstateEntity e = rec.getEstate();
        String condition = ConditionCodeMapper.toRussianLabel(e.getConditionCode());

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
                condition,
                e.getPropertyType(),
                e.getPrice(),
                e.getImagePath(),
                rec.getScore(),
                percent,
                rec.getCreatedAt()
        );
    }

    private int calculateScore(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) return 0;
        return calculateScore(value.doubleValue(), min, max);
    }

    private int calculateScore(Integer value, Integer min, Integer max) {
        if (value == null) return 0;
        double v = value.doubleValue();
        return calculateScore(v, min == null ? null : BigDecimal.valueOf(min), max == null ? null : BigDecimal.valueOf(max));
    }

    private int calculateScore(double value, BigDecimal min, BigDecimal max) {
        if (min != null && max != null) {
            double minD = min.doubleValue();
            double maxD = max.doubleValue();
            double range = maxD - minD;
            if (range <= 0) return 0;
            if (value >= minD && value <= maxD) return 3;

            double deviation = value < minD ? ((minD - value) / range) * 100.0 : ((value - maxD) / range) * 100.0;
            if (deviation <= 10) return 2;
            if (deviation <= 20) return 1;
            return 0;
        }
        if (min != null) {
            double minD = min.doubleValue();
            if (value >= minD) return 3;
            double deviation = ((minD - value) / minD) * 100.0;
            if (deviation <= 10) return 2;
            if (deviation <= 20) return 1;
            return 0;
        }
        if (max != null) {
            double maxD = max.doubleValue();
            if (value <= maxD) return 3;
            double deviation = ((value - maxD) / maxD) * 100.0;
            if (deviation <= 10) return 2;
            if (deviation <= 20) return 1;
            return 0;
        }
        return 0;
    }
}

