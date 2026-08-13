package com.construction.material.dto.request;

import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.LicensePlan;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateLicenseRequest {

    @NotNull
    private LicensePlan.LicenseType type;

    /** Null = never expires by date. */
    private LocalDate endDate;

    @NotNull
    private Integer maxUsers;

    @NotNull
    private Integer maxProjects;

    @NotEmpty
    private Set<LicenseModule> modules;
}
