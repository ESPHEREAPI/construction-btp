package com.construction.material.service.impl;

import com.construction.material.dto.request.GenerateLicenseRequest;
import com.construction.material.dto.request.LicensePlanRequest;
import com.construction.material.dto.response.LicenseGenerationResponse;
import com.construction.material.dto.response.LicenseMineResponse;
import com.construction.material.dto.response.LicensePlanResponse;
import com.construction.material.dto.response.LicenseResponse;
import com.construction.material.entity.Company;
import com.construction.material.entity.License;
import com.construction.material.entity.LicensePlan;
import com.construction.material.entity.SystemSetting;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.CompanyRepository;
import com.construction.material.repository.LicensePlanRepository;
import com.construction.material.repository.LicenseRepository;
import com.construction.material.repository.SystemSettingRepository;
import com.construction.material.security.license.LicenseKeyService;
import com.construction.material.service.LicenseEnforcementStatus;
import com.construction.material.service.LicenseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LicenseServiceImpl implements LicenseService {

    @Autowired
    private LicensePlanRepository licensePlanRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private LicenseKeyService licenseKeyService;

    @Autowired
    private MessageSource messageSource;

    // ============================================================
    // Plans (catalog configuration)
    // ============================================================

    @Override
    public List<LicensePlanResponse> getPlans() {
        return licensePlanRepository.findAll().stream()
                .map(this::toPlanResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LicensePlanResponse updatePlan(LicensePlan.LicenseType type, LicensePlanRequest request) {
        LicensePlan plan = licensePlanRepository.findByType(type)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage("license.plan.not.found")));

        plan.setDurationDays(request.getDurationDays());
        plan.setMaxUsers(request.getMaxUsers());
        plan.setMaxProjects(request.getMaxProjects());
        if (request.getModules() != null) {
            plan.setDefaultModules(new HashSet<>(request.getModules()));
        }

        return toPlanResponse(licensePlanRepository.save(plan));
    }

    // ============================================================
    // Super Admin: grant lifecycle
    // ============================================================

    @Override
    public LicenseGenerationResponse generateLicense(Long companyId, GenerateLicenseRequest request, String actorUsername) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage("company.not.found")));

        License license = License.builder()
                .company(company)
                .type(request.getType())
                .status(License.LicenseStatus.GENERATED)
                .startDate(null)
                .endDate(request.getEndDate())
                .maxUsers(request.getMaxUsers())
                .maxProjects(request.getMaxProjects())
                .activeModules(new HashSet<>(request.getModules()))
                .trial(false)
                .createdBy(actorUsername)
                .build();

        License saved = licenseRepository.save(license);
        String key = licenseKeyService.generateKey(saved);
        saved.setLicenseKey(key);
        saved = licenseRepository.save(saved);

        return LicenseGenerationResponse.builder()
                .license(toResponse(saved))
                .licenseKey(key)
                .build();
    }

    @Override
    public LicenseResponse revoke(Long companyId, String actorUsername) {
        License license = getLatestEntity(companyId);
        if (license.getStatus() == License.LicenseStatus.REVOKED) {
            throw new BusinessException(notFoundMessage("license.action.invalid.state"));
        }
        license.setStatus(License.LicenseStatus.REVOKED);
        license.setRevokedBy(actorUsername);
        license.setRevokedAt(LocalDateTime.now());
        return toResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseResponse suspend(Long companyId) {
        License license = getLatestEntity(companyId);
        if (license.getStatus() != License.LicenseStatus.ACTIVE) {
            throw new BusinessException(notFoundMessage("license.action.invalid.state"));
        }
        license.setStatus(License.LicenseStatus.SUSPENDED);
        return toResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseResponse reactivate(Long companyId) {
        License license = getLatestEntity(companyId);
        if (license.getStatus() != License.LicenseStatus.SUSPENDED) {
            throw new BusinessException(notFoundMessage("license.action.invalid.state"));
        }
        license.setStatus(License.LicenseStatus.ACTIVE);
        return toResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseResponse getLatest(Long companyId) {
        return toResponse(getLatestEntity(companyId));
    }

    @Override
    public List<LicenseResponse> getHistory(Long companyId) {
        return licenseRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Enforcement (LicenseEnforcementFilter, ModuleAccessGuard)
    // ============================================================

    @Override
    public LicenseEnforcementStatus resolveEnforcementStatus(Long companyId) {
        var active = licenseRepository.findFirstByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, License.LicenseStatus.ACTIVE);

        if (active.isPresent() && !active.get().isExpired()) {
            License license = active.get();
            return LicenseEnforcementStatus.allow(license.getActiveModules(), license.getMaxUsers(), license.getMaxProjects());
        }
        if (active.isPresent()) {
            return LicenseEnforcementStatus.deny("LICENSE_EXPIRED");
        }

        var latest = licenseRepository.findFirstByCompanyIdOrderByCreatedAtDesc(companyId);
        if (latest.isEmpty()) {
            return LicenseEnforcementStatus.deny("LICENSE_REQUIRED");
        }
        if (latest.get().getStatus() == License.LicenseStatus.SUSPENDED) {
            return LicenseEnforcementStatus.deny("LICENSE_SUSPENDED");
        }
        return LicenseEnforcementStatus.deny("LICENSE_REQUIRED");
    }

    // ============================================================
    // Self-service
    // ============================================================

    @Override
    public License issueTrialLicense(Company company) {
        LicensePlan plan = licensePlanRepository.findByType(LicensePlan.LicenseType.TRIAL)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage("license.plan.not.found")));

        License license = License.builder()
                .company(company)
                .type(LicensePlan.LicenseType.TRIAL)
                .status(License.LicenseStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(plan.getDurationDays() != null ? LocalDate.now().plusDays(plan.getDurationDays()) : null)
                .maxUsers(plan.getMaxUsers())
                .maxProjects(plan.getMaxProjects())
                .activeModules(new HashSet<>(plan.getDefaultModules()))
                .trial(true)
                .createdBy("self-registration")
                .build();

        return licenseRepository.save(license);
    }

    @Override
    public LicenseResponse activateByKey(Long companyId, String key) {
        Claims claims;
        try {
            claims = licenseKeyService.verifyKey(key);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(notFoundMessage("license.key.invalid"));
        }

        Long licenseId = Long.valueOf(claims.get("licenseId").toString());
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new BusinessException(notFoundMessage("license.key.invalid")));

        if (!license.getCompany().getId().equals(companyId) || license.getStatus() != License.LicenseStatus.GENERATED) {
            throw new BusinessException(notFoundMessage("license.key.invalid"));
        }

        licenseRepository.findFirstByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, License.LicenseStatus.ACTIVE)
                .ifPresent(currentActive -> {
                    currentActive.setStatus(License.LicenseStatus.REVOKED);
                    currentActive.setRevokedBy("system:renewal");
                    currentActive.setRevokedAt(LocalDateTime.now());
                    licenseRepository.save(currentActive);
                });

        license.setStatus(License.LicenseStatus.ACTIVE);
        license.setStartDate(LocalDate.now());
        return toResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseMineResponse getMine(Long companyId) {
        LicenseResponse current = licenseRepository.findFirstByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, License.LicenseStatus.ACTIVE)
                .filter(license -> !license.isExpired())
                .map(this::toResponse)
                .orElse(null);

        LicenseResponse pending = licenseRepository.findFirstByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, License.LicenseStatus.GENERATED)
                .map(this::toResponse)
                .orElse(null);

        return LicenseMineResponse.builder().current(current).pending(pending).build();
    }

    // ============================================================
    // Platform settings
    // ============================================================

    @Override
    public boolean isSelfRegistrationEnabled() {
        return systemSettingRepository.findByKey(SystemSetting.SELF_REGISTRATION_ENABLED)
                .map(setting -> Boolean.parseBoolean(setting.getValue()))
                .orElse(true);
    }

    @Override
    public void setSelfRegistrationEnabled(boolean enabled) {
        SystemSetting setting = systemSettingRepository.findByKey(SystemSetting.SELF_REGISTRATION_ENABLED)
                .orElseGet(() -> SystemSetting.builder().key(SystemSetting.SELF_REGISTRATION_ENABLED).build());
        setting.setValue(String.valueOf(enabled));
        systemSettingRepository.save(setting);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private License getLatestEntity(Long companyId) {
        return licenseRepository.findFirstByCompanyIdOrderByCreatedAtDesc(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage("license.not.found")));
    }

    private String notFoundMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private LicenseResponse toResponse(License license) {
        return LicenseResponse.builder()
                .id(license.getId())
                .companyId(license.getCompany().getId())
                .type(license.getType())
                .status(license.getStatus())
                .startDate(license.getStartDate())
                .endDate(license.getEndDate())
                .maxUsers(license.getMaxUsers())
                .maxProjects(license.getMaxProjects())
                .activeModules(license.getActiveModules())
                .trial(license.isTrial())
                .createdBy(license.getCreatedBy())
                .createdAt(license.getCreatedAt())
                .revokedBy(license.getRevokedBy())
                .revokedAt(license.getRevokedAt())
                .build();
    }

    private LicensePlanResponse toPlanResponse(LicensePlan plan) {
        return LicensePlanResponse.builder()
                .id(plan.getId())
                .type(plan.getType())
                .durationDays(plan.getDurationDays())
                .maxUsers(plan.getMaxUsers())
                .maxProjects(plan.getMaxProjects())
                .modules(plan.getDefaultModules())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
