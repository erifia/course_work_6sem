package com.example.coursework6sem.application.service.auth;

import com.example.coursework6sem.application.usecase.auth.RegisterUseCase;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final RegisterUseCase delegate;

    public RegistrationService(RegisterUseCase delegate) {
        this.delegate = delegate;
    }

    public AuthResponses.AuthResponse register(AuthRequests.RegisterRequest request) {
        return delegate.execute(request);
    }
}

