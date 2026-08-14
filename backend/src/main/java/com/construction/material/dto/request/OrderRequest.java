package com.construction.material.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull
    private Long projectId;

    private String supplier;

    @NotNull
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
