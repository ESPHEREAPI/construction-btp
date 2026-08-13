package com.construction.material.controller;

import com.construction.material.dto.request.CompanyRequest;
import com.construction.material.dto.request.CreateCompanyWithAdminRequest;
import com.construction.material.dto.request.GenerateLicenseRequest;
import com.construction.material.dto.response.CompanyResponse;
import com.construction.material.dto.response.LicenseGenerationResponse;
import com.construction.material.dto.response.LicenseResponse;
import com.construction.material.service.CompanyService;
import com.construction.material.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin only: manage client companies (tenants) of the platform and
 * their license grants (generate/revoke/suspend/reactivate/history).
 */
@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
@Tag(name = "Companies (Super Admin)", description = "Company/tenant management APIs")
public class CompanyController {

    private final CompanyService companyService;
    private final LicenseService licenseService;

    @GetMapping
    @Operation(summary = "List all companies with their license status")
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @PostMapping
    @Operation(summary = "Manually create a company and its first admin")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse created = companyService.createCompanyWithAdmin(CreateCompanyWithAdminRequest.builder()
                .companyName(request.getCompanyName())
                .selfRegistered(false)
                .username(request.getAdminUsername())
                .email(request.getAdminEmail())
                .password(request.getAdminPassword())
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .phone(request.getAdminPhone())
                .preferredLanguage(request.getPreferredLanguage())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Activate or suspend a company")
    public ResponseEntity<CompanyResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(companyService.setActive(id, active));
    }

    @PostMapping("/{id}/license/generate")
    @Operation(summary = "Generate a new license (GENERATED state) and its one-time activation key")
    public ResponseEntity<LicenseGenerationResponse> generateLicense(@PathVariable Long id,
                                                                       @Valid @RequestBody GenerateLicenseRequest request,
                                                                       Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(licenseService.generateLicense(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/license/revoke")
    @Operation(summary = "Revoke the company's current license (terminal)")
    public ResponseEntity<LicenseResponse> revokeLicense(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(licenseService.revoke(id, authentication.getName()));
    }

    @PostMapping("/{id}/license/suspend")
    @Operation(summary = "Suspend the company's active license")
    public ResponseEntity<LicenseResponse> suspendLicense(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.suspend(id));
    }

    @PostMapping("/{id}/license/reactivate")
    @Operation(summary = "Reactivate the company's suspended license")
    public ResponseEntity<LicenseResponse> reactivateLicense(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.reactivate(id));
    }

    @GetMapping("/{id}/license/history")
    @Operation(summary = "Full license history for a company")
    public ResponseEntity<List<LicenseResponse>> getLicenseHistory(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.getHistory(id));
    }
}
