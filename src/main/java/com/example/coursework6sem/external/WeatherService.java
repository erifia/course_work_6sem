package com.example.coursework6sem.external;

import com.example.coursework6sem.security.TokenHasher;
import com.example.coursework6sem.web.dto.external.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

@Service
public class WeatherService {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final HttpClient http;
    private final TokenHasher tokenHasher;

    public WeatherService(StringRedisTemplate redis, ObjectMapper mapper, TokenHasher tokenHasher) {
        this.redis = redis;
        this.mapper = mapper;
        this.tokenHasher = tokenHasher;
        this.http = HttpClient.newHttpClient();
    }

    public WeatherResponse getCurrent(double lat, double lon) {
        double rLat = Math.round(lat * 100.0) / 100.0;
        double rLon = Math.round(lon * 100.0) / 100.0;

        String key = "weather:openmeteo:" + tokenHasher.sha256Hex(rLat + ":" + rLon);
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                try {
                    return mapper.readValue(cached, WeatherResponse.class);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
            // redis необязателен
        }

        try {
            String url = String.format(Locale.ROOT,
                    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m&timezone=UTC",
                    rLat, rLon);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "course-work-6sem/1.0")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new IllegalArgumentException("Не удалось получить погоду");
            }

            JsonNode root = mapper.readTree(resp.body());
            JsonNode current = root.get("current");
            if (current == null) {
                throw new IllegalArgumentException("Некорректный ответ Open-Meteo");
            }

            double temp = current.get("temperature_2m").asDouble();
            JsonNode units = root.get("current_units");
            String unitsStr = units != null && units.has("temperature_2m") ? units.get("temperature_2m").asText() : "";
            String timezone = root.has("timezone") ? root.get("timezone").asText() : "UTC";

            WeatherResponse out = new WeatherResponse(temp, unitsStr, timezone);
            try {
                redis.opsForValue().set(key, mapper.writeValueAsString(out), TTL);
            } catch (Exception ignored) {
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка внешнего API Open-Meteo: " + e.getMessage());
        }
    }
}

