package com.construction.material.security.license;

import com.construction.material.entity.License;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Generates and verifies the signed activation key a Super Admin hands to a
 * company to activate a GENERATED license. Deliberately separate from JwtUtils
 * (the user session JWT) — different secret, different trust boundary.
 */
@Component
public class LicenseKeyService {

    /** endDate == null means "unlimited" on the License itself; the JWT spec requires a concrete expiration claim. */
    private static final int UNLIMITED_SENTINEL_YEARS = 50;

    @Value("${app.license.secret}")
    private String licenseSecret;

    public String generateKey(License license) {
        LocalDate expiryDate = license.getEndDate() != null
                ? license.getEndDate()
                : LocalDate.now().plusYears(UNLIMITED_SENTINEL_YEARS);

        return Jwts.builder()
                .subject(license.getCompany().getId().toString())
                .claim("licenseId", license.getId())
                .claim("companyId", license.getCompany().getId())
                .claim("maxUsers", license.getMaxUsers())
                .claim("maxProjects", license.getMaxProjects())
                .claim("modules", license.getActiveModules().stream().map(Enum::name).collect(Collectors.toList()))
                .issuedAt(new Date())
                .expiration(Date.from(expiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey())
                .compact();
    }

    /** Throws JwtException/IllegalArgumentException on a malformed, expired or forged key - let the caller translate to a BusinessException. */
    public Claims verifyKey(String key) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(key)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(licenseSecret.getBytes(StandardCharsets.UTF_8));
    }
}
