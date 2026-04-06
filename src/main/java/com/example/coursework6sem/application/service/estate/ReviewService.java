package com.example.coursework6sem.application.service.estate;

import com.example.coursework6sem.application.usecase.review.CreateReviewUseCase;
import com.example.coursework6sem.application.usecase.review.ListReviewsUseCase;
import com.example.coursework6sem.web.dto.review.ReviewRequests;
import com.example.coursework6sem.web.dto.review.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final CreateReviewUseCase createDelegate;
    private final ListReviewsUseCase listDelegate;

    public ReviewService(CreateReviewUseCase createDelegate, ListReviewsUseCase listDelegate) {
        this.createDelegate = createDelegate;
        this.listDelegate = listDelegate;
    }

    public ReviewResponse create(long estateId, ReviewRequests.CreateRequest request) {
        return createDelegate.execute(estateId, request);
    }

    public Page<ReviewResponse> list(long estateId, Pageable pageable) {
        return listDelegate.execute(estateId, pageable);
    }
}

