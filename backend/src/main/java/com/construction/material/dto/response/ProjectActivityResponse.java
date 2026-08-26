package com.construction.material.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectActivityResponse {
    private Long id;
    private String entityType;
    private String action;
    private Long entityId;
    private String description;
    private String performedBy;
    private LocalDateTime timestamp;
}
