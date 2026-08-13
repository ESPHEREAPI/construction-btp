package com.construction.material.controller;

import com.construction.material.entity.LicenseModule;
import com.construction.material.entity.Material;
import com.construction.material.security.ModuleAccessGuard;
import com.construction.material.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Materials", description = "Material management APIs")
public class MaterialController {

    private final MaterialService materialService;
    private final ModuleAccessGuard moduleAccessGuard;

    @GetMapping
    @Operation(summary = "Get all materials")
    public ResponseEntity<List<Material>> getAllMaterials() {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.ok(materialService.findAll());
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get paginated materials")
    public ResponseEntity<Page<Material>> getAllMaterialsPaginated(Pageable pageable) {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.ok(materialService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get material by ID")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.ok(materialService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MATERIAL_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create a new material")
    public ResponseEntity<Material> createMaterial(@Valid @RequestBody Material material) {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.save(material));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MATERIAL_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update a material")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @Valid @RequestBody Material material) {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.ok(materialService.update(id, material));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MATERIAL_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete a material")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        materialService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get materials by category")
    public ResponseEntity<List<Material>> getMaterialsByCategory(@PathVariable String category) {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.ok(materialService.findByCategory(category));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active materials")
    public ResponseEntity<List<Material>> getActiveMaterials() {
        moduleAccessGuard.require(LicenseModule.MATERIALS);
        return ResponseEntity.ok(materialService.findActive());
    }
}
