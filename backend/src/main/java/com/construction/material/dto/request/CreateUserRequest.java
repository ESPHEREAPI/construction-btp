package com.construction.material.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String firstName;

    private String lastName;

    private String phone;

    @NotEmpty
    private Set<Long> roleIds;

    /** Ignored for admin-tier roles (Super Admin/Company Admin/Admin), who are never project-restricted. */
    private Long assignedProjectId;
}
