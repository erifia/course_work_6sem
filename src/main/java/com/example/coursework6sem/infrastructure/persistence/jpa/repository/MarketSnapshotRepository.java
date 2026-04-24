package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.MarketSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MarketSnapshotRepository extends JpaRepository<MarketSnapshotEntity, Long> {
    boolean existsByDistrict_IdAndSnapshotMonthAndPropertyType(Long districtId, LocalDate snapshotMonth, String propertyType);
}

