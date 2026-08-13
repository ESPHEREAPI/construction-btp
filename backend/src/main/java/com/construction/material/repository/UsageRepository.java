package com.construction.material.repository;

import com.construction.material.entity.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageRepository extends JpaRepository<Usage, Long>, JpaSpecificationExecutor<Usage> {
    List<Usage> findByProjectId(Long projectId);
    List<Usage> findByMaterialId(Long materialId);
    List<Usage> findByProjectIdAndMaterialId(Long projectId, Long materialId);
    List<Usage> findByUsageDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(u.quantity) FROM Usage u WHERE u.project.id = :projectId AND u.material.id = :materialId")
    BigDecimal sumQuantityByProjectAndMaterial(Long projectId, Long materialId);
}
