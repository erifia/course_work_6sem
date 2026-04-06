package com.example.coursework6sem.application.usecase.review;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.ReviewEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.ReviewRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.review.ReviewRequests;
import com.example.coursework6sem.web.dto.review.ReviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateReviewUseCase {

    private final ReviewRepository reviews;
    private final EstateRepository estates;
    private final UserAccountRepository users;
    private final SecurityUtils securityUtils;

    public CreateReviewUseCase(ReviewRepository reviews, EstateRepository estates, UserAccountRepository users, SecurityUtils securityUtils) {
        this.reviews = reviews;
        this.estates = estates;
        this.users = users;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public ReviewResponse execute(long estateId, ReviewRequests.CreateRequest request) {
        Long userId = securityUtils.currentUserAccountId().orElseThrow(() -> new IllegalStateException("Требуется авторизация"));

        EstateEntity estate = estates.findById(estateId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));

        UserAccountEntity user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Instant now = Instant.now();

        ReviewEntity created = reviews.save(new ReviewEntity(
                estate,
                user,
                request.rating(),
                request.comment(),
                now,
                now
        ));

        return new ReviewResponse(
                created.getId(),
                created.getEstate().getId(),
                user.getId(),
                user.getUsername(),
                created.getRating(),
                created.getComment(),
                created.getCreatedAt()
        );
    }
}

