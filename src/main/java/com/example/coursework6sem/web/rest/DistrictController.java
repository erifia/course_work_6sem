package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.district.DistrictAdminService;
import com.example.coursework6sem.application.service.district.DistrictQueryService;
import com.example.coursework6sem.web.dto.DistrictRequests;
import com.example.coursework6sem.web.dto.DistrictResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/districts")
public class DistrictController {

    private final DistrictQueryService districtQueryService;
    private final DistrictAdminService districtAdminService;

    public DistrictController(DistrictQueryService districtQueryService, DistrictAdminService districtAdminService) {
        this.districtQueryService = districtQueryService;
        this.districtAdminService = districtAdminService;
    }

    @GetMapping
    public List<DistrictResponse> list() {
        return districtQueryService.list();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid DistrictRequests request) {
        try {
            DistrictResponse created = districtAdminService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Район успешно создан", "data", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
}

