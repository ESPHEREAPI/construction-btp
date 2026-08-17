package com.construction.material.repository;

import com.construction.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT m.category FROM Material m WHERE m.category IS NOT NULL AND m.category <> '' "
            + "AND (m.company IS NULL OR m.company.id = :companyId) ORDER BY m.category")
    List<String> findDistinctCategoriesByCompany(@Param("companyId") Long companyId);

    @Query("SELECT DISTINCT m.category FROM Material m WHERE m.category IS NOT NULL AND m.category <> '' ORDER BY m.category")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT m.supplier FROM Material m WHERE m.supplier IS NOT NULL AND m.supplier <> '' "
            + "AND (m.company IS NULL OR m.company.id = :companyId) ORDER BY m.supplier")
    List<String> findDistinctSuppliersByCompany(@Param("companyId") Long companyId);

    @Query("SELECT DISTINCT m.supplier FROM Material m WHERE m.supplier IS NOT NULL AND m.supplier <> '' ORDER BY m.supplier")
    List<String> findDistinctSuppliers();
}
