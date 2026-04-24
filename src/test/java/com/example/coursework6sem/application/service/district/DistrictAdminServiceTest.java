package com.example.coursework6sem.application.service.district;

import com.example.coursework6sem.application.usecase.district.CreateDistrictUseCase;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.DistrictRequests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistrictAdminServiceTest {

    @Mock
    CreateDistrictUseCase delegate;
    @Mock
    DistrictRepository districts;
    @Mock
    SecurityUtils securityUtils;

    @InjectMocks
    DistrictAdminService service;

    @Test
    void update_throwsWhenNotAuthorized() {
        when(securityUtils.currentRole()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, new DistrictRequests("Центр", BigDecimal.TEN, 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Требуется авторизация");
    }

    @Test
    void update_throwsWhenRoleNotAllowed() {
        when(securityUtils.currentRole()).thenReturn(Optional.of(RoleName.CLIENT.name()));

        assertThatThrownBy(() -> service.update(1L, new DistrictRequests("Центр", BigDecimal.TEN, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно прав");
    }

    @Test
    void update_throwsWhenDistrictNotFound() {
        when(securityUtils.currentRole()).thenReturn(Optional.of(RoleName.ADMIN.name()));
        when(districts.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, new DistrictRequests("Центр", BigDecimal.TEN, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Район не найден");
    }

    @Test
    void update_throwsWhenNameAlreadyExistsOnAnotherDistrict() {
        when(securityUtils.currentRole()).thenReturn(Optional.of(RoleName.APPRAISER.name()));
        DistrictEntity current = new DistrictEntity("Старое", BigDecimal.ONE, 1);
        when(districts.findById(10L)).thenReturn(Optional.of(current));

        DistrictEntity other = mock(DistrictEntity.class);
        when(other.getId()).thenReturn(99L);
        when(districts.findByNameIgnoreCase("Центр")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(10L, new DistrictRequests("Центр", BigDecimal.TEN, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void update_updatesAndSavesDistrict() {
        when(securityUtils.currentRole()).thenReturn(Optional.of(RoleName.ADMIN.name()));
        DistrictEntity current = new DistrictEntity("Старое", BigDecimal.ONE, 1);
        when(districts.findById(10L)).thenReturn(Optional.of(current));
        when(districts.findByNameIgnoreCase("Центр")).thenReturn(Optional.empty());
        when(districts.save(any(DistrictEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(10L, new DistrictRequests("Центр", new BigDecimal("123.45"), 7));

        ArgumentCaptor<DistrictEntity> captor = ArgumentCaptor.forClass(DistrictEntity.class);
        verify(districts).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Центр");
        assertThat(captor.getValue().getAvgPrice()).isEqualByComparingTo("123.45");
        assertThat(captor.getValue().getDemandLevel()).isEqualTo(7);
        verifyNoInteractions(delegate);
    }
}

