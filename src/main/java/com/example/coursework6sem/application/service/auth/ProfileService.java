package com.example.coursework6sem.application.service.auth;

import com.example.coursework6sem.application.usecase.auth.GetProfileUseCase;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final GetProfileUseCase delegate;

    public ProfileService(GetProfileUseCase delegate) {
        this.delegate = delegate;
    }

    public AuthResponses.ProfileResponse profile() {
        return delegate.execute();
    }
}

