package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.estate.EstateRequests;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UpdateEstateUseCase {

    private final EstateRepository estates;
    private final DistrictRepository districts;
    private final SecurityUtils securityUtils;

    public UpdateEstateUseCase(EstateRepository estates, DistrictRepository districts, SecurityUtils securityUtils) {
        this.estates = estates;
        this.districts = districts;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public EstateResponse execute(long estateId, EstateRequests.UpdateRequest request) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        EstateEntity estate = estates.findById(estateId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));

        boolean isAdmin = role.equalsIgnoreCase(RoleName.ADMIN.name());
        boolean isOwner = estate.getCreatedBy() != null && estate.getCreatedBy().getId().equals(userId);
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        DistrictEntity district = districts.findById(request.districtId())
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        ConditionCode conditionCode = ConditionCodeMapper.fromRussianLabel(request.condition())
                .orElseThrow(() -> new IllegalArgumentException("Некорректное состояние"));
        if (request.totalFloors() < request.floor()) {
            throw new IllegalArgumentException("Этаж не может быть больше этажности дома");
        }

        estate.setAddress(request.address());
        estate.setRooms(request.rooms());
        estate.setArea(request.area());
        estate.setDistrict(district);
        estate.setPrice(request.price());
        estate.setFloor(request.floor());
        estate.setTotalFloors(request.totalFloors());
        estate.setConditionCode(conditionCode);
        estate.setDescription(request.description());
        estate.setPropertyType(request.propertyType() == null || request.propertyType().isBlank() ? estate.getPropertyType() : request.propertyType());
        estate.setImagePath(request.imagePath());
        estate.setUpdatedAt(Instant.now());

        EstateEntity saved = estates.save(estate);

        return new EstateResponse(
                saved.getId(),
                saved.getAddress(),
                saved.getDistrict().getId(),
                saved.getDistrict().getName(),
                saved.getRooms(),
                saved.getArea(),
                ConditionCodeMapper.toRussianLabel(saved.getConditionCode()),
                saved.getPropertyType(),
                saved.getPrice(),
                saved.getFloor(),
                saved.getTotalFloors(),
                saved.getDescription(),
                saved.getImagePath(),
                saved.getCreatedAt()
        );
    }
}

