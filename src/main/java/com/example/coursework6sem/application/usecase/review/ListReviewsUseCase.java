package com.example.coursework6sem.application.usecase.review;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ReviewEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.ReviewRepository;
import com.example.coursework6sem.web.dto.review.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListReviewsUseCase {

    private final ReviewRepository reviews;

    public ListReviewsUseCase(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> execute(long estateId, Pageable pageable) {
        return reviews.findByEstate_IdOrderByCreatedAtDesc(estateId, pageable)
                .map(r -> new ReviewResponse(
                        r.getId(),
                        r.getEstate().getId(),
                        r.getUser().getId(),
                        r.getUser().getUsername(),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt()
                ));
    }
}

