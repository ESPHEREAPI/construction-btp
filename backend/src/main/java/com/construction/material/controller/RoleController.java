package com.construction.material.controller;

import com.construction.material.dto.request.RoleRequest;
import com.construction.material.dto.response.PermissionResponse;
import com.construction.material.dto.response.RoleResponse;
import com.construction.material.service.RoleManagementService;
import com.construction.material.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Roles assignable to a company user (everything except Super Admin), plus
 * CRUD for a company's own role management screen (Company Admin/Admin).
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
@Tag(name = "Roles", description = "Assignable role catalogue and per-company role management")
public class RoleController {

    private final UserManagementService userManagementService;
    private final RoleManagementService roleManagementService;

    @GetMapping
    @Operation(summary = "List roles that can be assigned to a company user")
    public ResponseEntity<List<RoleResponse>> getAssignableRoles() {
        return ResponseEntity.ok(userManagementService.listAssignableRoles());
    }

    @GetMapping("/permissions")
    @Operation(summary = "Full permission catalogue, for building the role edit form")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(roleManagementService.listAllPermissions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Get one of the caller's company roles")
    public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleManagementService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Create a custom role for the caller's company")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleManagementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Update a role's labels/permissions")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleManagementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Delete a custom role (not assigned to any user)")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleManagementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
