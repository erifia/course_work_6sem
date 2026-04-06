package com.example.coursework6sem.application.usecase.district;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.web.dto.DistrictResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListDistrictsUseCase {
    private final DistrictRepository districts;

    public ListDistrictsUseCase(DistrictRepository districts) {
        this.districts = districts;
    }

    public List<DistrictResponse> execute() {
        return districts.findAll().stream()
                .map(d -> new DistrictResponse(d.getId(), d.getName(), d.getAvgPrice(), d.getDemandLevel()))
                .toList();
    }
}

