package com.construction.material.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private String notes;
}
