package com.construction.material.service.impl;

import com.construction.material.dto.request.UsageRequest;
import com.construction.material.dto.response.UsageResponse;
import com.construction.material.entity.Alert;
import com.construction.material.entity.Material;
import com.construction.material.entity.Project;
import com.construction.material.entity.Quantification;
import com.construction.material.entity.Stock;
import com.construction.material.entity.StockMovement;
import com.construction.material.entity.Usage;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.AlertRepository;
import com.construction.material.repository.MaterialRepository;
import com.construction.material.repository.ProjectRepository;
import com.construction.material.repository.QuantificationRepository;
import com.construction.material.repository.StockMovementRepository;
import com.construction.material.repository.StockRepository;
import com.construction.material.repository.UsageRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.TenantContext;
import com.construction.material.service.UsageService;
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

@Service
@Transactional
public class UsageServiceImpl implements UsageService {

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private QuantificationRepository quantificationRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    public UsageResponse create(UsageRequest request) {
        Project project = loadAccessibleProject(request.getProjectId());
        Material material = loadAccessibleMaterial(request.getMaterialId());

        Usage usage = Usage.builder()
                .project(project)
                .material(material)
                .quantity(request.getQuantity())
                .usageDate(request.getUsageDate())
                .location(request.getLocation())
                .activity(request.getPurpose())
                .crew(request.getUsedBy())
                .notes(request.getNotes())
                .recordedBy(currentUser())
                .build();

        Usage saved = usageRepository.save(usage);
        applyEffects(saved);
        return toResponse(saved);
    }

    @Override
    public UsageResponse update(Long id, UsageRequest request) {
        Usage usage = loadAccessible(id);
        reverseEffects(usage);

        Project project = loadAccessibleProject(request.getProjectId());
        Material material = loadAccessibleMaterial(request.getMaterialId());

        usage.setProject(project);
        usage.setMaterial(material);
        usage.setQuantity(request.getQuantity());
        usage.setUsageDate(request.getUsageDate());
        usage.setLocation(request.getLocation());
        usage.setActivity(request.getPurpose());
        usage.setCrew(request.getUsedBy());
        usage.setNotes(request.getNotes());

        Usage saved = usageRepository.save(usage);
        applyEffects(saved);
        return toResponse(saved);
    }

    @Override
    public UsageResponse findById(Long id) {
        return toResponse(loadAccessible(id));
    }

