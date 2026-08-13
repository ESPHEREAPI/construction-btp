package com.construction.material.service;

import com.construction.material.entity.LicenseModule;
import lombok.*;

import java.util.Set;

/**
 * Result of resolving a company's current enforceable license state. The single
 * source of truth consumed by both LicenseEnforcementFilter (expiry/suspension,
 * write-blocking) and ModuleAccessGuard (per-module gating, read+write blocking).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseEnforcementStatus {
    private boolean allowed;
    /** LICENSE_EXPIRED | LICENSE_SUSPENDED | LICENSE_REQUIRED - null when allowed. */
    private String code;
    private Set<LicenseModule> activeModules;
    private Integer maxUsers;
    private Integer maxProjects;

    public static LicenseEnforcementStatus deny(String code) {
        return LicenseEnforcementStatus.builder().allowed(false).code(code).build();
    }

    public static LicenseEnforcementStatus allow(Set<LicenseModule> activeModules, Integer maxUsers, Integer maxProjects) {
        return LicenseEnforcementStatus.builder()
                .allowed(true)
                .activeModules(activeModules)
                .maxUsers(maxUsers)
                .maxProjects(maxProjects)
                .build();
    }
}
