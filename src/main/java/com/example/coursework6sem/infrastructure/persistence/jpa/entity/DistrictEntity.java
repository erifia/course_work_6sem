package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "district")
public class DistrictEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long id;

    @Column(name = "district_name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "avg_price", precision = 15, scale = 2)
    private BigDecimal avgPrice;

    @Column(name = "demand_level")
    private Integer demandLevel;

    protected DistrictEntity() {
    }

    public DistrictEntity(String name, BigDecimal avgPrice, Integer demandLevel) {
        this.name = name;
        this.avgPrice = avgPrice;
        this.demandLevel = demandLevel;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public Integer getDemandLevel() {
        return demandLevel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public void setDemandLevel(Integer demandLevel) {
        this.demandLevel = demandLevel;
    }
}

