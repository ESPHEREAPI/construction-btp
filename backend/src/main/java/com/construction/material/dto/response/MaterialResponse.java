package com.construction.material.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String unit;
    private String category;
    private BigDecimal unitPrice;
    private String supplier;
    private Boolean active;
    private LocalDateTime createdAt;
}
