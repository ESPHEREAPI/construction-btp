package com.construction.material.repository;

import com.construction.material.entity.HiddenMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HiddenMaterialRepository extends JpaRepository<HiddenMaterial, Long> {

    Optional<HiddenMaterial> findByCompanyIdAndMaterialId(Long companyId, Long materialId);

    boolean existsByCompanyIdAndMaterialId(Long companyId, Long materialId);

    @Query("SELECT h.material.id FROM HiddenMaterial h WHERE h.company.id = :companyId")
    List<Long> findHiddenMaterialIds(@Param("companyId") Long companyId);

    List<HiddenMaterial> findByCompanyId(Long companyId);
}
