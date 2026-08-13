package com.construction.material.dto.response;

import com.construction.material.entity.License;
import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.LicensePlan;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private String code;
    private Boolean active;
    private Boolean selfRegistered;
    private LocalDateTime createdAt;

    private LicensePlan.LicenseType licenseType;
    private License.LicenseStatus licenseStatus;
    private LocalDate licenseStartDate;
    private LocalDate licenseEndDate;
    private Integer maxUsers;
    private Integer maxProjects;
    private Set<LicenseModule> activeModules;
    private long currentUserCount;
    private long currentProjectCount;
    private boolean licenseBlocked;
    /** True when the latest license row exists but is still GENERATED (key not yet activated). */
    private boolean licenseKeyPending;
}
