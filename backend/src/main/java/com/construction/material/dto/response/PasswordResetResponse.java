package com.construction.material.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetResponse {
    private String tempPassword;
}
