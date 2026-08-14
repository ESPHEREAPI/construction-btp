package com.construction.material.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal minimumQuantity;
    private BigDecimal maximumQuantity;
    private String location;
    private Boolean lowStockAlert;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