    @Override
    public Page<UsageResponse> findAllPaginated(Long projectId, Long materialId, Pageable pageable) {
        Specification<Usage> spec = buildSpecification(projectId, materialId);
        return usageRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public void delete(Long id) {
        Usage usage = loadAccessible(id);
        reverseEffects(usage);
        stockMovementRepository.findByUsageId(id).forEach(movement -> {
            movement.setUsage(null);
            stockMovementRepository.save(movement);
        });
        usageRepository.delete(usage);
    }

    // --- stock / quantification integration -----------------------------

    private void applyEffects(Usage usage) {
        Stock stock = stockRepository.findByProjectIdAndMaterialId(usage.getProject().getId(), usage.getMaterial().getId())
                .orElseThrow(() -> new ResourceNotFoundException(msg("stock.not.found")));

        BigDecimal available = stock.getCurrentQuantity() != null ? stock.getCurrentQuantity() : BigDecimal.ZERO;
        if (stock.getReservedQuantity() != null) {
            available = available.subtract(stock.getReservedQuantity());
        }
        if (available.compareTo(usage.getQuantity()) < 0) {
            throw new BusinessException(msg("stock.insufficient"));
        }

        BigDecimal before = stock.getCurrentQuantity();
        boolean wasLow = Boolean.TRUE.equals(stock.getLowStockAlert());
        stock.removeQuantity(usage.getQuantity());
        stock = stockRepository.save(stock);

        stockMovementRepository.save(StockMovement.builder()
                .stock(stock)
                .type(StockMovement.MovementType.OUT)
                .quantity(usage.getQuantity())
                .quantityBefore(before)
                .quantityAfter(stock.getCurrentQuantity())
                .reason("Utilisation matériau")
                .usage(usage)
                .performedBy(currentUser())
                .build());

        if (!wasLow && Boolean.TRUE.equals(stock.getLowStockAlert())) {
            alertRepository.save(Alert.builder()
                    .type(Alert.AlertType.LOW_STOCK)
                    .severity(Alert.AlertSeverity.WARNING)
                    .title(msg("alert.stock.low"))
                    .message(messageSource.getMessage("alert.stock.low",
                            new Object[]{usage.getMaterial().getName()}, LocaleContextHolder.getLocale()))
                    .project(usage.getProject())
                    .material(usage.getMaterial())
                    .stock(stock)
                    .build());
        }

        quantificationRepository.findByProjectIdAndMaterialId(usage.getProject().getId(), usage.getMaterial().getId())
                .ifPresent(q -> {
                    boolean wasTriggered = Boolean.TRUE.equals(q.getAlertTriggered());
                    BigDecimal used = q.getUsedQuantity() != null ? q.getUsedQuantity() : BigDecimal.ZERO;
                    q.setUsedQuantity(used.add(usage.getQuantity()));
                    q.calculateMetrics();
                    quantificationRepository.save(q);

                    if (!wasTriggered && Boolean.TRUE.equals(q.getAlertTriggered())) {
                        alertRepository.save(Alert.builder()
                                .type(Alert.AlertType.USAGE_EXCEEDED)
                                .severity(Alert.AlertSeverity.WARNING)
                                .title(msg("alert.usage.exceeded"))
                                .message(messageSource.getMessage("alert.usage.exceeded",
                                        new Object[]{usage.getMaterial().getName(), q.getVariancePercentage()},
                                        LocaleContextHolder.getLocale()))
                                .project(usage.getProject())
                                .material(usage.getMaterial())
                                .quantification(q)
                                .build());
                    }
                });
    }

    private void reverseEffects(Usage usage) {
        stockRepository.findByProjectIdAndMaterialId(usage.getProject().getId(), usage.getMaterial().getId())
                .ifPresent(stock -> {
                    BigDecimal before = stock.getCurrentQuantity();
                    stock.addQuantity(usage.getQuantity());
                    Stock saved = stockRepository.save(stock);
                    stockMovementRepository.save(StockMovement.builder()
                            .stock(saved)
                            .type(StockMovement.MovementType.ADJUSTMENT)
                            .quantity(usage.getQuantity())
                            .quantityBefore(before)
                            .quantityAfter(saved.getCurrentQuantity())
                            .reason("Correction suite à modification/suppression d'une utilisation")
                            .performedBy(currentUser())
                            .build());
                });

        quantificationRepository.findByProjectIdAndMaterialId(usage.getProject().getId(), usage.getMaterial().getId())
                .ifPresent(q -> {
                    BigDecimal used = q.getUsedQuantity() != null ? q.getUsedQuantity() : BigDecimal.ZERO;
                    used = used.subtract(usage.getQuantity());
                    if (used.compareTo(BigDecimal.ZERO) < 0) {
                        used = BigDecimal.ZERO;
                    }
                    q.setUsedQuantity(used);
                    q.calculateMetrics();
                    quantificationRepository.save(q);
                });
    }

    // --- helpers ----------------------------------------------------------

    private Specification<Usage> buildSpecification(Long projectId, Long materialId) {
        Long companyId = TenantContext.get();
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) {
                predicates.add(cb.equal(root.get("project").get("company").get("id"), companyId));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (materialId != null) {
                predicates.add(cb.equal(root.get("material").get("id"), materialId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Usage loadAccessible(Long id) {
        Usage usage = usageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(msg("usage.not.found")));
        if (!belongsToCurrentTenant(usage.getProject())) {
            throw new ResourceNotFoundException(msg("usage.not.found"));
        }
        return usage;
    }

    private Project loadAccessibleProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(msg("project.not.found")));
        if (!belongsToCurrentTenant(project)) {
            throw new ResourceNotFoundException(msg("project.not.found"));
        }
        return project;
    }

    private Material loadAccessibleMaterial(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException(msg("material.not.found")));
        Long companyId = TenantContext.get();
        boolean visible = companyId == null
                || material.getCompany() == null
                || companyId.equals(material.getCompany().getId());
        if (!visible) {
            throw new ResourceNotFoundException(msg("material.not.found"));
        }
        return material;
    }

    private boolean belongsToCurrentTenant(Project project) {
        Long companyId = TenantContext.get();
        return companyId == null || (project.getCompany() != null && companyId.equals(project.getCompany().getId()));
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private UsageResponse toResponse(Usage usage) {
        return UsageResponse.builder()
                .id(usage.getId())
                .projectId(usage.getProject().getId())
                .projectName(usage.getProject().getName())
                .materialId(usage.getMaterial().getId())
                .materialName(usage.getMaterial().getName())
                .materialCode(usage.getMaterial().getCode())
                .quantity(usage.getQuantity())
                .unit(usage.getMaterial().getUnit() != null ? usage.getMaterial().getUnit().getSymbol() : null)
                .usageDate(usage.getUsageDate())
                .notes(usage.getNotes())
                .usedBy(usage.getCrew())
                .recordedBy(usage.getRecordedBy() != null ? usage.getRecordedBy().getFullName() : null)
                .createdAt(usage.getCreatedAt())
                .updatedAt(usage.getUpdatedAt())
                .build();
    }
}
