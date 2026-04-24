package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.application.events.EstateCreatedEvent;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.estate.EstateRequests;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

@Service
public class CreateEstateUseCase {

    private final EstateRepository estates;
    private final DistrictRepository districts;
    private final UserAccountRepository users;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventPublisher;

    public CreateEstateUseCase(EstateRepository estates,
                                DistrictRepository districts,
                                UserAccountRepository users,
                                SecurityUtils securityUtils,
                                ApplicationEventPublisher eventPublisher) {
        this.estates = estates;
        this.districts = districts;
        this.users = users;
        this.securityUtils = securityUtils;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EstateResponse execute(EstateRequests.CreateRequest request) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        String role = securityUtils.currentRole().orElseThrow(() -> new IllegalStateException("Роль не найдена"));
        boolean allowed = role.equalsIgnoreCase(RoleName.ADMIN.name()) || role.equalsIgnoreCase(RoleName.APPRAISER.name());
        if (!allowed) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        DistrictEntity district = districts.findById(request.districtId())
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        ConditionCode conditionCode = ConditionCodeMapper.fromRussianLabel(request.condition())
                .orElseThrow(() -> new IllegalArgumentException("Некорректное состояние"));
        if (request.totalFloors() < request.floor()) {
            throw new IllegalArgumentException("Этаж не может быть больше этажности дома");
        }

        Instant now = Instant.now();

        UserAccountEntity createdBy = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        EstateEntity entity = new EstateEntity(
                district,
                createdBy,
                request.address(),
                request.propertyType() == null || request.propertyType().isBlank() ? "APARTMENT" : request.propertyType(),
                request.rooms(),
                request.area(),
                request.price(),
                request.floor(),
                request.totalFloors(),
                conditionCode,
                request.description(),
                request.imagePath(),
                now,
                now
        );

        EstateEntity saved = estates.save(entity);

        // Observer pattern: реагируем на создание объекта через Spring events.
        eventPublisher.publishEvent(new EstateCreatedEvent(saved.getId(), createdBy.getId()));

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

