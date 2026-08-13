package com.construction.material.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    private String unit;
    
    private String category;
    private BigDecimal unitPrice;
    private String supplier;
    private String referenceNumber;
    private Boolean active;
    private BigDecimal minimumStock;
    private String specifications;
}
