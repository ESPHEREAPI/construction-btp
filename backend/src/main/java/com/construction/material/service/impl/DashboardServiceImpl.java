package com.construction.material.service.impl;

import com.construction.material.dto.response.DashboardStatsResponse;
import com.construction.material.entity.Project;
import com.construction.material.repository.*;
import com.construction.material.security.TenantContext;
import com.construction.material.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final MaterialRepository materialRepository;
    private final AlertRepository alertRepository;

    @Override
    public DashboardStatsResponse getStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        Long companyId = TenantContext.get();

        if (companyId != null) {
            stats.setTotalProjects(projectRepository.countByCompanyId(companyId));
            stats.setActiveProjects(projectRepository.countByCompanyIdAndStatus(companyId, Project.ProjectStatus.IN_PROGRESS));
            stats.setTotalMaterials(materialRepository.countByCompanyIsNullOrCompanyId(companyId));
        } else {
            stats.setTotalProjects(projectRepository.count());
            stats.setActiveProjects(projectRepository.countByStatus(Project.ProjectStatus.IN_PROGRESS));
            stats.setTotalMaterials(materialRepository.count());
        }

        stats.setAlertsCount((int) alertRepository.countByActiveTrueAndAcknowledgedFalse());

        return stats;
    }
}
