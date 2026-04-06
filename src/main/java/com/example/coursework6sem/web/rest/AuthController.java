package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.auth.LoginService;
import com.example.coursework6sem.application.service.auth.ProfileService;
import com.example.coursework6sem.application.service.auth.RegistrationService;
import com.example.coursework6sem.application.service.auth.TokenRefreshService;
import com.example.coursework6sem.web.dto.auth.AuthRequests;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final TokenRefreshService tokenRefreshService;
    private final ProfileService profileService;

    public AuthController(RegistrationService registrationService,
                           LoginService loginService,
                           TokenRefreshService tokenRefreshService,
                           ProfileService profileService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.tokenRefreshService = tokenRefreshService;
        this.profileService = profileService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid AuthRequests.RegisterRequest request) {
        try {
            AuthResponses.AuthResponse response = registrationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequests.LoginRequest request) {
        try {
            return ResponseEntity.ok(loginService.login(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid AuthRequests.RefreshRequest request) {
        try {
            return ResponseEntity.ok(tokenRefreshService.refresh(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        try {
            return ResponseEntity.ok(profileService.profile());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

