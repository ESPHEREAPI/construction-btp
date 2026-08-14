package com.construction.material.controller;

import com.construction.material.dto.response.RoleResponse;
import com.construction.material.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Roles assignable to a company user (everything except Super Admin). */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
@Tag(name = "Roles", description = "Assignable role catalogue")
public class RoleController {

    private final UserManagementService userManagementService;

    @GetMapping
    @Operation(summary = "List roles that can be assigned to a company user")
    public ResponseEntity<List<RoleResponse>> getAssignableRoles() {
        return ResponseEntity.ok(userManagementService.listAssignableRoles());
    }
}
