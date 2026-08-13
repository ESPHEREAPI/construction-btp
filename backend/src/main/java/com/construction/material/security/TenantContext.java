package com.construction.material.security;

/**
 * Request-scoped holder for the authenticated user's company (tenant) id.
 * Populated by JwtAuthFilter and cleared at the end of every request.
 * Null means the current user is not attached to any company (the Super Admin).
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_COMPANY_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long companyId) {
        CURRENT_COMPANY_ID.set(companyId);
    }

    public static Long get() {
        return CURRENT_COMPANY_ID.get();
    }

    public static void clear() {
        CURRENT_COMPANY_ID.remove();
    }
}
