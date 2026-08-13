package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a permission in the system
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String category;

    public enum PermissionName {
        // User permissions
        USER_CREATE,
        USER_READ,
        USER_UPDATE,
        USER_DELETE,
        
        // Project permissions
        PROJECT_CREATE,
        PROJECT_READ,
        PROJECT_UPDATE,
        PROJECT_DELETE,
        
        // Material permissions
        MATERIAL_CREATE,
        MATERIAL_READ,
        MATERIAL_UPDATE,
        MATERIAL_DELETE,
        
        // Quantification permissions
        QUANTIFICATION_CREATE,
        QUANTIFICATION_READ,
        QUANTIFICATION_UPDATE,
        QUANTIFICATION_DELETE,
        
        // Order permissions
        ORDER_CREATE,
        ORDER_READ,
        ORDER_UPDATE,
        ORDER_DELETE,
        ORDER_APPROVE,
        
        // Usage permissions
        USAGE_CREATE,
        USAGE_READ,
        USAGE_UPDATE,
        USAGE_DELETE,
        
        // Stock permissions
        STOCK_READ,
        STOCK_UPDATE,
        
        // Audit permissions
        AUDIT_READ,
        
        // Settings permissions
        SETTINGS_READ,
        SETTINGS_UPDATE
    }
}
