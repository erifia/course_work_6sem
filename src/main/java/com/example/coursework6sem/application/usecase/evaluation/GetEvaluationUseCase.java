package com.example.coursework6sem.application.usecase.evaluation;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetEvaluationUseCase {

    private final EvaluationRepository evaluations;

    public GetEvaluationUseCase(EvaluationRepository evaluations) {
        this.evaluations = evaluations;
    }

    @Transactional(readOnly = true)
    public EvaluationResponse execute(long evaluationId) {
        EvaluationEntity evaluation = evaluations.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Оценка не найдена"));

        return new EvaluationResponse(
                evaluation.getId(),
                evaluation.getEstate().getId(),
                evaluation.getEstate().getAddress(),
                evaluation.getAppraiser().getId(),
                evaluation.getAppraiser().getUsername(),
                evaluation.getEstimatedValue(),
                evaluation.getEvaluationMethod(),
                evaluation.getNotes(),
                evaluation.getCreatedAt()
        );
    }
}

