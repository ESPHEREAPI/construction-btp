package com.construction.material.service.impl;

import com.construction.material.entity.Company;
import com.construction.material.entity.HiddenMaterial;
import com.construction.material.entity.Material;
import com.construction.material.exception.BusinessException;
import com.construction.material.exception.ResourceNotFoundException;
import com.construction.material.repository.CompanyRepository;
import com.construction.material.repository.HiddenMaterialRepository;
import com.construction.material.repository.MaterialRepository;
import com.construction.material.security.TenantContext;
import com.construction.material.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final CompanyRepository companyRepository;
    private final HiddenMaterialRepository hiddenMaterialRepository;
    private final MessageSource messageSource;

    @Override
    public List<Material> findAll() {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return materialRepository.findAll();
        }
        return excludeHidden(materialRepository.findByCompanyIsNullOrCompanyId(companyId), companyId);
    }

    @Override
    public Page<Material> findAll(Pageable pageable) {
        // Company-scoped visibility only applies to the unpaginated listing today; paginated
        // browsing is used from admin/system contexts where the Super Admin sees everything.
        return materialRepository.findAll(pageable);
    }

    @Override
    public Material findById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
        if (!isVisibleToCurrentTenant(material)) {
            throw new ResourceNotFoundException("Material not found with id: " + id);
        }
        return material;
    }

    @Override
    @Transactional
    public Material save(Material material) {
        Long companyId = TenantContext.get();
        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            material.setCompany(company);
        }
        material.setCreatedAt(LocalDateTime.now());
        return materialRepository.save(material);
    }

    @Override
    @Transactional
    public Material update(Long id, Material material) {
        Material existing = findById(id);
        requireOwned(existing, "material.system.readonly");
        existing.setName(material.getName());
        existing.setDescription(material.getDescription());
        existing.setUnit(material.getUnit());
        existing.setCategory(material.getCategory());
        existing.setUnitPrice(material.getUnitPrice());
        existing.setSupplier(material.getSupplier());
        existing.setActive(material.getActive());
        existing.setUpdatedAt(LocalDateTime.now());
        return materialRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Material material = findById(id);
        requireOwned(material, "material.system.cannot.delete");
        materialRepository.delete(material);
    }

    @Override
    @Transactional
    public void hide(Long id) {
        Material material = findById(id);
        Long companyId = TenantContext.get();
        if (companyId == null) {
            throw new BusinessException(msg("material.system.readonly"));
        }
        if (material.getCompany() != null) {
            // A company's own material is removed with delete, not hide.
            throw new BusinessException(msg("material.system.readonly"));
        }
        if (!hiddenMaterialRepository.existsByCompanyIdAndMaterialId(companyId, id)) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            hiddenMaterialRepository.save(HiddenMaterial.builder()
                    .company(company)
                    .material(material)
                    .build());
        }
    }

    @Override
    @Transactional
    public void unhide(Long id) {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return;
        }
        hiddenMaterialRepository.findByCompanyIdAndMaterialId(companyId, id)
                .ifPresent(hiddenMaterialRepository::delete);
    }

    @Override
    public List<Material> findHidden() {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return List.of();
        }
        return hiddenMaterialRepository.findByCompanyId(companyId).stream()
                .map(HiddenMaterial::getMaterial)
                .collect(Collectors.toList());
    }

    @Override
    public List<Material> findByCategory(String category) {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return materialRepository.findByCategory(category);
        }
        return excludeHidden(materialRepository.findByCompanyIsNullOrCompanyIdAndCategory(companyId, category), companyId);
    }

    @Override
    public List<Material> findActive() {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return materialRepository.findByActiveTrue();
        }
        return excludeHidden(materialRepository.findByCompanyIsNullOrCompanyIdAndActiveTrue(companyId), companyId);
    }

    @Override
    public List<String> findCategories() {
        Long companyId = TenantContext.get();
        return companyId != null
                ? materialRepository.findDistinctCategoriesByCompany(companyId)
                : materialRepository.findDistinctCategories();
    }

    @Override
    public List<String> findSuppliers() {
        Long companyId = TenantContext.get();
        return companyId != null
                ? materialRepository.findDistinctSuppliersByCompany(companyId)
                : materialRepository.findDistinctSuppliers();
    }

    /** Super Admin (no company) sees everything; a company user sees system materials plus its own. */
    private boolean isVisibleToCurrentTenant(Material material) {
        Long companyId = TenantContext.get();
        return companyId == null
                || material.getCompany() == null
                || companyId.equals(material.getCompany().getId());
    }

    /** Write access (update/delete) is restricted to a company's own materials - the shared catalog is read/hide-only. */
    private void requireOwned(Material material, String messageKey) {
        Long companyId = TenantContext.get();
        if (companyId == null) {
            return;
        }
        boolean owned = material.getCompany() != null && companyId.equals(material.getCompany().getId());
        if (!owned) {
            throw new BusinessException(msg(messageKey));
        }
    }

    private List<Material> excludeHidden(List<Material> materials, Long companyId) {
        Set<Long> hiddenIds = Set.copyOf(hiddenMaterialRepository.findHiddenMaterialIds(companyId));
        if (hiddenIds.isEmpty()) {
            return materials;
        }
        return materials.stream()
                .filter(m -> !hiddenIds.contains(m.getId()))
                .collect(Collectors.toList());
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
