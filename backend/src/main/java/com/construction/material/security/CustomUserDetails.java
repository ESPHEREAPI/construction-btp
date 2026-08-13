package com.construction.material.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * UserDetails implementation that additionally carries the authenticated
 * user's company (tenant) id and mustChangePassword flag, so the JWT and
 * the request-scoped TenantContext can be populated from a single source.
 */
public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;
    private final Long companyId;
    private final boolean mustChangePassword;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                              boolean active, Long companyId, boolean mustChangePassword) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
        this.companyId = companyId;
        this.mustChangePassword = mustChangePassword;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
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
