package com.example.coursework6sem.web.dto.external;

public record WeatherResponse(
        double temperature2m,
        String units,
        String timezone
) {
}

