package com.construction.material.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String name;
    
    private String description;
    private String location;
    private String client;
    private String status;
    
    @NotNull
    private LocalDate startDate;
    
    private LocalDate estimatedEndDate;
    private BigDecimal budget;
    private Long projectManagerId;
    private Long siteManagerId;
}
