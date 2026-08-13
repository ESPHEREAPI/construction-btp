package com.construction.material.service;

import com.construction.material.dto.request.ProjectRequest;
import com.construction.material.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProjectService {
    ProjectResponse create(ProjectRequest request);
    ProjectResponse update(Long id, ProjectRequest request);
    ProjectResponse findById(Long id);
    List<ProjectResponse> findAll();
    Page<ProjectResponse> findAllPaginated(Pageable pageable);
    void delete(Long id);
    List<ProjectResponse> findByStatus(String status);
}
