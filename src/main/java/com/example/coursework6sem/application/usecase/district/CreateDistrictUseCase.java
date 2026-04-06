package com.example.coursework6sem.application.usecase.district;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.RoleRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.DistrictRequests;
import com.example.coursework6sem.web.dto.DistrictResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CreateDistrictUseCase {

    private final DistrictRepository districts;
    private final SecurityUtils securityUtils;
    private final RoleRepository roles;

    public CreateDistrictUseCase(DistrictRepository districts, SecurityUtils securityUtils, RoleRepository roles) {
        this.districts = districts;
        this.securityUtils = securityUtils;
        this.roles = roles;
    }

    public DistrictResponse execute(DistrictRequests request) {
        String role = securityUtils.currentRole()
                .orElseThrow(() -> new IllegalStateException("Требуется авторизация"));

        // Требования из старого проекта: ADMIN или APPRAISER
        boolean allowed = role.equalsIgnoreCase(RoleName.ADMIN.name()) || role.equalsIgnoreCase(RoleName.APPRAISER.name());
        if (!allowed) {
            throw new IllegalArgumentException("Недостаточно прав");
        }

        if (districts.findByNameIgnoreCase(request.districtName()).isPresent()) {
            throw new IllegalArgumentException("Район с таким именем уже существует");
        }

        DistrictEntity entity = districts.save(new DistrictEntity(
                request.districtName(),
                request.avgPrice(),
                request.demandLevel()
        ));

        return new DistrictResponse(entity.getId(), entity.getName(), entity.getAvgPrice(), entity.getDemandLevel());
    }
}

