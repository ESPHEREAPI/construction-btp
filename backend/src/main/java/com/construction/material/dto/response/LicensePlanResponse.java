package com.construction.material.dto.response;

import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.LicensePlan;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicensePlanResponse {
    private Long id;
    private LicensePlan.LicenseType type;
    private Integer durationDays;
    private Integer maxUsers;
    private Integer maxProjects;
    private Set<LicenseModule> modules;
    private LocalDateTime updatedAt;
}
