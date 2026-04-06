package com.example.coursework6sem.infrastructure.persistence.jpa.entity;

import com.example.coursework6sem.domain.RoleName;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_account")
public class UserAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_account_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserAccountEntity() {
    }

    public UserAccountEntity(String username, String passwordHash, String email, RoleEntity role, Instant createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public RoleName getRoleName() {
        return role.getName();
    }

    public RoleEntity getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }
}

