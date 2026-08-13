package com.construction.material.exception;

import com.construction.material.entity.LicenseModule;

/**
 * Thrown by ModuleAccessGuard when the current company cannot access a
 * module - either because its license doesn't include that module, or
 * because the license itself isn't currently active (expired/suspended/none).
 */
public class LicenseModuleException extends RuntimeException {

    private final LicenseModule module;
    private final String code;

    public LicenseModuleException(LicenseModule module, String code, String message) {
        super(message);
        this.module = module;
        this.code = code;
    }

    public LicenseModule getModule() {
        return module;
    }

    public String getCode() {
        return code;
    }
}
