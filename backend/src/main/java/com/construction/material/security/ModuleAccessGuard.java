package com.construction.material.security;

import com.construction.material.entity.LicenseModule;
import com.construction.material.exception.LicenseModuleException;
import com.construction.material.service.LicenseEnforcementStatus;
import com.construction.material.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Defense-in-depth module gating: call require(MODULE) as the first line of
 * every method on a tenant-scoped controller (ProjectController, MaterialController,
 * and any future Order/Usage/Stock/Admin controller). Gates BOTH reads and writes,
 * unlike LicenseEnforcementFilter's expiry-driven progressive lock (writes only) -
 * module absence is a feature-flag concept, not a grace-period concept.
 */
@Component
public class ModuleAccessGuard {

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private MessageSource messageSource;

    public void require(LicenseModule module) {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return; // Super Admin - not module-gated
        }

        LicenseEnforcementStatus status = licenseService.resolveEnforcementStatus(companyId);
        if (!status.isAllowed()) {
            throw new LicenseModuleException(module, status.getCode(), messageFor(status.getCode(), module));
        }
        if (status.getActiveModules() == null || !status.getActiveModules().contains(module)) {
            throw new LicenseModuleException(module, "LICENSE_MODULE_NOT_ACTIVE",
                    messageSource.getMessage("license.module.not.active", new Object[]{module.name()}, LocaleContextHolder.getLocale()));
        }
    }

    private String messageFor(String code, LicenseModule module) {
        String key = switch (code) {
            case "LICENSE_SUSPENDED" -> "license.enforcement.suspended";
            case "LICENSE_REQUIRED" -> "license.enforcement.required";
            default -> "license.enforcement.expired";
        };
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
