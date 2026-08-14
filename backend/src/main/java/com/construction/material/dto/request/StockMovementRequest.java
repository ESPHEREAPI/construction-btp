package com.construction.material.dto.request;

import com.construction.material.entity.StockMovement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequest {

    @NotNull
    private Long stockId;

    /** Required when movementType is TRANSFER. */
    private Long destinationStockId;

    @NotNull
    private StockMovement.MovementType movementType;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal quantity;

    private String reference;

    private String reason;

    private String notes;
}
