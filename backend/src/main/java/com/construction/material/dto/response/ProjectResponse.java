package com.construction.material.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String location;
    private String client;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate estimatedEndDate;
    private BigDecimal budget;
    private String projectManagerName;
    private String siteManagerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
