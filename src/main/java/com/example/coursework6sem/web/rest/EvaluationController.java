package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.evaluation.EvaluationCommandService;
import com.example.coursework6sem.application.service.evaluation.ManualEvaluationService;
import com.example.coursework6sem.application.service.evaluation.EvaluationQueryService;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationCommandService commandService;
    private final EvaluationQueryService queryService;
    private final ManualEvaluationService manualEvaluationService;

    public EvaluationController(
            EvaluationCommandService commandService,
            EvaluationQueryService queryService,
            ManualEvaluationService manualEvaluationService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.manualEvaluationService = manualEvaluationService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid EvaluationRequests.CreateRequest request) {
        try {
            EvaluationResponse created = commandService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Оценка создана", "data", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<EvaluationResponse>> list(
            @RequestParam(required = false) Long appraiserId,
            @RequestParam(required = false) Long estateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(queryService.list(appraiserId, estateId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok(queryService.get(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manual(@RequestBody @Valid EvaluationRequests.ManualCreateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Оценка сохранена",
                    "data", manualEvaluationService.evaluateAndSave(request)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody @Valid EvaluationRequests.UpdateRequest request) {
        try {
            return ResponseEntity.ok(Map.of("message", "Оценка обновлена", "data", commandService.update(id, request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        try {
            commandService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Оценка удалена"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
}

