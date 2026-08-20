package com.construction.material.service;

import com.construction.material.dto.request.RoleRequest;
import com.construction.material.dto.response.PermissionResponse;
import com.construction.material.dto.response.RoleResponse;

import java.util.List;

public interface RoleManagementService {

    /** Full permission catalogue, grouped by category client-side - used to build the role edit form. */
    List<PermissionResponse> listAllPermissions();

    RoleResponse findById(Long id);

    RoleResponse create(RoleRequest request);

    RoleResponse update(Long id, RoleRequest request);

    void delete(Long id);
}
