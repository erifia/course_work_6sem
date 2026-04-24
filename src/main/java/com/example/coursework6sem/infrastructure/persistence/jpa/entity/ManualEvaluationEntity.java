package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import com.example.coursework6sem.domain.ConditionCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "manual_evaluation")
public class ManualEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_evaluation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private DistrictEntity district;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appraiser_id", nullable = false)
    private UserAccountEntity appraiser;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "rooms", nullable = false)
    private Integer rooms;

    @Column(name = "area", precision = 10, scale = 2, nullable = false)
    private BigDecimal area;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "total_floors", nullable = false)
    private Integer totalFloors;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_code", nullable = false, length = 50)
    private ConditionCode conditionCode;

    @Column(name = "description")
    private String description;

    @Column(name = "estimated_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal estimatedValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ManualEvaluationEntity() {
    }

    public ManualEvaluationEntity(
            DistrictEntity district,
            UserAccountEntity appraiser,
            String address,
            Integer rooms,
            BigDecimal area,
            Integer floor,
            Integer totalFloors,
            ConditionCode conditionCode,
            String description,
            BigDecimal estimatedValue,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.district = district;
        this.appraiser = appraiser;
        this.address = address;
        this.rooms = rooms;
        this.area = area;
        this.floor = floor;
        this.totalFloors = totalFloors;
        this.conditionCode = conditionCode;
        this.description = description;
        this.estimatedValue = estimatedValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public DistrictEntity getDistrict() { return district; }
    public UserAccountEntity getAppraiser() { return appraiser; }
    public String getAddress() { return address; }
    public Integer getRooms() { return rooms; }
    public BigDecimal getArea() { return area; }
    public Integer getFloor() { return floor; }
    public Integer getTotalFloors() { return totalFloors; }
    public ConditionCode getConditionCode() { return conditionCode; }
    public String getDescription() { return description; }
    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public Instant getCreatedAt() { return createdAt; }
}
