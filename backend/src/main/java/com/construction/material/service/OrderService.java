package com.construction.material.service;

import com.construction.material.dto.request.OrderRequest;
import com.construction.material.dto.response.OrderResponse;
import com.construction.material.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse update(Long id, OrderRequest request);

    OrderResponse findById(Long id);

    Page<OrderResponse> findAllPaginated(Long projectId, Order.OrderStatus status, Pageable pageable);

    void delete(Long id);

    OrderResponse approve(Long id);

    OrderResponse receive(Long id);

    OrderResponse cancel(Long id);
}
