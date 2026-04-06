package com.example.coursework6sem.web.dto.review;

import jakarta.validation.constraints.*;

public final class ReviewRequests {
    private ReviewRequests() {
    }

    public record CreateRequest(
            @Min(1) @Max(5) Integer rating,
            String comment
    ) {
    }

    public record UpdateRequest(
            @Min(1) @Max(5) Integer rating,
            String comment
    ) {
    }
}

