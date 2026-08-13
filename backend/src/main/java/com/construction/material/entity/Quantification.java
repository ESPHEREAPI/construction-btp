package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the initial quantification of materials for a project
 */
@Entity
@Table(name = "quantifications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "material_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quantification {

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
    private BigDecimal estimatedQuantity;

    @Column(precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal usedQuantity = BigDecimal.ZERO;

    @Column(precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal remainingQuantity = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal variancePercentage;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean alertEnabled = true;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal alertThreshold = new BigDecimal("90.00");

    @Column(nullable = false)
    @Builder.Default
    private Boolean alertTriggered = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /**
     * Calculate and update the remaining quantity and variance percentage
     */
    public void calculateMetrics() {
        if (estimatedQuantity != null && usedQuantity != null) {
            this.remainingQuantity = estimatedQuantity.subtract(usedQuantity);
            
            if (estimatedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                this.variancePercentage = usedQuantity
                    .divide(estimatedQuantity, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            } else {
                this.variancePercentage = BigDecimal.ZERO;
            }

            // Check if alert should be triggered
            if (alertEnabled && variancePercentage.compareTo(alertThreshold) >= 0) {
                this.alertTriggered = true;
            }
        }
    }
}
