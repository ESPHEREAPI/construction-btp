package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing the configurable parameters of a license type (catalog of 2 rows: TRIAL, PAYANT)
 */
@Entity
@Table(name = "license_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicensePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 20)
    private LicenseType type;

    /** Null = unlimited duration */
    private Integer durationDays;

    @Column(nullable = false)
    private Integer maxUsers;

    @Column(nullable = false)
    private Integer maxProjects;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "license_plan_modules", joinColumns = @JoinColumn(name = "license_plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "module")
    @Builder.Default
    private Set<LicenseModule> defaultModules = new HashSet<>();

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum LicenseType {
        TRIAL,
        PAYANT
    }
}
