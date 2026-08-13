package com.construction.material.repository;

import com.construction.material.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    /** Admin views: the most recent row for a company, regardless of status. */
    Optional<License> findFirstByCompanyIdOrderByCreatedAtDesc(Long companyId);

    /** Enforcement/quota checks: the most recent row with a specific status (typically ACTIVE). */
    Optional<License> findFirstByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, License.LicenseStatus status);

    /** Full history, most recent first. */
    List<License> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
