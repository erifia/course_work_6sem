package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Page;

public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Long>, JpaSpecificationExecutor<EvaluationEntity> {

    Page<EvaluationEntity> findByEstate_IdOrderByCreatedAtDesc(Long estateId, Pageable pageable);
}

