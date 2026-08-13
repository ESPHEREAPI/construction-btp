package com.construction.material.security;

import com.construction.material.service.LicenseEnforcementStatus;
import com.construction.material.service.LicenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enforces the "read-only" (progressive) lock for companies whose license is
 * not currently ACTIVE (expired, suspended, or never issued/activated): GET
 * requests still pass through, but any write request is rejected with HTTP
 * 402 (Payment Required - deliberately distinct from 401/403, which stay pure
 * authentication/authorization concerns) until the Super Admin/company admin
 * resolves the license state.
 *
 * The Super Admin (no companyId in TenantContext), /api/auth/** and
 * /api/licenses/me/** are never blocked here - the latter so a company stuck
 * with no active license can still activate a key to fix it.
 */
@Component
public class LicenseEnforcementFilter extends OncePerRequestFilter {

    private static final int STATUS_PAYMENT_REQUIRED = 402;

    private static final Set<String> WRITE_METHODS = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.PATCH.name());

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Long companyId = TenantContext.get();
        String uri = request.getRequestURI();

        if (companyId != null && !isExempt(uri) && WRITE_METHODS.contains(request.getMethod())) {
            LicenseEnforcementStatus status = licenseService.resolveEnforcementStatus(companyId);
            if (!status.isAllowed()) {
                writeBlockedResponse(response, status.getCode());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExempt(String uri) {
        return uri.startsWith("/api/auth/") || uri.startsWith("/api/licenses/me");
    }

    private void writeBlockedResponse(HttpServletResponse response, String code) throws IOException {
        response.setStatus(STATUS_PAYMENT_REQUIRED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", STATUS_PAYMENT_REQUIRED);
        body.put("code", code);
        body.put("message", messageSource.getMessage(messageKeyFor(code), null, LocaleContextHolder.getLocale()));

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String messageKeyFor(String code) {
        return switch (code) {
            case "LICENSE_SUSPENDED" -> "license.enforcement.suspended";
            case "LICENSE_REQUIRED" -> "license.enforcement.required";
            default -> "license.enforcement.expired";
        };
    }
}
