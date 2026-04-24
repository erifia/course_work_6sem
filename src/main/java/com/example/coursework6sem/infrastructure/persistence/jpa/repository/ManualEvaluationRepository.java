package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ManualEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ManualEvaluationRepository extends JpaRepository<ManualEvaluationEntity, Long>, JpaSpecificationExecutor<ManualEvaluationEntity> {
}
