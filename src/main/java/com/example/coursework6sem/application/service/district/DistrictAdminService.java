package com.example.coursework6sem.application.service.district;

import com.example.coursework6sem.application.usecase.district.CreateDistrictUseCase;
import com.example.coursework6sem.web.dto.DistrictRequests;
import com.example.coursework6sem.web.dto.DistrictResponse;
import org.springframework.stereotype.Service;

@Service
public class DistrictAdminService {

    private final CreateDistrictUseCase delegate;

    public DistrictAdminService(CreateDistrictUseCase delegate) {
        this.delegate = delegate;
    }

    public DistrictResponse create(DistrictRequests request) {
        return delegate.execute(request);
    }
}

