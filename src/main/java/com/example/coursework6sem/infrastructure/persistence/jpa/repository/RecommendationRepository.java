package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RecommendationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {
    List<RecommendationEntity> findByUser_IdOrderByScoreDescCreatedAtDesc(Long userId, Pageable pageable);

    Optional<RecommendationEntity> findByUser_IdAndEstate_Id(Long userId, Long estateId);

    void deleteByUser_Id(Long userId);
}

