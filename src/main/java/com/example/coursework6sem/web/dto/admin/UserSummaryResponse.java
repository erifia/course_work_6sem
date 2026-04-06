package com.example.coursework6sem.web.dto.admin;

import com.example.coursework6sem.domain.RoleName;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        RoleName role
) {
}

