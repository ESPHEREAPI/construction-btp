package com.construction.material.repository;

import com.construction.material.entity.Project;

public interface ProjectRepositoryExtensions {
    long countByStatus(Project.ProjectStatus status);
}
