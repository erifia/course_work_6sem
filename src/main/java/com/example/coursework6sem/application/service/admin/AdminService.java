package com.example.coursework6sem.application.service.admin;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.RoleEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RoleRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.web.dto.admin.AdminStatsResponse;
import com.example.coursework6sem.web.dto.admin.UserSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final EstateRepository estates;
    private final EvaluationRepository evaluations;

    public AdminService(
            UserAccountRepository users,
            RoleRepository roles,
            EstateRepository estates,
            EvaluationRepository evaluations
    ) {
        this.users = users;
        this.roles = roles;
        this.estates = estates;
        this.evaluations = evaluations;
    }

    public List<UserSummaryResponse> getAllUsers() {
        return users.findAll().stream()
                .map(e -> new UserSummaryResponse(
                        e.getId(),
                        e.getUsername(),
                        e.getEmail(),
                        e.getRoleName()
                ))
                .toList();
    }

    @Transactional
    public void changeUserRole(Long userId, RoleName roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Роль не может быть пустой");
        }
        RoleEntity role = roles.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));

        var user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        user.setRole(role);
    }

    public AdminStatsResponse getStats() {
        long usersCount = users.count();
        long estatesCount = estates.count();
        long evaluationsCount = evaluations.count();
        return new AdminStatsResponse(usersCount, estatesCount, evaluationsCount);
    }
}

