package com.example.coursework6sem.application.service.estate;

import com.example.coursework6sem.application.usecase.estate.CreateEstateUseCase;
import com.example.coursework6sem.application.usecase.estate.DeleteEstateUseCase;
import com.example.coursework6sem.application.usecase.estate.UpdateEstateUseCase;
import com.example.coursework6sem.web.dto.estate.EstateRequests;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import org.springframework.stereotype.Service;

@Service
public class EstateCommandService {

    private final CreateEstateUseCase createDelegate;
    private final UpdateEstateUseCase updateDelegate;
    private final DeleteEstateUseCase deleteDelegate;

    public EstateCommandService(CreateEstateUseCase createDelegate,
                                  UpdateEstateUseCase updateDelegate,
                                  DeleteEstateUseCase deleteDelegate) {
        this.createDelegate = createDelegate;
        this.updateDelegate = updateDelegate;
        this.deleteDelegate = deleteDelegate;
    }

    public EstateResponse create(EstateRequests.CreateRequest request) {
        return createDelegate.execute(request);
    }

    public EstateResponse update(long estateId, EstateRequests.UpdateRequest request) {
        return updateDelegate.execute(estateId, request);
    }

    public void delete(long estateId) {
        deleteDelegate.execute(estateId);
    }
}

