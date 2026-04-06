package com.example.coursework6sem.application.usecase.estate;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.web.dto.valuation.EstateComparisonResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CompareEstatesUseCase {

    private final EstateRepository estates;

    public CompareEstatesUseCase(EstateRepository estates) {
        this.estates = estates;
    }

    @Transactional(readOnly = true)
    public EstateComparisonResponse execute(long id1, long id2) {
        EstateEntity e1 = estates.findById(id1).orElseThrow(() -> new IllegalArgumentException("Объект 1 не найден"));
        EstateEntity e2 = estates.findById(id2).orElseThrow(() -> new IllegalArgumentException("Объект 2 не найден"));

        BigDecimal pricePerSquare1 = e1.getPrice().divide(e1.getArea(), BigDecimal.ROUND_HALF_UP);
        BigDecimal pricePerSquare2 = e2.getPrice().divide(e2.getArea(), BigDecimal.ROUND_HALF_UP);

        EstateComparisonResponse.EstateSummary s1 = new EstateComparisonResponse.EstateSummary(
                e1.getId(),
                e1.getAddress(),
                e1.getPrice(),
                e1.getArea(),
                e1.getRooms(),
                pricePerSquare1
        );

        EstateComparisonResponse.EstateSummary s2 = new EstateComparisonResponse.EstateSummary(
                e2.getId(),
                e2.getAddress(),
                e2.getPrice(),
                e2.getArea(),
                e2.getRooms(),
                pricePerSquare2
        );

        EstateComparisonResponse.Differences diffs = new EstateComparisonResponse.Differences(
                e1.getPrice().subtract(e2.getPrice()),
                e1.getArea().subtract(e2.getArea()),
                e1.getRooms() - e2.getRooms(),
                pricePerSquare1.subtract(pricePerSquare2)
        );

        return new EstateComparisonResponse(s1, s2, diffs);
    }
}

