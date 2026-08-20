package com.construction.material.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private String nameFr;
    private String nameEn;
    private String namePt;
    private Boolean systemRole;
    private Boolean custom;
    private List<PermissionResponse> permissions;
}
