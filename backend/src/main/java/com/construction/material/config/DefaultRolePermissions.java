package com.construction.material.config;

import com.construction.material.entity.Permission;
import com.construction.material.entity.Role;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Starting point for a company's own copy of the 4 operational roles - both
 * for a brand-new company (cloned immediately at creation) and for the
 * one-time migration of companies that predate per-company roles. A company
 * is free to change permissions/labels afterward through the Role
 * management screen; nothing here is re-applied once a company has its own
 * rows, unlike the system roles which DataInitializer keeps force-synced.
 */
public final class DefaultRolePermissions {

    private DefaultRolePermissions() {
    }

    public record Labels(String fr, String en, String pt) {
    }

    private static final Map<Role.RoleName, Labels> LABELS = new EnumMap<>(Role.RoleName.class);
    private static final Map<Role.RoleName, Set<Permission.PermissionName>> PERMISSIONS = new EnumMap<>(Role.RoleName.class);

    static {
        LABELS.put(Role.RoleName.ROLE_PROJECT_MANAGER, new Labels("Chef de Projet", "Project Manager", "Gerente de Projeto"));
        LABELS.put(Role.RoleName.ROLE_SITE_MANAGER, new Labels("Chef de Chantier", "Site Manager", "Gerente de Obra"));
        LABELS.put(Role.RoleName.ROLE_INVENTORY_MANAGER, new Labels("Gestionnaire d'Inventaire", "Inventory Manager", "Gerente de Inventário"));
        LABELS.put(Role.RoleName.ROLE_READ_ONLY, new Labels("Employé", "Employee", "Funcionário"));

        PERMISSIONS.put(Role.RoleName.ROLE_PROJECT_MANAGER, Set.of(
                Permission.PermissionName.PROJECT_READ,
                Permission.PermissionName.MATERIAL_READ,
                Permission.PermissionName.QUANTIFICATION_CREATE,
                Permission.PermissionName.QUANTIFICATION_READ,
                Permission.PermissionName.QUANTIFICATION_UPDATE,
                Permission.PermissionName.QUANTIFICATION_DELETE,
                Permission.PermissionName.ORDER_CREATE,
                Permission.PermissionName.ORDER_READ,
                Permission.PermissionName.ORDER_UPDATE,
                Permission.PermissionName.ORDER_APPROVE,
                Permission.PermissionName.USAGE_CREATE,
                Permission.PermissionName.USAGE_READ,
                Permission.PermissionName.USAGE_UPDATE,
                Permission.PermissionName.STOCK_READ,
                Permission.PermissionName.STOCK_UPDATE
        ));
        PERMISSIONS.put(Role.RoleName.ROLE_SITE_MANAGER, Set.of(
                Permission.PermissionName.PROJECT_READ,
                Permission.PermissionName.MATERIAL_READ,
                Permission.PermissionName.ORDER_READ,
                Permission.PermissionName.ORDER_UPDATE,
                Permission.PermissionName.USAGE_CREATE,
                Permission.PermissionName.USAGE_READ,
                Permission.PermissionName.STOCK_READ,
                Permission.PermissionName.STOCK_UPDATE
        ));
        PERMISSIONS.put(Role.RoleName.ROLE_INVENTORY_MANAGER, Set.of(
                Permission.PermissionName.PROJECT_READ,
                Permission.PermissionName.MATERIAL_CREATE,
                Permission.PermissionName.MATERIAL_READ,
                Permission.PermissionName.MATERIAL_UPDATE,
                Permission.PermissionName.MATERIAL_DELETE,
                Permission.PermissionName.ORDER_CREATE,
                Permission.PermissionName.ORDER_READ,
                Permission.PermissionName.ORDER_UPDATE,
                Permission.PermissionName.USAGE_READ,
                Permission.PermissionName.STOCK_READ,
                Permission.PermissionName.STOCK_UPDATE
        ));
        PERMISSIONS.put(Role.RoleName.ROLE_READ_ONLY, Set.of(
                Permission.PermissionName.PROJECT_READ,
                Permission.PermissionName.STOCK_READ
        ));
    }

    /** The 4 role names a company gets a private copy of - everything except the 3 fixed system roles. */
    public static final Role.RoleName[] OPERATIONAL_ROLES = {
            Role.RoleName.ROLE_PROJECT_MANAGER,
            Role.RoleName.ROLE_SITE_MANAGER,
            Role.RoleName.ROLE_INVENTORY_MANAGER,
            Role.RoleName.ROLE_READ_ONLY
    };

    public static Labels labelsFor(Role.RoleName roleName) {
        return LABELS.get(roleName);
    }

    public static Set<Permission.PermissionName> permissionsFor(Role.RoleName roleName) {
        return PERMISSIONS.get(roleName);
    }
}
