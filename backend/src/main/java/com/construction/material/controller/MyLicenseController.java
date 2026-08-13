package com.construction.material.controller;

import com.construction.material.dto.request.ActivateLicenseRequest;
import com.construction.material.dto.response.LicenseMineResponse;
import com.construction.material.dto.response.LicenseResponse;
import com.construction.material.security.TenantContext;
import com.construction.material.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Self-service license management for any authenticated company user - deliberately
 * no class-level @PreAuthorize (unlike LicenseController, which is Super Admin only).
 * The Super Admin has no company (TenantContext is null for them) so these endpoints
 * are naturally meaningless for that role.
 */
@RestController
@RequestMapping("/api/licenses/me")
@RequiredArgsConstructor
@Tag(name = "My License", description = "Self-service license status and activation")
public class MyLicenseController {

    private final LicenseService licenseService;

    @GetMapping
    @Operation(summary = "Current active license and any license pending activation")
    public ResponseEntity<LicenseMineResponse> getMine() {
        return ResponseEntity.ok(licenseService.getMine(requireCompanyId()));
    }

    @GetMapping("/history")
    @Operation(summary = "Full license history for the current user's company")
    public ResponseEntity<List<LicenseResponse>> getHistory() {
        return ResponseEntity.ok(licenseService.getHistory(requireCompanyId()));
    }

    @PostMapping("/activate")
    @PreAuthorize("hasAuthority('ROLE_COMPANY_ADMIN')")
    @Operation(summary = "Activate a GENERATED license using its signed key")
    public ResponseEntity<LicenseResponse> activate(@Valid @RequestBody ActivateLicenseRequest request) {
        return ResponseEntity.ok(licenseService.activateByKey(requireCompanyId(), request.getKey()));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            throw new IllegalStateException("This account is not attached to any company");
        }
        return companyId;
    }
}
