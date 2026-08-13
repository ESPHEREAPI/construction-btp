package com.construction.material.controller;

import com.construction.material.dto.request.LicensePlanRequest;
import com.construction.material.dto.response.LicensePlanResponse;
import com.construction.material.dto.response.MessageResponse;
import com.construction.material.entity.LicensePlan;
import com.construction.material.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Super Admin only: configure license plans (TRIAL/PAYANT), assign a
 * company's license, and toggle public self-registration.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
@Tag(name = "Licenses (Super Admin)", description = "License plan and settings management APIs")
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping("/license-plans")
    @Operation(summary = "Get TRIAL/PAYANT plan configuration")
    public ResponseEntity<List<LicensePlanResponse>> getPlans() {
        return ResponseEntity.ok(licenseService.getPlans());
    }

    @PutMapping("/license-plans/{type}")
    @Operation(summary = "Update a plan's duration and usage limits")
    public ResponseEntity<LicensePlanResponse> updatePlan(@PathVariable LicensePlan.LicenseType type,
                                                            @Valid @RequestBody LicensePlanRequest request) {
        return ResponseEntity.ok(licenseService.updatePlan(type, request));
    }

    @PutMapping("/settings/self-registration")
    @Operation(summary = "Enable or disable public self-registration")
    public ResponseEntity<MessageResponse> setSelfRegistrationEnabled(@RequestParam boolean enabled) {
        licenseService.setSelfRegistrationEnabled(enabled);
        return ResponseEntity.ok(new MessageResponse("OK"));
    }

    @GetMapping("/settings/self-registration")
    @Operation(summary = "Get current self-registration toggle state")
    public ResponseEntity<Map<String, Boolean>> getSelfRegistrationEnabled() {
        return ResponseEntity.ok(Map.of("enabled", licenseService.isSelfRegistrationEnabled()));
    }
}
