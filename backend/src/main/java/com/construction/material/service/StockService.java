package com.construction.material.service;

import com.construction.material.dto.request.StockMovementRequest;
import com.construction.material.dto.request.StockThresholdsRequest;
import com.construction.material.dto.response.StockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockService {

    Page<StockResponse> findAllPaginated(Long projectId, Pageable pageable);

    StockResponse findById(Long id);

    Page<StockResponse> findByProjectPaginated(Long projectId, Pageable pageable);

    List<StockResponse> findLowStockAlerts(Long projectId);

    StockResponse addMovement(StockMovementRequest request);

    StockResponse updateThresholds(Long id, StockThresholdsRequest request);
}
