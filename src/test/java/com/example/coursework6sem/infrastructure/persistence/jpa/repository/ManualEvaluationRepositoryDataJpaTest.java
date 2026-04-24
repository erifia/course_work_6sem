package com.example.coursework6sem.infrastructure.persistence.jpa.repository;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ManualEvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RoleEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.testutil.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ManualEvaluationRepositoryDataJpaTest extends PostgresTestContainerConfig {

    @Autowired
    ManualEvaluationRepository manualEvaluationRepository;
    @Autowired
    DistrictRepository districtRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserAccountRepository userAccountRepository;

    @Test
    void saveAndFindById_roundTrip() {
        RoleEntity role = roleRepository.save(new RoleEntity(RoleName.APPRAISER));
        UserAccountEntity user = userAccountRepository.save(new UserAccountEntity(
                "appraiser1", "hash", "a1@mail", role, Instant.now()
        ));
        DistrictEntity district = districtRepository.save(new DistrictEntity("Центр", new BigDecimal("1000.00"), 7));

        ManualEvaluationEntity saved = manualEvaluationRepository.save(new ManualEvaluationEntity(
                district,
                user,
                "ул. Ленина, 1",
                2,
                new BigDecimal("45.50"),
                3,
                9,
                ConditionCode.GOOD,
                "описание",
                new BigDecimal("123456.78"),
                Instant.now(),
                Instant.now()
        ));

        var loaded = manualEvaluationRepository.findById(saved.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getEstimatedValue()).isEqualByComparingTo("123456.78");
        assertThat(loaded.get().getDistrict().getName()).isEqualTo("Центр");
        assertThat(loaded.get().getAppraiser().getUsername()).isEqualTo("appraiser1");
    }
}

