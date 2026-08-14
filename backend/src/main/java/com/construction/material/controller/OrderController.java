package com.construction.material.controller;

import com.construction.material.dto.request.OrderRequest;
import com.construction.material.dto.response.OrderResponse;
import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.Order;
import com.construction.material.security.ModuleAccessGuard;
import com.construction.material.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "Material order management API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final ModuleAccessGuard moduleAccessGuard;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ORDER_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update an order (only while PENDING)")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get an order by id")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ROLE_ADMIN')")
    @Operation(summary = "List orders, optionally filtered by project or status")
    public ResponseEntity<Page<OrderResponse>> getAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.findAllPaginated(projectId, status, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ORDER_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete an order (only while PENDING)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ORDER_APPROVE', 'ROLE_ADMIN')")
    @Operation(summary = "Approve a pending order")
    public ResponseEntity<OrderResponse> approve(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        return ResponseEntity.ok(orderService.approve(id));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Mark an approved order as received and credit stock")
    public ResponseEntity<OrderResponse> receive(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        return ResponseEntity.ok(orderService.receive(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Cancel a pending or approved order")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.ORDERS);
        return ResponseEntity.ok(orderService.cancel(id));
    }
}
