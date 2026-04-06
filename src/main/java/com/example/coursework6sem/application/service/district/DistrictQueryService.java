package com.example.coursework6sem.application.service.district;

import com.example.coursework6sem.application.usecase.district.ListDistrictsUseCase;
import com.example.coursework6sem.web.dto.DistrictResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictQueryService {

    private final ListDistrictsUseCase delegate;

    public DistrictQueryService(ListDistrictsUseCase delegate) {
        this.delegate = delegate;
    }

    public List<DistrictResponse> list() {
        return delegate.execute();
    }
}

