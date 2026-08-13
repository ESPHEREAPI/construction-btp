package com.construction.material.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Public self-registration payload: creates a new Company and its first
 * user, who becomes that company's COMPANY_ADMIN with a default TRIAL license.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min = 2, max = 200)
    private String companyName;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    private String firstName;
    private String lastName;
    private String phone;
    private String preferredLanguage;
}
