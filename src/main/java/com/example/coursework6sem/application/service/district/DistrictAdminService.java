package com.example.coursework6sem.application.service.district;

import com.example.coursework6sem.application.usecase.district.CreateDistrictUseCase;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.DistrictEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.DistrictRepository;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.DistrictRequests;
import com.example.coursework6sem.web.dto.DistrictResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistrictAdminService {

    private final CreateDistrictUseCase delegate;
    private final DistrictRepository districts;
    private final SecurityUtils securityUtils;

    public DistrictAdminService(CreateDistrictUseCase delegate, DistrictRepository districts, SecurityUtils securityUtils) {
        this.delegate = delegate;
        this.districts = districts;
        this.securityUtils = securityUtils;
    }

    public DistrictResponse create(DistrictRequests request) {
        return delegate.execute(request);
    }

    @Transactional
    public DistrictResponse update(Long districtId, DistrictRequests request) {
        String role = securityUtils.currentRole()
                .orElseThrow(() -> new IllegalStateException("Требуется авторизация"));
        boolean allowed = role.equalsIgnoreCase(RoleName.ADMIN.name()) || role.equalsIgnoreCase(RoleName.APPRAISER.name());
        if (!allowed) {
            throw new IllegalArgumentException("Недостаточно прав");
        }
        DistrictEntity district = districts.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        districts.findByNameIgnoreCase(request.districtName())
                .filter(existing -> !existing.getId().equals(districtId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Район с таким именем уже существует");
                });

        district.setName(request.districtName());
        district.setAvgPrice(request.avgPrice());
        district.setDemandLevel(request.demandLevel());
        DistrictEntity saved = districts.save(district);
        return new DistrictResponse(saved.getId(), saved.getName(), saved.getAvgPrice(), saved.getDemandLevel());
    }
}

