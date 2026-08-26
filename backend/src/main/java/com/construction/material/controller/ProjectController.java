package com.construction.material.controller;

import com.construction.material.dto.request.ProjectRequest;
import com.construction.material.dto.response.MessageResponse;
import com.construction.material.dto.response.ProjectActivityResponse;
import com.construction.material.dto.response.ProjectResponse;
import com.construction.material.entity.LicenseModule;
import com.construction.material.security.ModuleAccessGuard;
import com.construction.material.service.AuditLogService;
import com.construction.material.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Projects", description = "Project Management API")
@RestController
@RequestMapping("/api/projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ModuleAccessGuard moduleAccessGuard;

    @Autowired
    private AuditLogService auditLogService;

    @Operation(summary = "Create a new project")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_CREATE', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return new ResponseEntity<>(projectService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a project")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROJECT_UPDATE', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @Operation(summary = "Get project by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROJECT_READ', 'ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SITE_MANAGER')")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return ResponseEntity.ok(projectService.findById(id));
    }

    @Operation(summary = "Get all projects")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_READ', 'ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<List<ProjectResponse>> getAll() {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return ResponseEntity.ok(projectService.findAll());
    }

    @Operation(summary = "Get paginated projects")
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyAuthority('PROJECT_READ', 'ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<Page<ProjectResponse>> getAllPaginated(Pageable pageable) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return ResponseEntity.ok(projectService.findAllPaginated(pageable));
    }

    @Operation(summary = "Delete a project")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROJECT_DELETE', 'ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        projectService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Project deleted successfully"));
    }

    @Operation(summary = "Get key-validation activity history for a project")
    @GetMapping("/{id}/activity")
    @PreAuthorize("hasAnyAuthority('PROJECT_READ', 'ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SITE_MANAGER')")
    public ResponseEntity<Page<ProjectActivityResponse>> getActivity(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return ResponseEntity.ok(auditLogService.findProjectActivity(id, PageRequest.of(page, size)));
    }

    @Operation(summary = "Get projects by status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('PROJECT_READ', 'ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<List<ProjectResponse>> getByStatus(@PathVariable String status) {
        moduleAccessGuard.require(LicenseModule.PROJECTS);
        return ResponseEntity.ok(projectService.findByStatus(status));
    }
}
