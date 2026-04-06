package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<DistrictEntity, Long> {
    Optional<DistrictEntity> findByNameIgnoreCase(String name);
}

