package com.construction.material.service.impl;

import com.construction.material.dto.request.OrderItemRequest;
import com.construction.material.dto.request.OrderRequest;
import com.construction.material.dto.response.OrderItemResponse;
import com.construction.material.dto.response.OrderResponse;
import com.construction.material.entity.Material;
import com.construction.material.entity.Order;
import com.construction.material.entity.OrderItem;
import com.construction.material.entity.Project;
import com.construction.material.entity.Stock;
import com.construction.material.entity.StockMovement;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.MaterialRepository;
import com.construction.material.repository.OrderRepository;
import com.construction.material.repository.ProjectRepository;
import com.construction.material.repository.StockMovementRepository;
import com.construction.material.repository.StockRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.ProjectContext;
import com.construction.material.security.TenantContext;
import com.construction.material.service.OrderService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

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
    private MessageSource messageSource;

    @Override
    public OrderResponse create(OrderRequest request) {
        Project project = loadAccessibleProject(request.getProjectId());

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .project(project)
                .supplier(request.getSupplier())
                .orderDate(request.getOrderDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .notes(request.getNotes())
                .requestedBy(currentUser())
                .build();

        applyItems(order, request.getItems());
        order.calculateTotalAmount();

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = loadAccessible(id);
        requireStatus(order, Order.OrderStatus.PENDING);

        Project project = loadAccessibleProject(request.getProjectId());
        order.setProject(project);
        order.setSupplier(request.getSupplier());
        order.setOrderDate(request.getOrderDate());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setNotes(request.getNotes());

        order.getItems().clear();
        applyItems(order, request.getItems());
        order.calculateTotalAmount();

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse findById(Long id) {
        return toResponse(loadAccessible(id));
    }

    @Override
    public Page<OrderResponse> findAllPaginated(Long projectId, Order.OrderStatus status, Pageable pageable) {
        Specification<Order> spec = buildSpecification(projectId, status);
        return orderRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public void delete(Long id) {
        Order order = loadAccessible(id);
        requireStatus(order, Order.OrderStatus.PENDING);
        orderRepository.delete(order);
    }

    @Override
    public OrderResponse approve(Long id) {
        Order order = loadAccessible(id);
        requireStatus(order, Order.OrderStatus.PENDING);
        order.setStatus(Order.OrderStatus.APPROVED);
        order.setApprovedBy(currentUser());
        order.setApprovedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse receive(Long id) {
        Order order = loadAccessible(id);
        requireStatus(order, Order.OrderStatus.APPROVED);

        User performer = currentUser();
        for (OrderItem item : order.getItems()) {
            Stock stock = stockRepository.findByProjectIdAndMaterialId(order.getProject().getId(), item.getMaterial().getId())
                    .orElseGet(() -> Stock.builder()
                            .project(order.getProject())
                            .material(item.getMaterial())
                            .currentQuantity(BigDecimal.ZERO)
                            .build());

            BigDecimal before = stock.getCurrentQuantity() != null ? stock.getCurrentQuantity() : BigDecimal.ZERO;
            stock.addQuantity(item.getOrderedQuantity());
            stock = stockRepository.save(stock);

            item.setReceivedQuantity(item.getOrderedQuantity());

            StockMovement movement = StockMovement.builder()
                    .stock(stock)
                    .type(StockMovement.MovementType.IN)
                    .quantity(item.getOrderedQuantity())
                    .quantityBefore(before)
                    .quantityAfter(stock.getCurrentQuantity())
                    .reference(order.getOrderNumber())
                    .reason("Réception de commande")
                    .order(order)
                    .performedBy(performer)
                    .build();
            stockMovementRepository.save(movement);
        }

        order.setStatus(Order.OrderStatus.RECEIVED);
        order.setActualDeliveryDate(LocalDate.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse cancel(Long id) {
        Order order = loadAccessible(id);
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.APPROVED) {
            throw new BusinessException(msg("order.status.invalid.transition"));
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    // --- helpers -------------------------------------------------------

    /** order_number is NOT NULL, so it must be ready before the first save - no DB-generated id to lean on yet. */
    private String generateOrderNumber() {
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "CMD-" + System.currentTimeMillis() + "-" + random;
    }

    private void applyItems(Order order, List<OrderItemRequest> itemRequests) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            Material material = loadAccessibleMaterial(itemRequest.getMaterialId());
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .material(material)
                    .orderedQuantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .notes(itemRequest.getNotes())
                    .build();
            item.calculateTotalPrice();
            items.add(item);
        }
        order.setItems(items);
    }

    private void requireStatus(Order order, Order.OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new BusinessException(msg("order.status.invalid.transition"));
        }
    }

    private Specification<Order> buildSpecification(Long projectId, Order.OrderStatus status) {
        Long companyId = TenantContext.get();
        Long scopedProjectId = ProjectContext.get();
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) {
                predicates.add(cb.equal(root.get("project").get("company").get("id"), companyId));
            }
            if (scopedProjectId != null) {
                // A project-scoped user can never see another project - this overrides any client-supplied projectId.
                predicates.add(cb.equal(root.get("project").get("id"), scopedProjectId));
            } else if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Order loadAccessible(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(msg("order.not.found")));
        if (!belongsToCurrentTenant(order.getProject())) {
            throw new ResourceNotFoundException(msg("order.not.found"));
        }
        return order;
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
        boolean sameCompany = companyId == null || (project.getCompany() != null && companyId.equals(project.getCompany().getId()));
        if (!sameCompany) {
            return false;
        }
        Long scopedProjectId = ProjectContext.get();
        return scopedProjectId == null || scopedProjectId.equals(project.getId());
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getOrderNumber())
                .projectId(order.getProject().getId())
                .projectName(order.getProject().getName())
                .supplier(order.getSupplier())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .items(items)
                .createdBy(order.getRequestedBy() != null ? order.getRequestedBy().getFullName() : null)
                .approvedBy(order.getApprovedBy() != null ? order.getApprovedBy().getFullName() : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .materialId(item.getMaterial().getId())
                .materialName(item.getMaterial().getName())
                .materialCode(item.getMaterial().getCode())
                .quantity(item.getOrderedQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
