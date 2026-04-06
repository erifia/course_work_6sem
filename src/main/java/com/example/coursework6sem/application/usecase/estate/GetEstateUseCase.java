package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetEstateUseCase {

    private final EstateRepository estates;

    public GetEstateUseCase(EstateRepository estates) {
        this.estates = estates;
    }

    @Transactional(readOnly = true)
    public EstateResponse execute(long estateId) {
        EstateEntity estate = estates.findById(estateId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));

        String condition = ConditionCodeMapper.toRussianLabel(estate.getConditionCode());
        String districtName = Optional.ofNullable(estate.getDistrict())
                .map(d -> d.getName())
                .orElse(null);

        return new EstateResponse(
                estate.getId(),
                estate.getAddress(),
                estate.getDistrict().getId(),
                districtName,
                estate.getRooms(),
                estate.getArea(),
                condition,
                estate.getPropertyType(),
                estate.getPrice(),
                estate.getFloor(),
                estate.getTotalFloors(),
                estate.getDescription(),
                estate.getImagePath(),
                estate.getCreatedAt()
        );
    }
}

