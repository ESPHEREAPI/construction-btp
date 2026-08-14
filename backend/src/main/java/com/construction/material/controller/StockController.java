package com.construction.material.controller;

import com.construction.material.dto.request.StockMovementRequest;
import com.construction.material.dto.request.StockThresholdsRequest;
import com.construction.material.dto.response.StockResponse;
import com.construction.material.entity.LicenseModule;
import com.construction.material.security.ModuleAccessGuard;
import com.construction.material.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stock", description = "Stock levels and movements API")
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyAuthority('STOCK_READ', 'ROLE_ADMIN')")
public class StockController {

    private final StockService stockService;
    private final ModuleAccessGuard moduleAccessGuard;

    @GetMapping
    @Operation(summary = "List stock levels, optionally filtered by project")
    public ResponseEntity<Page<StockResponse>> getAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        moduleAccessGuard.require(LicenseModule.STOCK);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(stockService.findAllPaginated(projectId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a stock line by id")
    public ResponseEntity<StockResponse> getById(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.STOCK);
        return ResponseEntity.ok(stockService.findById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "List stock levels for a project")
    public ResponseEntity<Page<StockResponse>> getByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        moduleAccessGuard.require(LicenseModule.STOCK);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(stockService.findByProjectPaginated(projectId, pageable));
    }

    @GetMapping("/alerts")
    @Operation(summary = "List all stock lines currently below their minimum threshold")
    public ResponseEntity<List<StockResponse>> getAlerts() {
        moduleAccessGuard.require(LicenseModule.STOCK);
        return ResponseEntity.ok(stockService.findLowStockAlerts(null));
    }

    @GetMapping("/alerts/{projectId}")
    @Operation(summary = "List low-stock lines for a project")
    public ResponseEntity<List<StockResponse>> getAlertsForProject(@PathVariable Long projectId) {
        moduleAccessGuard.require(LicenseModule.STOCK);
        return ResponseEntity.ok(stockService.findLowStockAlerts(projectId));
    }

    @PostMapping("/movement")
    @PreAuthorize("hasAnyAuthority('STOCK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Record a stock movement (in/out/transfer/adjustment)")
    public ResponseEntity<StockResponse> addMovement(@Valid @RequestBody StockMovementRequest request) {
        moduleAccessGuard.require(LicenseModule.STOCK);
        return ResponseEntity.ok(stockService.addMovement(request));
    }

    @PutMapping("/{id}/thresholds")
    @PreAuthorize("hasAnyAuthority('STOCK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update the minimum/maximum thresholds of a stock line")
    public ResponseEntity<StockResponse> updateThresholds(@PathVariable Long id, @RequestBody StockThresholdsRequest request) {
        moduleAccessGuard.require(LicenseModule.STOCK);
        return ResponseEntity.ok(stockService.updateThresholds(id, request));
    }
}
