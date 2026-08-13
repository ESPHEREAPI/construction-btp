package com.construction.material.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseMineResponse {
    /** The current ACTIVE, non-expired license, if any. */
    private LicenseResponse current;
    /** A GENERATED license waiting to be activated by key, if any. */
    private LicenseResponse pending;
}
