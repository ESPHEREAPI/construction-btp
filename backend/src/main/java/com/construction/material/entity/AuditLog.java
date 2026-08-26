package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing audit log entries for tracking system actions
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    /** Denormalized project reference so a project's activity journal is a single query. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String username;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(length = 1000)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 100)
    private String moduleName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(length = 1000)
    private String errorMessage;

    public enum EntityType {
        USER,
        ROLE,
        PERMISSION,
        PROJECT,
        MATERIAL,
        QUANTIFICATION,
        ORDER,
        ORDER_ITEM,
        USAGE,
        STOCK,
        STOCK_MOVEMENT,
        ALERT,
        SETTINGS
    }

    public enum Action {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        EXPORT,
        IMPORT,
        APPROVE,
        REJECT,
        SUBMIT,
        CANCEL,
        RECEIVE
    }
}
