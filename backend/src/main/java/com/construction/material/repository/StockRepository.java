package com.construction.material.repository;

import com.construction.material.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {
    List<Stock> findByProjectId(Long projectId);
    List<Stock> findByMaterialId(Long materialId);
    Optional<Stock> findByProjectIdAndMaterialId(Long projectId, Long materialId);
    List<Stock> findByLowStockAlertTrue();
}
