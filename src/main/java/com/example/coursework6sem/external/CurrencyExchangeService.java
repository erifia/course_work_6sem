package com.example.coursework6sem.external;

import com.example.coursework6sem.security.TokenHasher;
import com.example.coursework6sem.web.dto.external.CurrencyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CurrencyExchangeService {

    private static final Duration TTL = Duration.ofHours(1);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final HttpClient http;
    private final TokenHasher tokenHasher;
    private final String baseCurrency;

    public CurrencyExchangeService(StringRedisTemplate redis,
                                     ObjectMapper mapper,
                                     TokenHasher tokenHasher) {
        this.redis = redis;
        this.mapper = mapper;
        this.tokenHasher = tokenHasher;
        this.http = HttpClient.newHttpClient();
        this.baseCurrency = "USD";
    }

    public CurrencyResponse getRate(String targetCurrency) {
        String target = targetCurrency == null ? null : targetCurrency.trim().toUpperCase();
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("targetCurrency обязателен");
        }
        if (baseCurrency.equalsIgnoreCase(target)) {
            return new CurrencyResponse(baseCurrency, target, BigDecimal.ONE, Instant.now());
        }

        String key = "currency:rate:v2:" + baseCurrency + ":" + target;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                try {
                    return mapper.readValue(cached, CurrencyResponse.class);
                } catch (Exception ignored) {
                    // если кэш битый — перезапросим
                }
            }
        } catch (Exception ignored) {
            // redis необязателен для работы внешнего API
        }

        try {
            String url = "https://open.er-api.com/v6/latest/" + URLEncoder.encode(baseCurrency, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "course-work-6sem/1.0")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new IllegalArgumentException("Не удалось получить курс валют");
            }

            JsonNode root = mapper.readTree(resp.body());
            JsonNode rates = root.get("rates");
            if (rates == null || !rates.has(target)) {
                throw new IllegalArgumentException("Валюта не поддерживается: " + target);
            }

            BigDecimal rate = new BigDecimal(rates.get(target).asText());
            CurrencyResponse out = new CurrencyResponse(baseCurrency, target, rate, Instant.now());

            try {
                redis.opsForValue().set(key, mapper.writeValueAsString(out), TTL);
            } catch (Exception ignored) {
                // кеш не критичен
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка внешнего API курса валют: " + e.getMessage());
        }
    }

    public List<String> getSupportedTargets() {
        try {
            String url = "https://open.er-api.com/v6/latest/" + URLEncoder.encode(baseCurrency, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "course-work-6sem/1.0")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new IllegalArgumentException("Не удалось получить список валют");
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode rates = root.get("rates");
            if (rates == null || !rates.isObject()) {
                throw new IllegalArgumentException("Некорректный ответ сервиса валют");
            }
            List<String> desired = List.of("USD", "BYN", "RUB", "CNY", "PLN");
            List<String> out = new ArrayList<>();
            for (String c : desired) {
                if (baseCurrency.equals(c) || rates.has(c)) {
                    out.add(c);
                }
            }
            if (out.isEmpty()) {
                out.add(baseCurrency);
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка внешнего API курса валют: " + e.getMessage());
        }
    }
}

