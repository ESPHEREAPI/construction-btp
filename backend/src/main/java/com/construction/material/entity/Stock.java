package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing the stock of materials for a specific project
 */
@Entity
@Table(name = "stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "material_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @Column(precision = 15, scale = 3)
    private BigDecimal minimumQuantity;

    @Column(precision = 15, scale = 3)
    private BigDecimal maximumQuantity;

    @Column(length = 200)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lowStockAlert = false;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockMovement> movements = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculate available quantity
     */
    public void calculateAvailableQuantity() {
        if (currentQuantity != null && reservedQuantity != null) {
            this.availableQuantity = currentQuantity.subtract(reservedQuantity);
        } else if (currentQuantity != null) {
            this.availableQuantity = currentQuantity;
        } else {
            this.availableQuantity = BigDecimal.ZERO;
        }

        // Check for low stock alert
        if (minimumQuantity != null && currentQuantity != null) {
            this.lowStockAlert = currentQuantity.compareTo(minimumQuantity) <= 0;
        }
    }

    /**
     * Add quantity to stock
     */
    public void addQuantity(BigDecimal quantity) {
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            if (this.currentQuantity == null) {
                this.currentQuantity = BigDecimal.ZERO;
            }
            this.currentQuantity = this.currentQuantity.add(quantity);
            calculateAvailableQuantity();
        }
    }

    /**
     * Remove quantity from stock
     */
    public void removeQuantity(BigDecimal quantity) {
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            if (this.currentQuantity == null) {
                this.currentQuantity = BigDecimal.ZERO;
            }
            this.currentQuantity = this.currentQuantity.subtract(quantity);
            if (this.currentQuantity.compareTo(BigDecimal.ZERO) < 0) {
                this.currentQuantity = BigDecimal.ZERO;
            }
            calculateAvailableQuantity();
        }
    }
}
