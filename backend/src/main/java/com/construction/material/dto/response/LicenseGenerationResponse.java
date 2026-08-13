package com.construction.material.dto.response;

import lombok.*;

/** Returned exactly once, at generation time - the only response that ever carries the plaintext key. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseGenerationResponse {
    private LicenseResponse license;
    private String licenseKey;
}
