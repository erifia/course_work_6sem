package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.estate.*;
import com.example.coursework6sem.web.dto.estate.EstateRequests;
import com.example.coursework6sem.web.dto.estate.EstateResponse;
import com.example.coursework6sem.web.dto.review.ReviewRequests;
import com.example.coursework6sem.web.dto.review.ReviewResponse;
import com.example.coursework6sem.web.dto.valuation.EstateComparisonResponse;
import com.example.coursework6sem.web.dto.valuation.EstateValuationResponse;
import com.example.coursework6sem.web.dto.valuation.EstatePredictionResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/real-estate")
public class EstateController {

    private final EstateQueryService estateQueryService;
    private final EstateCommandService estateCommandService;
    private final EstateValuationService valuationService;
    private final EstateComparisonService comparisonService;
    private final EstatePredictionService predictionService;
    private final ReviewService reviewService;

    public EstateController(EstateQueryService estateQueryService,
                             EstateCommandService estateCommandService,
                             EstateValuationService valuationService,
                             EstateComparisonService comparisonService,
                             EstatePredictionService predictionService,
                             ReviewService reviewService) {
        this.estateQueryService = estateQueryService;
        this.estateCommandService = estateCommandService;
        this.valuationService = valuationService;
        this.comparisonService = comparisonService;
        this.predictionService = predictionService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<Page<EstateResponse>> search(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea,
            @RequestParam(required = false) Integer minRooms,
            @RequestParam(required = false) Integer maxRooms,
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), mapSortField(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(estateQueryService.search(
                pageable,
                districtId,
                minPrice,
                maxPrice,
                minArea,
                maxArea,
                minRooms,
                maxRooms,
                condition
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok(estateQueryService.get(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid EstateRequests.CreateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Объект создан", "data", estateCommandService.create(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody @Valid EstateRequests.UpdateRequest request) {
        try {
            return ResponseEntity.ok(Map.of("message", "Объект обновлен", "data", estateCommandService.update(id, request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        try {
            estateCommandService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Объект удалён"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/calculate")
    public ResponseEntity<?> calculate(
            @PathVariable("id") long id,
            @RequestParam(defaultValue = "AUTO") String method
    ) {
        try {
            EstateValuationResponse response = valuationService.calculate(id, method);
            return ResponseEntity.ok(Map.of("message", "Стоимость рассчитана", "data", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/compare")
    public ResponseEntity<?> compare(
            @RequestParam("id1") long id1,
            @RequestParam("id2") long id2
    ) {
        try {
            EstateComparisonResponse response = comparisonService.execute(id1, id2);
            return ResponseEntity.ok(Map.of("message", "Сравнение выполнено", "data", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/predict")
    public ResponseEntity<?> predict(
            @PathVariable("id") long id,
            @RequestParam(defaultValue = "12") int months
    ) {
        try {
            EstatePredictionResponse response = predictionService.predict(id, months);
            return ResponseEntity.ok(Map.of("message", "Прогноз рассчитан", "data", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Reviews
    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> listReviews(
            @PathVariable("id") long id,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.list(id, pageable));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable("id") long id,
            @RequestBody @Valid ReviewRequests.CreateRequest request
    ) {
        try {
            ReviewResponse created = reviewService.create(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Отзыв создан", "data", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "price" -> "price";
            case "area" -> "area";
            case "rooms" -> "rooms";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };
    }
}

