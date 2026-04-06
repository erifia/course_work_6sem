package com.example.coursework6sem.infrastructure.persistence.jpa.spec;

import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EvaluationEntity;
import org.springframework.data.jpa.domain.Specification;

public final class EvaluationSpecifications {
    private EvaluationSpecifications() {
    }

    public static Specification<EvaluationEntity> estateIdIs(Long estateId) {
        return (root, query, cb) -> estateId == null ? cb.conjunction() : cb.equal(root.get("estate").get("id"), estateId);
    }

    public static Specification<EvaluationEntity> appraiserIdIs(Long appraiserId) {
        return (root, query, cb) -> appraiserId == null ? cb.conjunction() : cb.equal(root.get("appraiser").get("id"), appraiserId);
    }
}

