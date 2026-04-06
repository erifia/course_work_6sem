package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import com.example.coursework6sem.domain.ConditionCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_preference", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_preference_user", columnNames = {"user_account_id"})
})
public class UserPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccountEntity user;

    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "min_area", precision = 10, scale = 2)
    private BigDecimal minArea;

    @Column(name = "max_area", precision = 10, scale = 2)
    private BigDecimal maxArea;

    @Column(name = "min_rooms")
    private Integer minRooms;

    @Column(name = "max_rooms")
    private Integer maxRooms;

    @Column(name = "min_floor")
    private Integer minFloor;

    @Column(name = "max_floor")
    private Integer maxFloor;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_code", length = 50)
    private ConditionCode conditionCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserPreferenceEntity() {
    }

    public UserPreferenceEntity(UserAccountEntity user,
                                  BigDecimal minPrice,
                                  BigDecimal maxPrice,
                                  BigDecimal minArea,
                                  BigDecimal maxArea,
                                  Integer minRooms,
                                  Integer maxRooms,
                                  Integer minFloor,
                                  Integer maxFloor,
                                  ConditionCode conditionCode,
                                  Instant createdAt) {
        this.user = user;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minArea = minArea;
        this.maxArea = maxArea;
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.conditionCode = conditionCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UserAccountEntity getUser() {
        return user;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public BigDecimal getMinArea() {
        return minArea;
    }

    public BigDecimal getMaxArea() {
        return maxArea;
    }

    public Integer getMinRooms() {
        return minRooms;
    }

    public Integer getMaxRooms() {
        return maxRooms;
    }

    public ConditionCode getConditionCode() {
        return conditionCode;
    }

    public Integer getMinFloor() {
        return minFloor;
    }

    public Integer getMaxFloor() {
        return maxFloor;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setMinArea(BigDecimal minArea) {
        this.minArea = minArea;
    }

    public void setMaxArea(BigDecimal maxArea) {
        this.maxArea = maxArea;
    }

    public void setMinRooms(Integer minRooms) {
        this.minRooms = minRooms;
    }

    public void setMaxRooms(Integer maxRooms) {
        this.maxRooms = maxRooms;
    }

    public void setMinFloor(Integer minFloor) {
        this.minFloor = minFloor;
    }

    public void setMaxFloor(Integer maxFloor) {
        this.maxFloor = maxFloor;
    }

    public void setConditionCode(ConditionCode conditionCode) {
        this.conditionCode = conditionCode;
    }
}

