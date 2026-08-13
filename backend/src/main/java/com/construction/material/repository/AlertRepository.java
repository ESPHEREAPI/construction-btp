package com.construction.material.repository;

import com.construction.material.entity.Alert;
import com.construction.material.entity.Alert.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByActiveTrue();
    List<Alert> findByAcknowledgedFalse();
    List<Alert> findByProjectId(Long projectId);
    List<Alert> findByType(AlertType type);
    long countByActiveTrueAndAcknowledgedFalse();
}
