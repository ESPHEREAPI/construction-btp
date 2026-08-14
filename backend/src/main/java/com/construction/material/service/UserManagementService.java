package com.construction.material.service;

import com.construction.material.dto.request.CreateUserRequest;
import com.construction.material.dto.request.UpdateUserRequest;
import com.construction.material.dto.response.PasswordResetResponse;
import com.construction.material.dto.response.RoleResponse;
import com.construction.material.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserManagementService {

    /** Roles a Company Admin/Admin may assign to a user (everything except Super Admin). */
    List<RoleResponse> listAssignableRoles();

    /**
     * Lists users for the caller's own company (Company Admin/Admin), or for an
     * explicit companyId when the caller is the Super Admin.
     */
    Page<UserResponse> findUsers(Long companyIdParam, Pageable pageable);

    UserResponse findById(Long id);

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);

    UserResponse toggleStatus(Long id);

    PasswordResetResponse resetPassword(Long id);
}
