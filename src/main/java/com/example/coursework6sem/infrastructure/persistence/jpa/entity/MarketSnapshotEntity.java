package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "market_snapshot", uniqueConstraints = {
        @UniqueConstraint(name = "uq_market_snapshot_district_month_type", columnNames = {"district_id", "snapshot_month", "property_type"})
})
public class MarketSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private DistrictEntity district;

    @Column(name = "snapshot_month", nullable = false)
    private LocalDate snapshotMonth;

    @Column(name = "avg_price", precision = 15, scale = 2)
    private BigDecimal avgPrice;

    @Column(name = "demand_level")
    private Integer demandLevel;

    @Column(name = "property_type", nullable = false, length = 50)
    private String propertyType;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    protected MarketSnapshotEntity() {
    }

    public MarketSnapshotEntity(DistrictEntity district,
                                  LocalDate snapshotMonth,
                                  BigDecimal avgPrice,
                                  Integer demandLevel,
                                  String propertyType,
                                  Instant capturedAt) {
        this.district = district;
        this.snapshotMonth = snapshotMonth;
        this.avgPrice = avgPrice;
        this.demandLevel = demandLevel;
        this.propertyType = propertyType;
        this.capturedAt = capturedAt;
    }

    public Long getId() {
        return id;
    }

    public DistrictEntity getDistrict() {
        return district;
    }

    public LocalDate getSnapshotMonth() {
        return snapshotMonth;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public Integer getDemandLevel() {
        return demandLevel;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }
}

