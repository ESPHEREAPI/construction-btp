package com.construction.material.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a construction material
 */
@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit;

    @Column(length = 100)
    private String category;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 100)
    private String supplier;

    @Column(length = 50)
    private String referenceNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Null = system material, seeded at startup and visible to every company.
     * Non-null = created by that company, visible only to it.
     * Excluded from JSON: lazy and never rendered by the frontend, and
     * serializing it after the request's Hibernate session closes throws
     * LazyInitializationException for any company-owned material.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    @Column(precision = 15, scale = 3)
    private BigDecimal minimumStock;

    @Column(length = 500)
    private String specifications;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Unit {
        KG("Kilogram", "kg"),
        TON("Ton", "t"),
        M3("Cubic Meter", "m³"),
        M2("Square Meter", "m²"),
        PIECE("Piece", "pcs"),
        LITER("Liter", "L"),
        METER("Meter", "m"),
        BAG("Bag", "bag"),
        BOX("Box", "box"),
        ROLL("Roll", "roll");

        private final String displayName;
        private final String symbol;

        Unit(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}
