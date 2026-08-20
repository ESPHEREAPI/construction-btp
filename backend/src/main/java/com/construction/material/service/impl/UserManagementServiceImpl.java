package com.construction.material.service.impl;

import com.construction.material.dto.request.CreateUserRequest;
import com.construction.material.dto.request.UpdateUserRequest;
import com.construction.material.dto.response.PasswordResetResponse;
import com.construction.material.dto.response.PermissionResponse;
import com.construction.material.dto.response.RoleResponse;
import com.construction.material.dto.response.UserResponse;
import com.construction.material.entity.Permission;
import com.construction.material.entity.Project;
import com.construction.material.entity.Role;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.ProjectRepository;
import com.construction.material.repository.RoleRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.TenantContext;
import com.construction.material.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private static final String SUPER_ADMIN_AUTHORITY = "ROLE_SUPER_ADMIN";
    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 14;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

    @Override
    public List<RoleResponse> listAssignableRoles() {
        Long companyId = isSuperAdmin() ? null : requireCompanyId();
        return roleRepository.findAll().stream()
                .filter(r -> !SUPER_ADMIN_AUTHORITY.equals(r.getName()))
                .filter(r -> Boolean.TRUE.equals(r.getSystemRole())
                        || (r.getCompany() != null && r.getCompany().getId().equals(companyId)))
                .map(this::toRoleResponse)
                .sorted(Comparator.comparing(RoleResponse::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> findUsers(Long companyIdParam, Pageable pageable) {
        Long companyId = isSuperAdmin() ? companyIdParam : requireCompanyId();
        if (companyId == null) {
            throw new BusinessException(msg("company.not.found"));
        }
        return userRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
    }

    @Override
    public UserResponse findById(Long id) {
        return toResponse(loadAccessible(id));
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(msg("user.username.exists"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(msg("user.email.exists"));
        }

        Long companyId = requireCompanyId();
        Set<Role> roles = resolveAssignableRoles(request.getRoleIds());
        Project assignedProject = resolveAssignedProject(roles, request.getAssignedProjectId(), companyId);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .active(true)
                .mustChangePassword(true)
                .company(buildCompanyRef(companyId))
                .roles(roles)
                .assignedProject(assignedProject)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = loadAccessible(id);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(msg("user.email.exists"));
        }

        Set<Role> roles = resolveAssignableRoles(request.getRoleIds());
        Long companyId = user.getCompany() != null ? user.getCompany().getId() : requireCompanyId();

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRoles(roles);
        user.setAssignedProject(resolveAssignedProject(roles, request.getAssignedProjectId(), companyId));

        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        User user = loadAccessible(id);
        if (user.getUsername().equals(currentUsername())) {
            throw new BusinessException(msg("user.self.delete.forbidden"));
        }
        userRepository.delete(user);
    }

    @Override
    public UserResponse toggleStatus(Long id) {
        User user = loadAccessible(id);
        if (Boolean.TRUE.equals(user.getActive()) && user.getUsername().equals(currentUsername())) {
            throw new BusinessException(msg("user.self.deactivate.forbidden"));
        }
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        return toResponse(userRepository.save(user));
    }

    @Override
    public PasswordResetResponse resetPassword(Long id) {
        User user = loadAccessible(id);
        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        return PasswordResetResponse.builder().tempPassword(tempPassword).build();
    }

    // --- helpers -----------------------------------------------------------

    /** Loads a user by id, enforcing that a non-Super-Admin caller only reaches their own company's users. */
    private User loadAccessible(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(msg("user.not.found")));
        if (!isSuperAdmin()) {
            Long companyId = requireCompanyId();
            if (user.getCompany() == null || !companyId.equals(user.getCompany().getId())) {
                throw new BusinessException(msg("user.access.denied"));
            }
        }
        return user;
    }

    private Set<Role> resolveAssignableRoles(Set<Long> roleIds) {
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(msg("role.not.found"));
        }
        boolean assignsSuperAdmin = roles.stream()
                .anyMatch(r -> SUPER_ADMIN_AUTHORITY.equals(r.getName()));
        if (assignsSuperAdmin) {
            throw new BusinessException(msg("user.role.super.admin.forbidden"));
        }
        Long companyId = requireCompanyId();
        boolean assignsUnownedRole = roles.stream()
                .anyMatch(r -> !Boolean.TRUE.equals(r.getSystemRole())
                        && (r.getCompany() == null || !companyId.equals(r.getCompany().getId())));
        if (assignsUnownedRole) {
            throw new BusinessException(msg("role.not.found"));
        }
        return new HashSet<>(roles);
    }

    private static final Set<String> ADMIN_TIER_ROLES = Set.of(
            "ROLE_SUPER_ADMIN", "ROLE_COMPANY_ADMIN", "ROLE_ADMIN");

    /** Admin-tier roles are never project-restricted; any assignedProjectId sent for them is ignored. */
    private Project resolveAssignedProject(Set<Role> roles, Long assignedProjectId, Long companyId) {
        boolean isAdminTier = roles.stream().anyMatch(r -> ADMIN_TIER_ROLES.contains(r.getName()));
        if (isAdminTier || assignedProjectId == null) {
            return null;
        }
        Project project = projectRepository.findById(assignedProjectId)
                .orElseThrow(() -> new ResourceNotFoundException(msg("project.not.found")));
        if (project.getCompany() == null || !companyId.equals(project.getCompany().getId())) {
            throw new ResourceNotFoundException(msg("project.not.found"));
        }
        return project;
    }

    private com.construction.material.entity.Company buildCompanyRef(Long companyId) {
        com.construction.material.entity.Company company = new com.construction.material.entity.Company();
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

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(SUPER_ADMIN_AUTHORITY::equals);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private UserResponse toResponse(User user) {
        List<RoleResponse> roleResponses = user.getRoles().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .active(user.getActive())
                .roles(roleResponses)
                .permissions(permissions)
                .preferredLanguage(user.getPreferredLanguage())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .assignedProjectId(user.getAssignedProject() != null ? user.getAssignedProject().getId() : null)
                .assignedProjectName(user.getAssignedProject() != null ? user.getAssignedProject().getName() : null)
                .build();
    }

    private RoleResponse toRoleResponse(Role role) {
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
