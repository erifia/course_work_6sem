package com.example.coursework6sem.application.usecase.evaluation;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.spec.EvaluationSpecifications;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListEvaluationsUseCase {

    private final EvaluationRepository evaluations;

    public ListEvaluationsUseCase(EvaluationRepository evaluations) {
        this.evaluations = evaluations;
    }

    @Transactional(readOnly = true)
    public Page<EvaluationResponse> execute(Long appraiserId, Long estateId, Pageable pageable) {
        Specification<EvaluationEntity> spec = Specification
                .where(EvaluationSpecifications.appraiserIdIs(appraiserId))
                .and(EvaluationSpecifications.estateIdIs(estateId));

        return evaluations.findAll(spec, pageable).map(e -> new EvaluationResponse(
                e.getId(),
                e.getEstate().getId(),
                e.getEstate().getAddress(),
                e.getAppraiser().getId(),
                e.getAppraiser().getUsername(),
                e.getEstimatedValue(),
                e.getEvaluationMethod(),
                e.getNotes(),
                e.getCreatedAt()
        ));
    }
}

