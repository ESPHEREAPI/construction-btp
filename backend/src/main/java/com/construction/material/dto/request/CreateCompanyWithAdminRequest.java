package com.construction.material.dto.request;

import lombok.*;

/**
 * Internal parameter object shared by the two ways a Company + its first
 * admin user can be created: public self-registration and manual creation
 * by the Super Admin. Self-registered companies get an immediate TRIAL
 * license; Super-Admin-created companies get none (generated separately).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCompanyWithAdminRequest {
    private String companyName;
    private boolean selfRegistered;

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String preferredLanguage;
}
