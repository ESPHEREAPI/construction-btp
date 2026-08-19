package com.construction.material.security;

/**
 * Request-scoped holder for the authenticated user's assigned project id.
 * Populated by JwtAuthFilter and cleared at the end of every request, mirroring
 * TenantContext. Null means the current user is not restricted to a single
 * project (Super Admin, Company Admin, Admin, or an operational user with no
 * project assignment yet).
 */
public final class ProjectContext {

    private static final ThreadLocal<Long> CURRENT_PROJECT_ID = new ThreadLocal<>();

    private ProjectContext() {
    }

    public static void set(Long projectId) {
        CURRENT_PROJECT_ID.set(projectId);
    }

    public static Long get() {
        return CURRENT_PROJECT_ID.get();
    }

    public static void clear() {
        CURRENT_PROJECT_ID.remove();
    }
}
