package com.construction.material.repository;

import com.construction.material.entity.AuditLog;
import com.construction.material.entity.AuditLog.Action;
import com.construction.material.entity.AuditLog.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByAction(Action action);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    Page<AuditLog> findByProjectIdOrderByTimestampDesc(Long projectId, Pageable pageable);
}
