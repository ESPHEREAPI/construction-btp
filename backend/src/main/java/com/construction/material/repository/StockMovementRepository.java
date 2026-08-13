package com.construction.material.repository;

import com.construction.material.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByStockId(Long stockId);
    List<StockMovement> findByStockIdOrderByMovementDateDesc(Long stockId);
    List<StockMovement> findByOrderId(Long orderId);
    List<StockMovement> findByUsageId(Long usageId);
}
