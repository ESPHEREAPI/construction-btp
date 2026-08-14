package com.construction.material.controller;

import com.construction.material.dto.request.UsageRequest;
import com.construction.material.dto.response.UsageResponse;
import com.construction.material.entity.LicenseModule;
import com.construction.material.security.ModuleAccessGuard;
import com.construction.material.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@Tag(name = "Usage", description = "Material usage tracking API")
@RestController
@RequestMapping("/api/usages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsageController {

    private final UsageService usageService;
    private final ModuleAccessGuard moduleAccessGuard;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USAGE_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Record a material usage")
    public ResponseEntity<UsageResponse> create(@Valid @RequestBody UsageRequest request) {
        moduleAccessGuard.require(LicenseModule.USAGE);
        return new ResponseEntity<>(usageService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USAGE_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update a usage entry")
    public ResponseEntity<UsageResponse> update(@PathVariable Long id, @Valid @RequestBody UsageRequest request) {
        moduleAccessGuard.require(LicenseModule.USAGE);
        return ResponseEntity.ok(usageService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USAGE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get a usage entry by id")
    public ResponseEntity<UsageResponse> getById(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.USAGE);
        return ResponseEntity.ok(usageService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USAGE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "List usage entries, optionally filtered by project or material")
    public ResponseEntity<Page<UsageResponse>> getAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        moduleAccessGuard.require(LicenseModule.USAGE);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(usageService.findAllPaginated(projectId, materialId, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USAGE_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete a usage entry")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.USAGE);
        usageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
