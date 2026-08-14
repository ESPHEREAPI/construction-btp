package com.construction.material.controller;

import com.construction.material.dto.request.CreateUserRequest;
import com.construction.material.dto.request.UpdateUserRequest;
import com.construction.material.dto.response.PasswordResetResponse;
import com.construction.material.dto.response.UserResponse;
import com.construction.material.entity.LicenseModule;
import com.construction.material.security.ModuleAccessGuard;
import com.construction.material.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User management within a company. Company Admin/Admin manage their own
 * company's users (scoped via TenantContext, enforced in the service layer).
 * The Super Admin may additionally list a specific company's users and reset
 * any user's password (e.g. from the company detail screen), but does not
 * create/update/delete users here - that stays a Company Admin action.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
@Tag(name = "Users", description = "Company-scoped user management")
public class UserController {

    private final UserManagementService userManagementService;
    private final ModuleAccessGuard moduleAccessGuard;

    @GetMapping
    @Operation(summary = "List users (own company, or ?companyId= for the Super Admin)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userManagementService.findUsers(companyId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single user")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        return ResponseEntity.ok(userManagementService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Create a user in the caller's company")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Update a user in the caller's company")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        return ResponseEntity.ok(userManagementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Delete a user in the caller's company")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        userManagementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN','ROLE_ADMIN')")
    @Operation(summary = "Activate or deactivate a user in the caller's company")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        return ResponseEntity.ok(userManagementService.toggleStatus(id));
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "Reset a user's password to a random temporary one (returned once)")
    public ResponseEntity<PasswordResetResponse> resetPassword(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ADMIN);
        return ResponseEntity.ok(userManagementService.resetPassword(id));
    }
}
