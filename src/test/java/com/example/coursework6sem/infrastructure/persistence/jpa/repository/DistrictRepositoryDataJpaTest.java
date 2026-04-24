package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.testutil.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DistrictRepositoryDataJpaTest extends PostgresTestContainerConfig {

    @Autowired
    DistrictRepository districtRepository;

    @Test
    void findByNameIgnoreCase_findsByCaseInsensitiveName() {
        districtRepository.save(new DistrictEntity("Центр", new BigDecimal("1000.00"), 7));

        assertThat(districtRepository.findByNameIgnoreCase("центр")).isPresent();
        assertThat(districtRepository.findByNameIgnoreCase("ЦЕНТР")).isPresent();
        assertThat(districtRepository.findByNameIgnoreCase("не существует")).isEmpty();
    }
}

