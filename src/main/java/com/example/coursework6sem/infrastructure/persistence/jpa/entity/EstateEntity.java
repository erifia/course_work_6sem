package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import com.example.coursework6sem.domain.ConditionCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "estate")
public class EstateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estate_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private DistrictEntity district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserAccountEntity createdBy;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "property_type", nullable = false, length = 50)
    private String propertyType;

    @Column(name = "rooms", nullable = false)
    private Integer rooms;

    @Column(name = "area", precision = 10, scale = 2, nullable = false)
    private BigDecimal area;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "total_floors", nullable = false)
    private Integer totalFloors;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_code", nullable = false, length = 50)
    private ConditionCode conditionCode;

    @Column(name = "description")
    private String description;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EstateEntity() {
    }

    public EstateEntity(
            DistrictEntity district,
            UserAccountEntity createdBy,
            String address,
            String propertyType,
            Integer rooms,
            BigDecimal area,
            BigDecimal price,
            Integer floor,
            Integer totalFloors,
            ConditionCode conditionCode,
            String description,
            String imagePath,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.district = district;
        this.createdBy = createdBy;
        this.address = address;
        this.propertyType = propertyType;
        this.rooms = rooms;
        this.area = area;
        this.price = price;
        this.floor = floor;
        this.totalFloors = totalFloors;
        this.conditionCode = conditionCode;
        this.description = description;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public DistrictEntity getDistrict() {
        return district;
    }

    public String getAddress() {
        return address;
    }

    public Integer getRooms() {
        return rooms;
    }

    public BigDecimal getArea() {
        return area;
    }

    public Integer getFloor() {
        return floor;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public ConditionCode getConditionCode() {
        return conditionCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UserAccountEntity getCreatedBy() {
        return createdBy;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public void setConditionCode(ConditionCode conditionCode) {
        this.conditionCode = conditionCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setDistrict(DistrictEntity district) {
        this.district = district;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

