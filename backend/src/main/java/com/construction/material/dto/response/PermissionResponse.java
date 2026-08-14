package com.construction.material.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
}
