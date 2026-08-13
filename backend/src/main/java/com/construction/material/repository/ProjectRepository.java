package com.construction.material.repository;

import com.construction.material.entity.Project;
import com.construction.material.entity.Project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Optional<Project> findByCode(String code);
    Boolean existsByCode(String code);
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByProjectManagerId(Long projectManagerId);
    List<Project> findBySiteManagerId(Long siteManagerId);
    long countByCompanyId(Long companyId);
    long countByStatus(ProjectStatus status);
    long countByCompanyIdAndStatus(Long companyId, ProjectStatus status);
    List<Project> findByCompanyId(Long companyId);
    Page<Project> findByCompanyId(Long companyId, Pageable pageable);
    List<Project> findByCompanyIdAndStatus(Long companyId, ProjectStatus status);
}
