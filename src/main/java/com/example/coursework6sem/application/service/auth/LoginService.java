package com.example.coursework6sem.application.service.auth;

import com.example.coursework6sem.application.usecase.auth.LoginUseCase;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final LoginUseCase delegate;

    public LoginService(LoginUseCase delegate) {
        this.delegate = delegate;
    }

    public AuthResponses.AuthResponse login(AuthRequests.LoginRequest request) {
        return delegate.execute(request);
    }
}

