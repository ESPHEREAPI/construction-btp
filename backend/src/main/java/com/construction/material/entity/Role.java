package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a role in the system.
 *
 * Super Admin/Company Admin/Administrateur (systemRole=true) are global
 * (company=null), fixed, and never shown in a company's role management
 * screen. Every other role belongs to exactly one company: the 4
 * operational roles are cloned per company at company-creation time
 * (name stays fixed - it doubles as the Spring Security authority string
 * checked by @PreAuthorize - only the permission set and the nameFr/
 * nameEn/namePt display labels are editable), and a company may also
 * create fully custom roles (custom=true, deletable, name auto-generated
 * and never shown/edited directly).
 */
@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "company_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "name_fr", length = 100)
    private String nameFr;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(name = "name_pt", length = 100)
    private String namePt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false)
    @Builder.Default
    private Boolean systemRole = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean custom = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    public enum RoleName {
        ROLE_SUPER_ADMIN,
        ROLE_COMPANY_ADMIN,
        ROLE_ADMIN,
        ROLE_PROJECT_MANAGER,
        ROLE_SITE_MANAGER,
        ROLE_INVENTORY_MANAGER,
        ROLE_READ_ONLY
    }
}
