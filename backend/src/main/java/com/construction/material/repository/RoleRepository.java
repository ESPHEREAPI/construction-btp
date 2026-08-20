package com.construction.material.repository;

import com.construction.material.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Boolean existsByName(String name);

    Optional<Role> findByNameAndCompanyIsNull(String name);
    Boolean existsByNameAndCompanyId(String name, Long companyId);
}
