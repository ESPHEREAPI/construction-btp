package com.construction.material.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockThresholdsRequest {
    private BigDecimal minimumQuantity;
    private BigDecimal maximumQuantity;
}
