package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.MarketSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketSnapshotRepository extends JpaRepository<MarketSnapshotEntity, Long> {
}

