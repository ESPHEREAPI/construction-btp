package com.construction.material.repository;

import com.construction.material.entity.Order;
import com.construction.material.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByProjectId(Long projectId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByRequestedById(Long userId);
}
