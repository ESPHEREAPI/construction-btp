package com.construction.material.repository;

import com.construction.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {
    Optional<Material> findByCode(String code);
    Boolean existsByCode(String code);
    List<Material> findByActiveTrue();
    List<Material> findByCategory(String category);

    /** System materials (company IS NULL, visible to everyone) plus this company's own materials. */
    List<Material> findByCompanyIsNullOrCompanyId(Long companyId);
    List<Material> findByCompanyIsNullOrCompanyIdAndActiveTrue(Long companyId);
    List<Material> findByCompanyIsNullOrCompanyIdAndCategory(Long companyId, String category);
    long countByCompanyIsNullOrCompanyId(Long companyId);
}
