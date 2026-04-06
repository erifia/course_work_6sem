package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.recommendation.PreferenceService;
import com.example.coursework6sem.application.service.recommendation.RecommendationGenerationService;
import com.example.coursework6sem.application.service.recommendation.RecommendationQueryService;
import com.example.coursework6sem.web.dto.recommendation.RecommendationResponse;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceRequests;
import com.example.coursework6sem.web.dto.recommendation.UserPreferenceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final PreferenceService preferenceService;
    private final RecommendationGenerationService generationService;
    private final RecommendationQueryService queryService;

    public RecommendationController(PreferenceService preferenceService,
                                    RecommendationGenerationService generationService,
                                    RecommendationQueryService queryService) {
        this.preferenceService = preferenceService;
        this.generationService = generationService;
        this.queryService = queryService;
    }

    @GetMapping("/preferences")
    public ResponseEntity<?> preference() {
        try {
            UserPreferenceResponse pref = preferenceService.get();
            return ResponseEntity.ok(Map.of("message", "Предпочтения", "data", pref));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> savePreference(@RequestBody @Valid UserPreferenceRequests request) {
        try {
            UserPreferenceResponse pref = preferenceService.save(request);
            return ResponseEntity.ok(Map.of("message", "Предпочтения сохранены", "data", pref));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(
            @RequestParam(required = false) String districtIds,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Set<Long> districts = parseIds(districtIds);
            List<RecommendationResponse> recs = generationService.generate(districts, limit);
            return ResponseEntity.ok(Map.of("message", "Рекомендации сгенерированы", "data", recs));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(Map.of("message", "Рекомендации", "data", queryService.list(limit)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private Set<Long> parseIds(String districtIds) {
        if (districtIds == null || districtIds.isBlank()) return Collections.emptySet();
        return Arrays.stream(districtIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
    }
}

