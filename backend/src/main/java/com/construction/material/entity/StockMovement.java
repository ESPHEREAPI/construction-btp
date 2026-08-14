package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a stock movement (in/out)
 */
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantityBefore;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantityAfter;

    @Column(length = 1000)
    private String reason;

    @Column(length = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_id")
    private Usage usage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime movementDate;

    @Column(length = 500)
    private String notes;

    public enum MovementType {
        IN,
        OUT,
        TRANSFER,
        ADJUSTMENT
    }
}
