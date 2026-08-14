package com.construction.material.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageRequest {

    @NotNull
    private Long projectId;

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal quantity;

    @NotNull
    private LocalDate usageDate;

    private String usedBy;

    private String location;

    private String purpose;

    private String notes;
}
