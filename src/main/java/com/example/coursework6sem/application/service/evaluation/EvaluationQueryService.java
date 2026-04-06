package com.example.coursework6sem.application.service.evaluation;

import com.example.coursework6sem.application.usecase.evaluation.GetEvaluationUseCase;
import com.example.coursework6sem.application.usecase.evaluation.ListEvaluationsUseCase;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EvaluationQueryService {

    private final ListEvaluationsUseCase listDelegate;
    private final GetEvaluationUseCase getDelegate;

    public EvaluationQueryService(ListEvaluationsUseCase listDelegate, GetEvaluationUseCase getDelegate) {
        this.listDelegate = listDelegate;
        this.getDelegate = getDelegate;
    }

    public Page<EvaluationResponse> list(Long appraiserId, Long estateId, Pageable pageable) {
        return listDelegate.execute(appraiserId, estateId, pageable);
    }

    public EvaluationResponse get(long evaluationId) {
        return getDelegate.execute(evaluationId);
    }
}

