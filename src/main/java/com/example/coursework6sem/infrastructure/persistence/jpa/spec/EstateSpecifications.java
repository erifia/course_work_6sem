package com.example.coursework6sem.infrastructure.persistence.jpa.spec;

import com.example.coursework6sem.domain.ConditionCode;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.EstateEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collection;

public final class EstateSpecifications {
    private EstateSpecifications() {
    }

    public static Specification<EstateEntity> districtIdIs(Long districtId) {
        return (root, query, cb) -> districtId == null ? cb.conjunction() : cb.equal(root.get("district").get("id"), districtId);
    }

    public static Specification<EstateEntity> districtIdsIn(Collection<Long> districtIds) {
        return (root, query, cb) -> {
            if (districtIds == null || districtIds.isEmpty()) return cb.conjunction();
            return root.get("district").get("id").in(districtIds);
        };
    }

    public static Specification<EstateEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("price"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }

    public static Specification<EstateEntity> areaBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("area"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("area"), min);
            return cb.lessThanOrEqualTo(root.get("area"), max);
        };
    }

    public static Specification<EstateEntity> roomsBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("rooms"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("rooms"), min);
            return cb.lessThanOrEqualTo(root.get("rooms"), max);
        };
    }

    public static Specification<EstateEntity> conditionIs(ConditionCode code) {
        return (root, query, cb) -> code == null ? cb.conjunction() : cb.equal(root.get("conditionCode"), code);
    }

    public static Specification<EstateEntity> floorBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("floor"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("floor"), min);
            return cb.lessThanOrEqualTo(root.get("floor"), max);
        };
    }

    public static Specification<EstateEntity> excludeManualDrafts() {
        return (root, query, cb) -> cb.notEqual(root.get("propertyType"), "MANUAL_DRAFT");
    }
}

