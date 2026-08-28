package com.construction.material.controller;

import com.construction.material.dto.request.CompanySettingsRequest;
import com.construction.material.dto.response.CompanySettingsResponse;
import com.construction.material.security.TenantContext;
import com.construction.material.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Self-service settings for the current user's own company - deliberately no class-level
 * @PreAuthorize (unlike CompanyController, which is Super Admin only managing every
 * company). The Super Admin has no company (TenantContext is null for them) so these
 * endpoints are naturally meaningless for that role.
 */
@RestController
@RequestMapping("/api/companies/me")
@RequiredArgsConstructor
@Tag(name = "Company Settings", description = "Self-service settings for the current company (e.g. currency)")
public class CompanySettingsController {

    private final CompanyService companyService;

    @GetMapping("/settings")
    @Operation(summary = "Current company's settings (currency label used across the app)")
    public ResponseEntity<CompanySettingsResponse> getSettings() {
        return ResponseEntity.ok(companyService.getSettings(requireCompanyId()));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPANY_ADMIN', 'ROLE_ADMIN')")
    @Operation(summary = "Update the current company's settings")
    public ResponseEntity<CompanySettingsResponse> updateSettings(@Valid @RequestBody CompanySettingsRequest request) {
        return ResponseEntity.ok(companyService.updateSettings(requireCompanyId(), request));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            throw new IllegalStateException("This account is not attached to any company");
        }
        return companyId;
    }
}
