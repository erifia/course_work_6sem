package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "evaluation")
public class EvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estate_id", nullable = false)
    private EstateEntity estate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appraiser_id", nullable = false)
    private UserAccountEntity appraiser;

    @Column(name = "estimated_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal estimatedValue;

    @Column(name = "evaluation_method", length = 100)
    private String evaluationMethod;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EvaluationEntity() {
    }

    public EvaluationEntity(EstateEntity estate,
                              UserAccountEntity appraiser,
                              BigDecimal estimatedValue,
                              String evaluationMethod,
                              String notes,
                              Instant createdAt,
                              Instant updatedAt) {
        this.estate = estate;
        this.appraiser = appraiser;
        this.estimatedValue = estimatedValue;
        this.evaluationMethod = evaluationMethod;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public EstateEntity getEstate() {
        return estate;
    }

    public UserAccountEntity getAppraiser() {
        return appraiser;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public String getEvaluationMethod() {
        return evaluationMethod;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public void setEvaluationMethod(String evaluationMethod) {
        this.evaluationMethod = evaluationMethod;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

