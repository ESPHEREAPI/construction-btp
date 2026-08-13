package com.construction.material.dto.response;

import com.construction.material.entity.License;
import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.LicensePlan;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/** Read model for a License row. Deliberately never carries the raw activation key. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseResponse {
    private Long id;
    private Long companyId;
    private LicensePlan.LicenseType type;
    private License.LicenseStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsers;
    private Integer maxProjects;
    private Set<LicenseModule> activeModules;
    private boolean trial;
    private String createdBy;
    private LocalDateTime createdAt;
    private String revokedBy;
    private LocalDateTime revokedAt;
}
