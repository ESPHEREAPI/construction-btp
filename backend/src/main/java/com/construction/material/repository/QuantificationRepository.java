package com.construction.material.repository;

import com.construction.material.entity.Quantification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuantificationRepository extends JpaRepository<Quantification, Long> {
    List<Quantification> findByProjectId(Long projectId);
    List<Quantification> findByMaterialId(Long materialId);
    Optional<Quantification> findByProjectIdAndMaterialId(Long projectId, Long materialId);
    List<Quantification> findByAlertTriggeredTrue();
    
    @Query("SELECT q FROM Quantification q WHERE q.project.id = :projectId AND q.alertEnabled = true AND q.variancePercentage >= q.alertThreshold")
    List<Quantification> findAlertsForProject(Long projectId);
}
