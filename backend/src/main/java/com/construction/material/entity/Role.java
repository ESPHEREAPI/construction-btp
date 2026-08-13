package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a role in the system
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

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
