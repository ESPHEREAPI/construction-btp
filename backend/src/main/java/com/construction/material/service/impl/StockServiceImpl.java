package com.construction.material.service.impl;

import com.construction.material.dto.request.StockMovementRequest;
import com.construction.material.dto.request.StockThresholdsRequest;
import com.construction.material.dto.response.StockResponse;
import com.construction.material.entity.Alert;
import com.construction.material.entity.Stock;
import com.construction.material.entity.StockMovement;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.AlertRepository;
import com.construction.material.repository.StockMovementRepository;
import com.construction.material.repository.StockRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.ProjectContext;
import com.construction.material.security.TenantContext;
import com.construction.material.service.StockService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    public Page<StockResponse> findAllPaginated(Long projectId, Pageable pageable) {
        Specification<Stock> spec = buildSpecification(projectId, false);
        return stockRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public StockResponse findById(Long id) {
        return toResponse(loadAccessible(id));
    }

    @Override
    public Page<StockResponse> findByProjectPaginated(Long projectId, Pageable pageable) {
        Specification<Stock> spec = buildSpecification(projectId, false);
        return stockRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public List<StockResponse> findLowStockAlerts(Long projectId) {
        Specification<Stock> spec = buildSpecification(projectId, true);
        return stockRepository.findAll(spec).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StockResponse addMovement(StockMovementRequest request) {
        Stock stock = loadAccessible(request.getStockId());
        User performer = currentUser();

        if (request.getMovementType() == StockMovement.MovementType.TRANSFER) {
            if (request.getDestinationStockId() == null) {
                throw new BusinessException(msg("stock.not.found"));
            }
            Stock destination = loadAccessible(request.getDestinationStockId());
            requireAvailable(stock, request.getQuantity());

            BigDecimal sourceBefore = stock.getCurrentQuantity();
            boolean sourceWasLow = Boolean.TRUE.equals(stock.getLowStockAlert());
            stock.removeQuantity(request.getQuantity());
            stock = stockRepository.save(stock);
            recordMovement(stock, StockMovement.MovementType.TRANSFER, request.getQuantity(),
                    sourceBefore, stock.getCurrentQuantity(), "Vers stock #" + destination.getId(), request, performer);
            checkLowStockAlertTransition(stock, sourceWasLow);

            BigDecimal destBefore = destination.getCurrentQuantity();
            destination.addQuantity(request.getQuantity());
            destination = stockRepository.save(destination);
            recordMovement(destination, StockMovement.MovementType.TRANSFER, request.getQuantity(),
                    destBefore, destination.getCurrentQuantity(), "Depuis stock #" + stock.getId(), request, performer);

            return toResponse(stock);
        }

        if (request.getMovementType() == StockMovement.MovementType.OUT) {
            requireAvailable(stock, request.getQuantity());
            BigDecimal before = stock.getCurrentQuantity();
            boolean wasLow = Boolean.TRUE.equals(stock.getLowStockAlert());
            stock.removeQuantity(request.getQuantity());
            stock = stockRepository.save(stock);
            recordMovement(stock, request.getMovementType(), request.getQuantity(), before, stock.getCurrentQuantity(), request.getReference(), request, performer);
            checkLowStockAlertTransition(stock, wasLow);
            return toResponse(stock);
        }

        // IN or ADJUSTMENT: additive, unconstrained
        BigDecimal before = stock.getCurrentQuantity();
        stock.addQuantity(request.getQuantity());
        stock = stockRepository.save(stock);
        recordMovement(stock, request.getMovementType(), request.getQuantity(), before, stock.getCurrentQuantity(), request.getReference(), request, performer);
        return toResponse(stock);
    }

    @Override
    public StockResponse updateThresholds(Long id, StockThresholdsRequest request) {
        Stock stock = loadAccessible(id);
        boolean wasLow = Boolean.TRUE.equals(stock.getLowStockAlert());
        stock.setMinimumQuantity(request.getMinimumQuantity());
        stock.setMaximumQuantity(request.getMaximumQuantity());
        stock.calculateAvailableQuantity();
        stock = stockRepository.save(stock);
        checkLowStockAlertTransition(stock, wasLow);
        return toResponse(stock);
    }

    // --- helpers ------------------------------------------------------

    private void requireAvailable(Stock stock, BigDecimal quantity) {
        BigDecimal available = stock.getCurrentQuantity() != null ? stock.getCurrentQuantity() : BigDecimal.ZERO;
        if (stock.getReservedQuantity() != null) {
            available = available.subtract(stock.getReservedQuantity());
        }
        if (available.compareTo(quantity) < 0) {
            throw new BusinessException(msg("stock.insufficient"));
        }
    }

    private void recordMovement(Stock stock, StockMovement.MovementType type, BigDecimal quantity,
                                 BigDecimal before, BigDecimal after, String reference,
                                 StockMovementRequest request, User performer) {
        stockMovementRepository.save(StockMovement.builder()
                .stock(stock)
                .type(type)
                .quantity(quantity)
                .quantityBefore(before)
                .quantityAfter(after)
                .reference(reference)
                .reason(request.getReason())
                .notes(request.getNotes())
                .performedBy(performer)
                .build());
    }

    private void checkLowStockAlert(Stock stock, boolean wasLow) {
        checkLowStockAlertTransition(stock, wasLow);
    }

    private void checkLowStockAlertTransition(Stock stock, boolean wasLow) {
        if (!wasLow && Boolean.TRUE.equals(stock.getLowStockAlert())) {
            alertRepository.save(Alert.builder()
                    .type(Alert.AlertType.LOW_STOCK)
                    .severity(Alert.AlertSeverity.WARNING)
                    .title(msg("alert.stock.low"))
                    .message(messageSource.getMessage("alert.stock.low",
                            new Object[]{stock.getMaterial().getName()}, LocaleContextHolder.getLocale()))
                    .project(stock.getProject())
                    .material(stock.getMaterial())
                    .stock(stock)
                    .build());
        }
    }

    private Specification<Stock> buildSpecification(Long projectId, boolean lowStockOnly) {
        Long companyId = TenantContext.get();
        Long scopedProjectId = ProjectContext.get();
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) {
                predicates.add(cb.equal(root.get("project").get("company").get("id"), companyId));
            }
            if (scopedProjectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), scopedProjectId));
            } else if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (lowStockOnly) {
                predicates.add(cb.isTrue(root.get("lowStockAlert")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Stock loadAccessible(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(msg("stock.not.found")));
        Long companyId = TenantContext.get();
        boolean visible = companyId == null
                || (stock.getProject().getCompany() != null && companyId.equals(stock.getProject().getCompany().getId()));
        if (!visible) {
            throw new ResourceNotFoundException(msg("stock.not.found"));
        }
        Long scopedProjectId = ProjectContext.get();
        if (scopedProjectId != null && !scopedProjectId.equals(stock.getProject().getId())) {
            throw new ResourceNotFoundException(msg("stock.not.found"));
        }
        return stock;
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .projectId(stock.getProject().getId())
                .projectName(stock.getProject().getName())
                .materialId(stock.getMaterial().getId())
                .materialName(stock.getMaterial().getName())
                .materialCode(stock.getMaterial().getCode())
                .currentQuantity(stock.getCurrentQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .minimumQuantity(stock.getMinimumQuantity())
                .maximumQuantity(stock.getMaximumQuantity())
                .location(stock.getLocation())
                .lowStockAlert(stock.getLowStockAlert())
                .unit(stock.getMaterial().getUnit() != null ? stock.getMaterial().getUnit().getSymbol() : null)
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
