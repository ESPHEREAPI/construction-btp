package com.construction.material.repository;

import com.construction.material.entity.Order;
import com.construction.material.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByProjectId(Long projectId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByRequestedById(Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.project.id = :projectId AND o.status = :status")
    BigDecimal sumTotalAmountByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.project.id = :projectId AND o.status IN :statuses")
    BigDecimal sumTotalAmountByProjectIdAndStatusIn(@Param("projectId") Long projectId, @Param("statuses") Collection<OrderStatus> statuses);

    long countByProjectIdAndStatusIn(Long projectId, Collection<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.project.id = :projectId AND o.status IN :statuses AND o.orderDate < :cutoff")
    long countStaleOrders(@Param("projectId") Long projectId, @Param("statuses") Collection<OrderStatus> statuses, @Param("cutoff") LocalDate cutoff);
}
