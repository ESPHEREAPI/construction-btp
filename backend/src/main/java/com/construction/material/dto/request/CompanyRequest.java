package com.construction.material.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Payload for the Super Admin manually creating a company + its first admin.
 * No license is created here - the Super Admin generates one separately via
 * LicenseController once the company exists.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String adminUsername;

    @NotBlank
    @Email
    private String adminEmail;

    @NotBlank
    private String adminPassword;

    private String adminFirstName;
    private String adminLastName;
    private String adminPhone;
    private String preferredLanguage;
}
