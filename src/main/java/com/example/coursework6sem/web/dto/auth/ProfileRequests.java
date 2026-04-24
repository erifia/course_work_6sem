package com.example.coursework6sem.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ProfileRequests {
    private ProfileRequests() {
    }

    public record UpdateProfileRequest(
            @NotBlank
            @Size(min = 3, max = 30)
            @Pattern(regexp = "^[A-Za-z0-9_.-]{3,30}$", message = "Логин: только латиница/цифры и символы _.-")
            String username,

            @NotBlank
            @Email
            String email
    ) {
    }
}

