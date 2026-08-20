package com.construction.material.service.impl;

import com.construction.material.dto.request.RoleRequest;
import com.construction.material.dto.response.PermissionResponse;
import com.construction.material.dto.response.RoleResponse;
import com.construction.material.entity.Company;
import com.construction.material.entity.Permission;
import com.construction.material.entity.Role;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.PermissionRepository;
import com.construction.material.repository.RoleRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.TenantContext;
import com.construction.material.service.RoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages a company's own roles: the 4 operational roles cloned at company
 * creation (editable but not deletable/renamable) and any fully custom roles
 * the company creates on top (editable and deletable). System roles (Super
 * Admin/Company Admin/Admin) never appear here.
 */
@Service
@Transactional
public class RoleManagementServiceImpl implements RoleManagementService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    public List<PermissionResponse> listAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .category(p.getCategory())
                        .build())
                .sorted(Comparator.comparing(PermissionResponse::getName))
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse findById(Long id) {
        return toResponse(loadOwned(id));
    }

    @Override
    public RoleResponse create(RoleRequest request) {
        Long companyId = requireCompanyId();

        Role role = Role.builder()
                .name("ROLE_CUSTOM_PENDING_" + System.nanoTime())
                .description(request.getNameFr())
                .nameFr(request.getNameFr())
                .nameEn(request.getNameEn())
                .namePt(request.getNamePt())
                .company(buildCompanyRef(companyId))
                .systemRole(false)
                .custom(true)
                .permissions(resolvePermissions(request.getPermissionIds()))
                .build();
        role = roleRepository.save(role);

        role.setName("ROLE_CUSTOM_" + role.getId());
        role = roleRepository.save(role);

        return toResponse(role);
    }

    @Override
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = loadOwned(id);
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BusinessException(msg("role.system.readonly"));
        }

        role.setNameFr(request.getNameFr());
        role.setNameEn(request.getNameEn());
        role.setNamePt(request.getNamePt());
        role.setDescription(request.getNameFr());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));

        return toResponse(roleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        Role role = loadOwned(id);
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BusinessException(msg("role.system.readonly"));
        }
        if (!Boolean.TRUE.equals(role.getCustom())) {
            throw new BusinessException(msg("role.delete.not.custom"));
        }
        long usersOnRole = userRepository.countByRoleId(id);
        if (usersOnRole > 0) {
            throw new BusinessException(messageSource.getMessage(
                    "role.delete.still.assigned", new Object[]{usersOnRole}, LocaleContextHolder.getLocale()));
        }
        roleRepository.delete(role);
    }

    // --- helpers -----------------------------------------------------------

    /** Loads a role by id, enforcing that it belongs to the caller's own company. */
    private Role loadOwned(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(msg("role.not.found")));
        Long companyId = requireCompanyId();
        if (role.getCompany() == null || !companyId.equals(role.getCompany().getId())) {
            throw new ResourceNotFoundException(msg("role.not.found"));
        }
        return role;
    }

    private Set<Permission> resolvePermissions(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BusinessException(msg("role.not.found"));
        }
        return new HashSet<>(permissions);
    }

    private Company buildCompanyRef(Long companyId) {
        Company company = new Company();
        company.setId(companyId);
        return company;
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            throw new IllegalStateException("This account is not attached to any company");
        }
        return companyId;
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .category(p.getCategory())
                        .build())
                .sorted(Comparator.comparing(PermissionResponse::getName))
                .collect(Collectors.toList());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .nameFr(role.getNameFr())
                .nameEn(role.getNameEn())
                .namePt(role.getNamePt())
                .systemRole(role.getSystemRole())
                .custom(role.getCustom())
                .permissions(permissions)
                .build();
    }
}
