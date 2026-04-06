package com.example.coursework6sem.external;

import com.example.coursework6sem.security.TokenHasher;
import com.example.coursework6sem.web.dto.external.GeocodeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Service
public class GeocodingService {

    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final HttpClient http;
    private final TokenHasher tokenHasher;

    public GeocodingService(StringRedisTemplate redis, ObjectMapper mapper, TokenHasher tokenHasher) {
        this.redis = redis;
        this.mapper = mapper;
        this.tokenHasher = tokenHasher;
        this.http = HttpClient.newHttpClient();
    }

    public GeocodeResponse geocode(String address) {
        String addr = address == null ? null : address.trim();
        if (addr == null || addr.isBlank()) {
            throw new IllegalArgumentException("address обязателен");
        }

        String key = "geo:nominatim:" + tokenHasher.sha256Hex(addr.toLowerCase());
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                try {
                    return mapper.readValue(cached, GeocodeResponse.class);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
            // redis необязателен
        }

        try {
            String q = URLEncoder.encode(addr, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + q;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "course-work-6sem/1.0")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new IllegalArgumentException("Не удалось выполнить геокодирование");
            }

            JsonNode arr = mapper.readTree(resp.body());
            if (!arr.isArray() || arr.isEmpty()) {
                throw new IllegalArgumentException("По адресу ничего не найдено");
            }
            JsonNode first = arr.get(0);
            double lat = Double.parseDouble(first.get("lat").asText());
            double lon = Double.parseDouble(first.get("lon").asText());
            String display = first.get("display_name").asText();

            GeocodeResponse out = new GeocodeResponse(display, lat, lon);
            try {
                redis.opsForValue().set(key, mapper.writeValueAsString(out), TTL);
            } catch (Exception ignored) {
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка внешнего API Nominatim: " + e.getMessage());
        }
    }
}

