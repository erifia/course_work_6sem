package com.example.coursework6sem.application.service.auth;

import com.example.coursework6sem.application.usecase.auth.GetProfileUseCase;
import com.example.coursework6sem.application.usecase.auth.UpdateProfileUseCase;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import com.example.coursework6sem.web.dto.auth.ProfileRequests;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final GetProfileUseCase delegate;
    private final UpdateProfileUseCase updateDelegate;

    public ProfileService(GetProfileUseCase delegate, UpdateProfileUseCase updateDelegate) {
        this.delegate = delegate;
        this.updateDelegate = updateDelegate;
    }

    public AuthResponses.ProfileResponse profile() {
        return delegate.execute();
    }

    public AuthResponses.ProfileResponse update(ProfileRequests.UpdateProfileRequest request) {
        return updateDelegate.execute(request);
    }
}

