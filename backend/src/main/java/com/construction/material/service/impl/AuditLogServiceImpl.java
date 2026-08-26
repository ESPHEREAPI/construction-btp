package com.construction.material.service.impl;

import com.construction.material.dto.response.ProjectActivityResponse;
import com.construction.material.entity.AuditLog;
import com.construction.material.entity.Project;
import com.construction.material.entity.User;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.AuditLogRepository;
import com.construction.material.repository.ProjectRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.ProjectContext;
import com.construction.material.security.TenantContext;
import com.construction.material.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void record(AuditLog.EntityType entityType, Long entityId, AuditLog.Action action, Project project, String description) {
        User performer = currentUser();
        auditLogRepository.save(AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .project(project)
                .user(performer)
                .username(performer != null ? performer.getUsername() : null)
                .description(description)
                .moduleName(entityType.name())
                .success(true)
                .build());
    }

    @Override
    public Page<ProjectActivityResponse> findProjectActivity(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .filter(this::belongsToCurrentTenant)
                .orElseThrow(() -> new ResourceNotFoundException(msg("project.not.found")));
        return auditLogRepository.findByProjectIdOrderByTimestampDesc(project.getId(), pageable)
                .map(this::toResponse);
    }

    /** Same access rule as OrderServiceImpl/ProjectServiceImpl: same company, and if the caller is project-scoped, the same project. */
    private boolean belongsToCurrentTenant(Project project) {
        Long companyId = TenantContext.get();
        boolean sameCompany = companyId == null || (project.getCompany() != null && companyId.equals(project.getCompany().getId()));
        if (!sameCompany) {
            return false;
        }
        Long projectId = ProjectContext.get();
        return projectId == null || projectId.equals(project.getId());
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private ProjectActivityResponse toResponse(AuditLog log) {
        return ProjectActivityResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType().name())
                .action(log.getAction().name())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .performedBy(log.getUser() != null ? log.getUser().getFullName() : log.getUsername())
                .timestamp(log.getTimestamp())
                .build();
    }
}
