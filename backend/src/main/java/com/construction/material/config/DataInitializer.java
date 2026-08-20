package com.construction.material.config;

import com.construction.material.entity.Company;
import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.LicensePlan;
import com.construction.material.entity.Material;
import com.construction.material.entity.Permission;
import com.construction.material.entity.Role;
import com.construction.material.entity.SystemSetting;
import com.construction.material.entity.User;
import com.construction.material.repository.CompanyRepository;
import com.construction.material.repository.LicensePlanRepository;
import com.construction.material.repository.MaterialRepository;
import com.construction.material.repository.PermissionRepository;
import com.construction.material.repository.RoleRepository;
import com.construction.material.repository.SystemSettingRepository;
import com.construction.material.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bootstraps the data this application cannot start meaningfully without:
 * permissions, roles, the TRIAL/PAYANT license plan definitions, the
 * self-registration toggle and the platform's first Super Admin account.
 * There is no database/init_database.sql in this project, so this runs
 * in its place on every startup and is a no-op once seeded.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String SUPER_ADMIN_USERNAME = "superadmin";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final LicensePlanRepository licensePlanRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * No default here on purpose: this repo is public, so a hardcoded initial
     * password would let anyone race the real admin's first login on a fresh
     * deployment. Local dev gets its value from application-local.properties
     * (gitignored); every other environment must set SUPERADMIN_INITIAL_PASSWORD.
     */
    @Value("${app.superadmin.initial-password}")
    private String superAdminInitialPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissionsAndRoles();
        backfillRolePermissions();
        migrateOperationalRolesToPerCompany();
        seedLicensePlans();
        backfillLicensePlanDefaultModules();
        seedSelfRegistrationSetting();
        seedSuperAdmin();
        seedSystemMaterials();
    }

    private static final Role.RoleName[] SYSTEM_ROLES = {
            Role.RoleName.ROLE_SUPER_ADMIN, Role.RoleName.ROLE_COMPANY_ADMIN, Role.RoleName.ROLE_ADMIN
    };

    private void seedPermissionsAndRoles() {
        if (roleRepository.count() > 0) {
            return;
        }

        for (Permission.PermissionName name : Permission.PermissionName.values()) {
            permissionRepository.save(Permission.builder()
                    .name(name.name())
                    .category(name.name().split("_")[0])
                    .build());
        }
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        for (Role.RoleName name : SYSTEM_ROLES) {
            roleRepository.save(Role.builder()
                    .name(name.name())
                    .description(name.name())
                    .systemRole(true)
                    .permissions(new HashSet<>(allPermissions))
                    .build());
        }

        log.info("Seeded {} permissions and {} system roles", allPermissions.size(), SYSTEM_ROLES.length);
    }

    /**
     * seedPermissionsAndRoles() only runs once (roleRepository.count() > 0 guard), so a
     * deployment seeded before systemRole existed would otherwise be stuck without it.
     * Super Admin/Company Admin/Administrateur are fixed and never user-editable, so
     * unconditionally reapplying full access to them on every boot is safe and
     * self-healing - same pattern as backfillLicensePlanDefaultModules() below. The 4
     * operational roles are no longer touched here: they are per-company and editable
     * (see migrateOperationalRolesToPerCompany()).
     */
    private void backfillRolePermissions() {
        Set<Permission> all = new HashSet<>(permissionRepository.findAll());

        for (Role.RoleName name : SYSTEM_ROLES) {
            Role role = roleRepository.findByNameAndCompanyIsNull(name.name()).orElse(null);
            if (role == null) {
                continue;
            }
            if (!Boolean.TRUE.equals(role.getSystemRole())) {
                role.setSystemRole(true);
            }
            Set<String> currentNames = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
            Set<String> targetNames = all.stream().map(Permission::getName).collect(Collectors.toSet());
            if (!currentNames.equals(targetNames)) {
                role.setPermissions(new HashSet<>(all));
            }
            roleRepository.save(role);
        }
    }

    /**
     * One-time-per-company migration: gives every company its own editable copy of the 4
     * operational roles (previously global rows shared/force-synced across all companies),
     * then moves each affected user from the old global role to the new company-owned
     * clone. Unconditional and idempotent - safe to run on every boot, same pattern as
     * backfillLicensePlanDefaultModules(). Old global rows are left in place (orphaned,
     * inert) rather than deleted.
     */
    private void migrateOperationalRolesToPerCompany() {
        Map<String, Permission> permissionsByName = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        for (Company company : companyRepository.findAll()) {
            for (Role.RoleName roleName : DefaultRolePermissions.OPERATIONAL_ROLES) {
                if (roleRepository.existsByNameAndCompanyId(roleName.name(), company.getId())) {
                    continue;
                }

                DefaultRolePermissions.Labels labels = DefaultRolePermissions.labelsFor(roleName);
                Set<Permission> permissions = DefaultRolePermissions.permissionsFor(roleName).stream()
                        .map(pn -> permissionsByName.get(pn.name()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                Role clone = roleRepository.save(Role.builder()
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

                List<User> usersOnGlobalRole = userRepository.findByCompanyIdAndGlobalRoleName(company.getId(), roleName.name());
                for (User user : usersOnGlobalRole) {
                    user.getRoles().removeIf(r -> roleName.name().equals(r.getName()) && r.getCompany() == null);
                    user.getRoles().add(clone);
                    userRepository.save(user);
                }

                log.info("Migrated role {} to company {} ({} users re-pointed)", roleName, company.getId(), usersOnGlobalRole.size());
            }
        }
    }

    private void seedLicensePlans() {
        if (licensePlanRepository.count() > 0) {
            return;
        }

        Set<LicenseModule> allModules = Set.of(LicenseModule.values());

        licensePlanRepository.save(LicensePlan.builder()
                .type(LicensePlan.LicenseType.TRIAL)
                .durationDays(30)
                .maxUsers(5)
                .maxProjects(3)
                .defaultModules(new HashSet<>(allModules))
                .build());

        licensePlanRepository.save(LicensePlan.builder()
                .type(LicensePlan.LicenseType.PAYANT)
                .durationDays(null)
                .maxUsers(1000)
                .maxProjects(1000)
                .defaultModules(new HashSet<>(allModules))
                .build());

        log.info("Seeded default TRIAL and PAYANT license plans");
    }

    /**
     * Plans seeded before the module catalog existed would otherwise carry an
     * empty defaultModules set, which would make issueTrialLicense() grant zero
     * modules to every new self-registered company. Fill full access as the default.
     */
    private void backfillLicensePlanDefaultModules() {
        Set<LicenseModule> allModules = Set.of(LicenseModule.values());
        licensePlanRepository.findAll().forEach(plan -> {
            if (plan.getDefaultModules() == null || plan.getDefaultModules().isEmpty()) {
                plan.setDefaultModules(new HashSet<>(allModules));
                licensePlanRepository.save(plan);
                log.info("Backfilled defaultModules for license plan {}", plan.getType());
            }
        });
    }

    private void seedSelfRegistrationSetting() {
        if (systemSettingRepository.findByKey(SystemSetting.SELF_REGISTRATION_ENABLED).isPresent()) {
            return;
        }
        systemSettingRepository.save(SystemSetting.builder()
                .key(SystemSetting.SELF_REGISTRATION_ENABLED)
                .value("true")
                .build());
        log.info("Seeded self-registration setting (enabled by default)");
    }

    private void seedSuperAdmin() {
        if (userRepository.existsByUsername(SUPER_ADMIN_USERNAME)) {
            return;
        }

        Role superAdminRole = roleRepository.findByName(Role.RoleName.ROLE_SUPER_ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN was not seeded"));

        User superAdmin = User.builder()
                .username(SUPER_ADMIN_USERNAME)
                .email("superadmin@platform.local")
                .password(passwordEncoder.encode(superAdminInitialPassword))
                .firstName("Super")
                .lastName("Admin")
                .active(true)
                .mustChangePassword(true)
                .preferredLanguage("fr")
                .company(null)
                .roles(new HashSet<>(Set.of(superAdminRole)))
                .build();
        userRepository.save(superAdmin);

        log.warn("Seeded Super Admin account -> username: '{}' (password from app.superadmin.initial-password, must be changed on first login)",
                SUPER_ADMIN_USERNAME);
    }

    /**
     * System materials (company = null) are the shared base catalog visible to every
     * company. Companies can still add their own materials, which stay private to them.
     */
    private void seedSystemMaterials() {
        if (materialRepository.count() > 0) {
            return;
        }

        record Seed(String code, String name, Material.Unit unit, String category, java.math.BigDecimal minimumStock) {}

        List<Seed> seeds = List.of(
                new Seed("CIM-001", "Ciment", Material.Unit.BAG, "Liants", new java.math.BigDecimal("50")),
                new Seed("FER-001", "Fer à béton", Material.Unit.TON, "Métallurgie", new java.math.BigDecimal("2")),
                new Seed("SAB-001", "Sable", Material.Unit.M3, "Granulats", new java.math.BigDecimal("20")),
                new Seed("GRA-001", "Gravier", Material.Unit.M3, "Granulats", new java.math.BigDecimal("20")),
                new Seed("BRI-001", "Briques", Material.Unit.PIECE, "Maçonnerie", new java.math.BigDecimal("500")),
                new Seed("PEI-001", "Peinture", Material.Unit.LITER, "Finition", new java.math.BigDecimal("30")),
                new Seed("TUI-001", "Tuiles", Material.Unit.PIECE, "Toiture", new java.math.BigDecimal("200")),
                new Seed("BOI-001", "Bois", Material.Unit.M3, "Charpente", new java.math.BigDecimal("5"))
        );

        for (Seed seed : seeds) {
            materialRepository.save(Material.builder()
                    .code(seed.code())
                    .name(seed.name())
                    .unit(seed.unit())
                    .category(seed.category())
                    .minimumStock(seed.minimumStock())
                    .active(true)
                    .company(null)
                    .build());
        }

        log.info("Seeded {} system materials (visible to every company)", seeds.size());
    }
}
