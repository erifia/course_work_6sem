package com.example.coursework6sem.application.service.evaluation;

import com.example.coursework6sem.application.valuation.AutoValuationStrategy;
import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ManualEvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RoleEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.ManualEvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManualEvaluationServiceTest {

    @Mock
    DistrictRepository districts;
    @Mock
    UserAccountRepository users;
    @Mock
    ManualEvaluationRepository manualEvaluations;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    AutoValuationStrategy autoValuationStrategy;

    @InjectMocks
    ManualEvaluationService service;

    @Test
    void evaluateAndSave_throwsWhenUnauthorized() {
        when(securityUtils.currentUserAccountId()).thenReturn(Optional.empty());

        EvaluationRequests.ManualCreateRequest request = new EvaluationRequests.ManualCreateRequest(
                1L, "ул. Ленина, 1", 2, new BigDecimal("45.5"),
                3, 9, "хорошее", "описание"
        );

        assertThatThrownBy(() -> service.evaluateAndSave(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Требуется авторизация");
        verifyNoInteractions(users, districts, manualEvaluations, autoValuationStrategy);
    }

    @Test
    void evaluateAndSave_throwsWhenConditionInvalid() {
        when(securityUtils.currentUserAccountId()).thenReturn(Optional.of(1L));
        when(users.findById(1L)).thenReturn(Optional.of(new UserAccountEntity(
                "u", "hash", "u@mail", new RoleEntity(com.example.coursework6sem.domain.RoleName.CLIENT), Instant.now()
        )));
        when(districts.findById(1L)).thenReturn(Optional.of(new DistrictEntity("Центр", BigDecimal.TEN, 5)));

        EvaluationRequests.ManualCreateRequest request = new EvaluationRequests.ManualCreateRequest(
                1L, "ул. Ленина, 1", 2, new BigDecimal("45.5"),
                3, 9, "НЕИЗВЕСТНО", "описание"
        );

        assertThatThrownBy(() -> service.evaluateAndSave(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Некорректное состояние");
        verifyNoInteractions(autoValuationStrategy, manualEvaluations);
    }

    @Test
    void evaluateAndSave_savesManualEvaluationWithEstimatedValue() {
        when(securityUtils.currentUserAccountId()).thenReturn(Optional.of(10L));

        RoleEntity role = new RoleEntity(com.example.coursework6sem.domain.RoleName.APPRAISER);
        UserAccountEntity me = new UserAccountEntity("me", "hash", "me@mail", role, Instant.now());
        DistrictEntity district = new DistrictEntity("Центр", new BigDecimal("1000.00"), 8);

        when(users.findById(10L)).thenReturn(Optional.of(me));
        when(districts.findById(5L)).thenReturn(Optional.of(district));
        when(autoValuationStrategy.calculate(any(), eq(district)))
                .thenReturn(new EstateValuationResponse(
                        new BigDecimal("1000.00"),
                        new BigDecimal("100000.00"),
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        new BigDecimal("123456.78")
                ));

        when(manualEvaluations.save(any(ManualEvaluationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluationRequests.ManualCreateRequest request = new EvaluationRequests.ManualCreateRequest(
                5L, "ул. Ленина, 1", 2, new BigDecimal("45.5"),
                3, 9, "хорошее", "описание"
        );

        var response = service.evaluateAndSave(request);

        ArgumentCaptor<ManualEvaluationEntity> captor = ArgumentCaptor.forClass(ManualEvaluationEntity.class);
        verify(manualEvaluations).save(captor.capture());
        ManualEvaluationEntity saved = captor.getValue();

        assertThat(saved.getDistrict()).isSameAs(district);
        assertThat(saved.getAddress()).isEqualTo("ул. Ленина, 1");
        assertThat(saved.getRooms()).isEqualTo(2);
        assertThat(saved.getArea()).isEqualByComparingTo("45.5");
        assertThat(saved.getFloor()).isEqualTo(3);
        assertThat(saved.getTotalFloors()).isEqualTo(9);
        assertThat(saved.getConditionCode()).isEqualTo(ConditionCode.GOOD);
        assertThat(saved.getEstimatedValue()).isEqualByComparingTo("123456.78");

        assertThat(response.estimatedValue()).isEqualByComparingTo("123456.78");
        assertThat(response.condition()).isEqualTo("хорошее");
    }
}

