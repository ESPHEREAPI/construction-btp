package com.construction.material.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank
    private String nameFr;

    private String nameEn;

    private String namePt;

    private Set<Long> permissionIds;
}
