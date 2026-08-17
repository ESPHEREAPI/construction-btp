package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Per-company override hiding a shared catalog material (Material.company == null)
 * from that company's own material list, without affecting any other company.
 * Never used for a company's own materials - those are deleted outright instead.
 */
@Entity
@Table(name = "hidden_materials", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "material_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HiddenMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime hiddenAt;
}
