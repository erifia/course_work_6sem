package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Page<ReviewEntity> findByEstate_IdOrderByCreatedAtDesc(Long estateId, Pageable pageable);
}

