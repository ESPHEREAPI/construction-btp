package com.construction.material.repository;

import com.construction.material.entity.LicensePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicensePlanRepository extends JpaRepository<LicensePlan, Long> {
    Optional<LicensePlan> findByType(LicensePlan.LicenseType type);
}
