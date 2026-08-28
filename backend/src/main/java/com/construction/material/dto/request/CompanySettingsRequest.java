package com.construction.material.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySettingsRequest {
    @NotBlank
    private String currency;
}
