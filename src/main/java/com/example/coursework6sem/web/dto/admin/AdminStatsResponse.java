package com.example.coursework6sem.web.dto.admin;

public record AdminStatsResponse(
        long usersCount,
        long estatesCount,
        long evaluationsCount
) {
}

