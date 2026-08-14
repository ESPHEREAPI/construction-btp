package com.construction.material.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank
    @Email
    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    @NotEmpty
    private Set<Long> roleIds;
}
