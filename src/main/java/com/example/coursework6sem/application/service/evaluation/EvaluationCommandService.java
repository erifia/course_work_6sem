package com.example.coursework6sem.application.service.evaluation;

import com.example.coursework6sem.application.usecase.evaluation.*;
import com.example.coursework6sem.web.dto.evaluation.EvaluationRequests;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import org.springframework.stereotype.Service;

@Service
public class EvaluationCommandService {

    private final CreateEvaluationUseCase createDelegate;
    private final UpdateEvaluationUseCase updateDelegate;
    private final DeleteEvaluationUseCase deleteDelegate;

    public EvaluationCommandService(CreateEvaluationUseCase createDelegate,
                                     UpdateEvaluationUseCase updateDelegate,
                                     DeleteEvaluationUseCase deleteDelegate) {
        this.createDelegate = createDelegate;
        this.updateDelegate = updateDelegate;
        this.deleteDelegate = deleteDelegate;
    }

    public EvaluationResponse create(EvaluationRequests.CreateRequest request) {
        return createDelegate.execute(request);
    }

    public EvaluationResponse update(long evaluationId, EvaluationRequests.UpdateRequest request) {
        return updateDelegate.execute(evaluationId, request);
    }

    public void delete(long evaluationId) {
        deleteDelegate.execute(evaluationId);
    }
}

