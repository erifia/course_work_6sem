package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recommendation", uniqueConstraints = {
        @UniqueConstraint(name = "uq_recommendation_user_estate", columnNames = {"user_account_id", "estate_id"})
})
public class RecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccountEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estate_id", nullable = false)
    private EstateEntity estate;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RecommendationEntity() {
    }

    public RecommendationEntity(UserAccountEntity user, EstateEntity estate, BigDecimal score, Instant createdAt) {
        this.user = user;
        this.estate = estate;
        this.score = score;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UserAccountEntity getUser() {
        return user;
    }

    public EstateEntity getEstate() {
        return estate;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

