package com.construction.material.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * UserDetails implementation that additionally carries the authenticated
 * user's company (tenant) id, assigned project id, and mustChangePassword
 * flag, so the JWT and the request-scoped TenantContext/ProjectContext can
 * be populated from a single source.
 */
public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;
    private final Long companyId;
    private final boolean mustChangePassword;
    private final Long assignedProjectId;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                              boolean active, Long companyId, boolean mustChangePassword, Long assignedProjectId) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
        this.companyId = companyId;
        this.mustChangePassword = mustChangePassword;
        this.assignedProjectId = assignedProjectId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public Long getAssignedProjectId() {
        return assignedProjectId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
