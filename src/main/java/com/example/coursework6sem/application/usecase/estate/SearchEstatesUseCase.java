package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.domain.ConditionCodeMapper;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.spec.EstateSpecifications;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SearchEstatesUseCase {

    private final EstateRepository estates;

    public SearchEstatesUseCase(EstateRepository estates) {
        this.estates = estates;
    }

    @Transactional(readOnly = true)
    public Page<EstateResponse> execute(
            Pageable pageable,
            Long districtId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minArea,
            BigDecimal maxArea,
            Integer minRooms,
            Integer maxRooms,
            String conditionRussian
    ) {
        ConditionCode conditionCode = ConditionCodeMapper.fromRussianLabel(conditionRussian).orElse(null);

        Specification<EstateEntity> spec = Specification.where(null);
        spec = spec.and(EstateSpecifications.excludeManualDrafts());
        spec = spec.and(EstateSpecifications.districtIdIs(districtId));
        spec = spec.and(EstateSpecifications.priceBetween(minPrice, maxPrice));
        spec = spec.and(EstateSpecifications.areaBetween(minArea, maxArea));
        spec = spec.and(EstateSpecifications.roomsBetween(minRooms, maxRooms));
        spec = spec.and(EstateSpecifications.conditionIs(conditionCode));

        Page<EstateEntity> page = estates.findAll(spec, pageable);

        return page.map(e -> new EstateResponse(
                e.getId(),
                e.getAddress(),
                e.getDistrict().getId(),
                e.getDistrict().getName(),
                e.getRooms(),
                e.getArea(),
                ConditionCodeMapper.toRussianLabel(e.getConditionCode()),
                e.getPropertyType(),
                e.getPrice(),
                e.getFloor(),
                e.getTotalFloors(),
                e.getDescription(),
                e.getImagePath(),
                e.getCreatedAt()
        ));
    }
}

