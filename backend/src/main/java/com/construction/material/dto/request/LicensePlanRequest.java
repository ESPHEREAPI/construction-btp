package com.construction.material.dto.request;

import com.construction.material.entity.LicenseModule;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlanRequest {
    /** Null = unlimited duration */
    private Integer durationDays;

    @Min(1)
    private Integer maxUsers;

    @Min(1)
    private Integer maxProjects;

    private Set<LicenseModule> modules;
}
