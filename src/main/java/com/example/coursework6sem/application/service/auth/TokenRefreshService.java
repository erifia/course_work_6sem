package com.example.coursework6sem.application.service.auth;

import com.example.coursework6sem.application.usecase.auth.RefreshUseCase;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.stereotype.Service;

@Service
public class TokenRefreshService {

    private final RefreshUseCase delegate;

    public TokenRefreshService(RefreshUseCase delegate) {
        this.delegate = delegate;
    }

    public AuthResponses.AuthResponse refresh(AuthRequests.RefreshRequest request) {
        return delegate.execute(request);
    }
}

