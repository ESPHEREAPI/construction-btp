package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing an item in a material order
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal orderedQuantity;

    @Column(precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String notes;

    /**
     * Calculate total price from unit price and ordered quantity
     */
    public void calculateTotalPrice() {
        if (unitPrice != null && orderedQuantity != null) {
            this.totalPrice = unitPrice.multiply(orderedQuantity)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    /**
     * Check if the item is fully received
     */
    public boolean isFullyReceived() {
        if (orderedQuantity == null || receivedQuantity == null) {
            return false;
        }
        return receivedQuantity.compareTo(orderedQuantity) >= 0;
    }

    /**
     * Check if the item is partially received
     */
    public boolean isPartiallyReceived() {
        if (receivedQuantity == null || receivedQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return !isFullyReceived();
    }
}
