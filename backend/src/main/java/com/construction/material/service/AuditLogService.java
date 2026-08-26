package com.construction.material.service;

import com.construction.material.dto.response.ProjectActivityResponse;
import com.construction.material.entity.AuditLog;
import com.construction.material.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    /** Records a key validation event (order approve/receive/cancel, project status change). */
    void record(AuditLog.EntityType entityType, Long entityId, AuditLog.Action action, Project project, String description);

    /** Key-validation activity journal for a single project, most recent first. */
    Page<ProjectActivityResponse> findProjectActivity(Long projectId, Pageable pageable);
}
