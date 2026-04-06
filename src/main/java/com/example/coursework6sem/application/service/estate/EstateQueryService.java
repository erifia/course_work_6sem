package com.example.coursework6sem.application.service.estate;

import com.example.coursework6sem.application.usecase.estate.GetEstateUseCase;
import com.example.coursework6sem.application.usecase.estate.SearchEstatesUseCase;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EstateQueryService {

    private final SearchEstatesUseCase searchDelegate;
    private final GetEstateUseCase getDelegate;

    public EstateQueryService(SearchEstatesUseCase searchDelegate, GetEstateUseCase getDelegate) {
        this.searchDelegate = searchDelegate;
        this.getDelegate = getDelegate;
    }

    public Page<EstateResponse> search(
            Pageable pageable,
            Long districtId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minArea,
            BigDecimal maxArea,
            Integer minRooms,
            Integer maxRooms,
            String condition
    ) {
        return searchDelegate.execute(pageable, districtId, minPrice, maxPrice, minArea, maxArea, minRooms, maxRooms, condition);
    }

    public EstateResponse get(long estateId) {
        return getDelegate.execute(estateId);
    }
}

