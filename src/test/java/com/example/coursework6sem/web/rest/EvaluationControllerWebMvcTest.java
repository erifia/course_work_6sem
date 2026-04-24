package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.evaluation.EvaluationCommandService;
import com.example.coursework6sem.application.service.evaluation.EvaluationQueryService;
import com.example.coursework6sem.application.service.evaluation.ManualEvaluationService;
import com.example.coursework6sem.security.JwtAuthenticationFilter;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import com.example.coursework6sem.web.dto.evaluation.ManualEvaluationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(EvaluationController.class)
@AutoConfigureMockMvc(addFilters = false)
class EvaluationControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EvaluationCommandService commandService;
    @MockBean
    EvaluationQueryService queryService;
    @MockBean
    ManualEvaluationService manualEvaluationService;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void create_returns201OnSuccess() throws Exception {
        when(commandService.create(any())).thenReturn(mock(EvaluationResponse.class));

        var body = new EvaluationRequests.CreateRequest(1L, new BigDecimal("123"), "ok", null);

        mockMvc.perform(post("/api/evaluations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Оценка создана"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void create_returns401OnIllegalStateException() throws Exception {
        when(commandService.create(any())).thenThrow(new IllegalStateException("Требуется авторизация"));

        var body = new EvaluationRequests.CreateRequest(1L, new BigDecimal("123"), "ok", null);

        mockMvc.perform(post("/api/evaluations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Требуется авторизация"));
    }

    @Test
    void create_returns403OnIllegalArgumentException() throws Exception {
        when(commandService.create(any())).thenThrow(new IllegalArgumentException("Недостаточно прав"));

        var body = new EvaluationRequests.CreateRequest(1L, new BigDecimal("123"), "ok", null);

        mockMvc.perform(post("/api/evaluations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав"));
    }

    @Test
    void manual_returns201OnSuccess() throws Exception {
        when(manualEvaluationService.evaluateAndSave(any())).thenReturn(new ManualEvaluationResponse(
                1L, 2L, "Центр", "ул. Ленина, 1", 2, new BigDecimal("45.5"),
                3, 9, "хорошее", "описание",
                10L, "me", new BigDecimal("123456.78"), Instant.parse("2025-01-01T00:00:00Z")
        ));

        var body = new EvaluationRequests.ManualCreateRequest(
                2L, "ул. Ленина, 1", 2, new BigDecimal("45.5"), 3, 9, "хорошее", "описание"
        );

        mockMvc.perform(post("/api/evaluations/manual")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Оценка рассчитана и сохранена"))
                .andExpect(jsonPath("$.data.estimatedValue").value(123456.78));
    }

    @Test
    void manual_returns400OnIllegalArgumentException() throws Exception {
        when(manualEvaluationService.evaluateAndSave(any())).thenThrow(new IllegalArgumentException("Некорректное состояние"));

        var body = new EvaluationRequests.ManualCreateRequest(
                2L, "ул. Ленина, 1", 2, new BigDecimal("45.5"), 3, 9, "НЕИЗВЕСТНО", "описание"
        );

        mockMvc.perform(post("/api/evaluations/manual")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректное состояние"));
    }

    private static <T> T mock(Class<T> type) {
        return org.mockito.Mockito.mock(type);
    }
}

