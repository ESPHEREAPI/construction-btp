package com.construction.material.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a global platform key/value setting (e.g. self-registration toggle)
 */
@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100, name = "setting_key")
    private String key;

    @Column(nullable = false, length = 500, name = "setting_value")
    private String value;

    public static final String SELF_REGISTRATION_ENABLED = "self_registration_enabled";
}
