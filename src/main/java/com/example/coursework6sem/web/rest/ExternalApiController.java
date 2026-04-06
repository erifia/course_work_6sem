package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.external.CurrencyExchangeService;
import com.example.coursework6sem.external.GeocodingService;
import com.example.coursework6sem.external.WeatherService;
import com.example.coursework6sem.web.dto.external.CurrencyResponse;
import com.example.coursework6sem.web.dto.external.GeocodeResponse;
import com.example.coursework6sem.web.dto.external.WeatherResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    private final CurrencyExchangeService currencyExchangeService;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    public ExternalApiController(CurrencyExchangeService currencyExchangeService,
                                  GeocodingService geocodingService,
                                  WeatherService weatherService) {
        this.currencyExchangeService = currencyExchangeService;
        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
    }

    @GetMapping("/currency")
    public ResponseEntity<?> currency(@RequestParam String target) {
        try {
            CurrencyResponse rate = currencyExchangeService.getRate(target);
            return ResponseEntity.ok(Map.of("message", "Курс валют получен", "data", rate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/currency/supported")
    public ResponseEntity<?> supportedCurrencies() {
        try {
            return ResponseEntity.ok(Map.of("message", "Список валют получен", "data", currencyExchangeService.getSupportedTargets()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String address) {
        try {
            GeocodeResponse out = geocodingService.geocode(address);
            return ResponseEntity.ok(Map.of("message", "Геокодирование выполнено", "data", out));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/weather")
    public ResponseEntity<?> weather(@RequestParam double lat, @RequestParam double lon) {
        try {
            WeatherResponse out = weatherService.getCurrent(lat, lon);
            return ResponseEntity.ok(Map.of("message", "Погода получена", "data", out));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}

