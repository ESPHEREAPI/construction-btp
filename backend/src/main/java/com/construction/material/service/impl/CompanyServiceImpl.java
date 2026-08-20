package com.construction.material.service.impl;

import com.construction.material.config.DefaultRolePermissions;
import com.construction.material.dto.request.CreateCompanyWithAdminRequest;
import com.construction.material.dto.response.CompanyResponse;
import com.construction.material.entity.Company;
import com.construction.material.entity.License;
import com.construction.material.entity.Permission;
import com.construction.material.entity.Role;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.CompanyRepository;
import com.construction.material.repository.LicenseRepository;
import com.construction.material.repository.PermissionRepository;
import com.construction.material.repository.ProjectRepository;
import com.construction.material.repository.RoleRepository;
import com.construction.material.repository.UserRepository;
import com.construction.material.service.CompanyService;
import com.construction.material.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

    @Override
    public CompanyResponse createCompanyWithAdmin(CreateCompanyWithAdminRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(messageSource.getMessage("user.username.exists", null, LocaleContextHolder.getLocale()));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(messageSource.getMessage("user.email.exists", null, LocaleContextHolder.getLocale()));
        }
        if (companyRepository.existsByName(request.getCompanyName())) {
            throw new BusinessException(messageSource.getMessage("company.code.exists", null, LocaleContextHolder.getLocale()));
        }

        Company company = Company.builder()
                .name(request.getCompanyName())
                .code(generateUniqueCode(request.getCompanyName()))
                .active(true)
                .selfRegistered(request.isSelfRegistered())
                .build();
        company = companyRepository.save(company);

        if (request.isSelfRegistered()) {
            licenseService.issueTrialLicense(company);
        }
        // else: Super-Admin-initiated creation - no License row yet, generated separately.

        cloneOperationalRoles(company);

        Role adminRole = roleRepository.findByName("ROLE_COMPANY_ADMIN")
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("role.not.found", null, LocaleContextHolder.getLocale())));

        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "fr")
                .active(true)
                .mustChangePassword(true)
                .company(company)
                .roles(new HashSet<>(List.of(adminRole)))
                .build();
        userRepository.save(admin);

        return toResponse(company);
    }

    @Override
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompanyResponse setActive(Long companyId, boolean active) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("company.not.found", null, LocaleContextHolder.getLocale())));
        company.setActive(active);
        companyRepository.save(company);
        return toResponse(company);
    }

    /**
     * Gives a newly created company its own editable copy of the 4 operational roles,
     * seeded with the default permission matrix - the company's admin can then change
     * them freely through the Role management screen without ever needing a code change.
     * Mirrors DataInitializer.migrateOperationalRolesToPerCompany() for existing companies.
     */
    private void cloneOperationalRoles(Company company) {
        Map<String, Permission> permissionsByName = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        for (Role.RoleName roleName : DefaultRolePermissions.OPERATIONAL_ROLES) {
            DefaultRolePermissions.Labels labels = DefaultRolePermissions.labelsFor(roleName);
            Set<Permission> permissions = DefaultRolePermissions.permissionsFor(roleName).stream()
                    .map(pn -> permissionsByName.get(pn.name()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            roleRepository.save(Role.builder()
                    .name(roleName.name())
                    .description(roleName.name())
                    .nameFr(labels.fr())
                    .nameEn(labels.en())
                    .namePt(labels.pt())
                    .company(company)
                    .systemRole(false)
                    .custom(false)
                    .permissions(permissions)
                    .build());
        }
    }

    private String generateUniqueCode(String companyName) {
        String base = Normalizer.normalize(companyName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "COMPANY";
        }
        String candidate = base;
        int suffix = 1;
        while (companyRepository.existsByCode(candidate)) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }

    private CompanyResponse toResponse(Company company) {
        License license = licenseRepository.findFirstByCompanyIdOrderByCreatedAtDesc(company.getId()).orElse(null);
        long userCount = userRepository.countByCompanyId(company.getId());
        long projectCount = projectRepository.countByCompanyId(company.getId());

        CompanyResponse.CompanyResponseBuilder builder = CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .code(company.getCode())
                .active(company.getActive())
                .selfRegistered(company.getSelfRegistered())
                .createdAt(company.getCreatedAt())
                .currentUserCount(userCount)
                .currentProjectCount(projectCount);

        if (license != null) {
            builder.licenseType(license.getType())
                    .licenseStatus(license.getStatus())
                    .licenseStartDate(license.getStartDate())
                    .licenseEndDate(license.getEndDate())
                    .maxUsers(license.getMaxUsers())
                    .maxProjects(license.getMaxProjects())
                    .activeModules(license.getActiveModules())
                    .licenseBlocked(license.isBlocked())
                    .licenseKeyPending(license.getStatus() == License.LicenseStatus.GENERATED);
        }

        return builder.build();
    }
}
