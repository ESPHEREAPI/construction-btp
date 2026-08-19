package com.construction.material.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.construction.material.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private static final String CLAIM_COMPANY_ID = "companyId";
    private static final String CLAIM_MUST_CHANGE_PASSWORD = "mustChangePassword";
    private static final String CLAIM_ASSIGNED_PROJECT_ID = "assignedProjectId";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        JwtBuilder builder = Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs));

        if (userPrincipal instanceof CustomUserDetails customUserDetails) {
            builder.claim(CLAIM_COMPANY_ID, customUserDetails.getCompanyId());
            builder.claim(CLAIM_MUST_CHANGE_PASSWORD, customUserDetails.isMustChangePassword());
            builder.claim(CLAIM_ASSIGNED_PROJECT_ID, customUserDetails.getAssignedProjectId());
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getCompanyIdFromJwtToken(String token) {
        Object companyId = parseClaims(token).get(CLAIM_COMPANY_ID);
        return companyId != null ? Long.valueOf(companyId.toString()) : null;
    }

    public boolean getMustChangePasswordFromJwtToken(String token) {
        Object value = parseClaims(token).get(CLAIM_MUST_CHANGE_PASSWORD);
        return value != null && Boolean.parseBoolean(value.toString());
    }

    public Long getAssignedProjectIdFromJwtToken(String token) {
        Object projectId = parseClaims(token).get(CLAIM_ASSIGNED_PROJECT_ID);
        return projectId != null ? Long.valueOf(projectId.toString()) : null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
