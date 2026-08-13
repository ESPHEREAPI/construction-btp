package com.construction.material.service;

import com.construction.material.dto.request.LicensePlanRequest;
import com.construction.material.dto.request.GenerateLicenseRequest;
import com.construction.material.dto.response.LicenseGenerationResponse;
import com.construction.material.dto.response.LicenseMineResponse;
import com.construction.material.dto.response.LicensePlanResponse;
import com.construction.material.dto.response.LicenseResponse;
import com.construction.material.entity.Company;
import com.construction.material.entity.License;
import com.construction.material.entity.LicensePlan;

import java.util.List;

public interface LicenseService {

    // --- Plans (catalog configuration) ---
    List<LicensePlanResponse> getPlans();
    LicensePlanResponse updatePlan(LicensePlan.LicenseType type, LicensePlanRequest request);

    // --- Super Admin: grant lifecycle ---
    LicenseGenerationResponse generateLicense(Long companyId, GenerateLicenseRequest request, String actorUsername);
    LicenseResponse revoke(Long companyId, String actorUsername);
    LicenseResponse suspend(Long companyId);
    LicenseResponse reactivate(Long companyId);
    LicenseResponse getLatest(Long companyId);
    List<LicenseResponse> getHistory(Long companyId);

    // --- Enforcement (used by LicenseEnforcementFilter and ModuleAccessGuard) ---
    LicenseEnforcementStatus resolveEnforcementStatus(Long companyId);

    // --- Self-service ---
    /** Issues an immediately-ACTIVE TRIAL license with no key, for self-registered companies. */
    License issueTrialLicense(Company company);
    LicenseResponse activateByKey(Long companyId, String key);
    LicenseMineResponse getMine(Long companyId);

    // --- Platform settings ---
    boolean isSelfRegistrationEnabled();
    void setSelfRegistrationEnabled(boolean enabled);
}
