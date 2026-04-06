package com.example.coursework6sem.web.dto.review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long estateId,
        Long userAccountId,
        String username,
        Integer rating,
        String comment,
        Instant createdAt
) {
}

