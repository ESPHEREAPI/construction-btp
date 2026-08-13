package com.construction.material.repository;

import com.construction.material.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);
    Boolean existsByCode(String code);
    Boolean existsByName(String name);
}
