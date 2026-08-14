package com.construction.material.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal quantity;
    private String unit;
    private LocalDate usageDate;
    private String notes;
    private String usedBy;
    private String recordedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
