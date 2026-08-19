package com.construction.material.service.impl;

import com.construction.material.dto.request.ProjectRequest;
import com.construction.material.dto.response.ProjectResponse;
import com.construction.material.entity.Company;
import com.construction.material.entity.License;
import com.construction.material.entity.Project;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.CompanyRepository;
import com.construction.material.repository.LicenseRepository;
import com.construction.material.repository.ProjectRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.ProjectContext;
import com.construction.material.security.TenantContext;
import com.construction.material.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Project code already exists");
        }

        Long companyId = TenantContext.get();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("company.not.found", null, LocaleContextHolder.getLocale())));

        licenseRepository.findFirstByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, License.LicenseStatus.ACTIVE).ifPresent(license -> {
            long currentProjectCount = projectRepository.countByCompanyId(companyId);
            if (currentProjectCount >= license.getMaxProjects()) {
                throw new BusinessException(messageSource.getMessage("license.limit.projects.exceeded", null, LocaleContextHolder.getLocale()));
            }
        });

        Project project = Project.builder()
                .company(company)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .client(request.getClient())
                .status(request.getStatus() != null ? Project.ProjectStatus.valueOf(request.getStatus()) : Project.ProjectStatus.PLANNED)
                .startDate(request.getStartDate())
                .estimatedEndDate(request.getEstimatedEndDate())
                .budget(request.getBudget())
                .build();

        if (request.getProjectManagerId() != null) {
            User pm = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project Manager not found"));
            project.setProjectManager(pm);
        }

        if (request.getSiteManagerId() != null) {
            User sm = userRepository.findById(request.getSiteManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site Manager not found"));
            project.setSiteManager(sm);
        }

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .filter(this::belongsToCurrentTenant)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("project.not.found", null, LocaleContextHolder.getLocale())));

        project.setCode(request.getCode());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLocation(request.getLocation());
        project.setClient(request.getClient());
        if (request.getStatus() != null) {
            project.setStatus(Project.ProjectStatus.valueOf(request.getStatus()));
        }
        project.setStartDate(request.getStartDate());
        project.setEstimatedEndDate(request.getEstimatedEndDate());
        project.setBudget(request.getBudget());

        Project updated = projectRepository.save(project);
        return toResponse(updated);
    }

    @Override
    public ProjectResponse findById(Long id) {
        Project project = projectRepository.findById(id)
                .filter(this::belongsToCurrentTenant)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("project.not.found", null, LocaleContextHolder.getLocale())));
        return toResponse(project);
    }

    @Override
    public List<ProjectResponse> findAll() {
        Long companyId = TenantContext.get();
        List<Project> projects = companyId != null ? projectRepository.findByCompanyId(companyId) : projectRepository.findAll();
        Long projectId = ProjectContext.get();
        if (projectId != null) {
            projects = projects.stream().filter(p -> projectId.equals(p.getId())).collect(Collectors.toList());
        }
        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProjectResponse> findAllPaginated(Pageable pageable) {
        Long companyId = TenantContext.get();
        Page<Project> projects = companyId != null ? projectRepository.findByCompanyId(companyId, pageable) : projectRepository.findAll(pageable);
        return projects.map(this::toResponse);
    }

    /**
     * Super Admin (no company) can see any project; a company user only sees its own
     * company's projects; a project-scoped user (Chef de Projet/Chantier, Gestionnaire
     * d'Inventaire, Employé) only sees the single project they're assigned to.
     */
    private boolean belongsToCurrentTenant(Project project) {
        Long companyId = TenantContext.get();
        boolean sameCompany = companyId == null || (project.getCompany() != null && companyId.equals(project.getCompany().getId()));
        if (!sameCompany) {
            return false;
        }
        Long projectId = ProjectContext.get();
        return projectId == null || projectId.equals(project.getId());
    }

    @Override
    public void delete(Long id) {
        Project project = projectRepository.findById(id)
                .filter(this::belongsToCurrentTenant)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("project.not.found", null, LocaleContextHolder.getLocale())));
        projectRepository.delete(project);
    }

    @Override
    public List<ProjectResponse> findByStatus(String status) {
        Long companyId = TenantContext.get();
        Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status);
        List<Project> projects = companyId != null
                ? projectRepository.findByCompanyIdAndStatus(companyId, projectStatus)
                : projectRepository.findByStatus(projectStatus);
        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .code(project.getCode())
                .name(project.getName())
                .description(project.getDescription())
                .location(project.getLocation())
                .client(project.getClient())
                .status(project.getStatus().name())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .estimatedEndDate(project.getEstimatedEndDate())
                .budget(canSeeBudget() ? project.getBudget() : null)
                .projectManagerName(project.getProjectManager() != null ? project.getProjectManager().getFullName() : null)
                .siteManagerName(project.getSiteManager() != null ? project.getSiteManager().getFullName() : null)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private static final Set<String> BUDGET_VISIBLE_AUTHORITIES = Set.of(
            "ROLE_SUPER_ADMIN", "ROLE_COMPANY_ADMIN", "ROLE_ADMIN");

    /** Project budget is only ever shown to platform/company administrators. */
    private boolean canSeeBudget() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(BUDGET_VISIBLE_AUTHORITIES::contains);
    }
}
