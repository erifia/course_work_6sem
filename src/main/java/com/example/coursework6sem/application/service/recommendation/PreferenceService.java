package com.example.coursework6sem.application.service.recommendation;

import com.example.coursework6sem.application.usecase.recommendation.GetUserPreferenceUseCase;
import com.example.coursework6sem.application.usecase.recommendation.UpdateUserPreferenceUseCase;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceRequests;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceResponse;
import org.springframework.stereotype.Service;

@Service
public class PreferenceService {

    private final UpdateUserPreferenceUseCase updateDelegate;
    private final GetUserPreferenceUseCase getDelegate;

    public PreferenceService(UpdateUserPreferenceUseCase updateDelegate, GetUserPreferenceUseCase getDelegate) {
        this.updateDelegate = updateDelegate;
        this.getDelegate = getDelegate;
    }

    public UserPreferenceResponse get() {
        return getDelegate.execute();
    }

    public UserPreferenceResponse save(UserPreferenceRequests request) {
        return updateDelegate.execute(request);
    }
}

