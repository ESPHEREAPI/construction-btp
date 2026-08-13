package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A company's license grant. Each generate/renew/trial issuance creates a NEW
 * row (history), never mutates an old one in place except for the explicit
 * status transitions (suspend/reactivate/revoke/activate). The "current" row
 * is always resolved by query (see LicenseRepository), never by a boolean flag.
 * maxUsers/maxProjects/activeModules are snapshotted at issuance time, so later
 * changes to a LicensePlan do not retroactively affect already-issued licenses.
 */
@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LicensePlan.LicenseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LicenseStatus status = LicenseStatus.GENERATED;

    /** Null until activated (set when a GENERATED license is activated, or immediately for a trial). */
    private LocalDate startDate;

    /** Null = never expires by date */
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer maxUsers;

    @Column(nullable = false)
    private Integer maxProjects;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "license_modules", joinColumns = @JoinColumn(name = "license_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "module")
    @Builder.Default
    private Set<LicenseModule> activeModules = new HashSet<>();

    /** The signed activation key generated for this license (null for trial-issued licenses, which activate immediately). */
    @Column(name = "license_key", length = 2000)
    private String licenseKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean trial = false;

    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String revokedBy;

    private LocalDateTime revokedAt;

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /** Used for admin display only; enforcement uses LicenseService.resolveEnforcementStatus(). */
    public boolean isBlocked() {
        return status == LicenseStatus.SUSPENDED || isExpired();
    }

    public enum LicenseStatus {
        /** Generated (signed key handed to the company) but not yet activated - grants no access. */
        GENERATED,
        ACTIVE,
        SUSPENDED,
        EXPIRED,
        REVOKED
    }
}
