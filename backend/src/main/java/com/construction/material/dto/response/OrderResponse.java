package com.construction.material.dto.response;

import com.construction.material.entity.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String code;
    private Long projectId;
    private String projectName;
    private String supplier;
    private Order.OrderStatus status;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private BigDecimal totalAmount;
    private String notes;
    private List<OrderItemResponse> items;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
