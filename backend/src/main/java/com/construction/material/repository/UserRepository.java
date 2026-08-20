package com.construction.material.repository;

import com.construction.material.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    long countByCompanyId(Long companyId);

    Page<User> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") Long roleId);

    /** Users of this company still holding the old global (pre-migration) copy of an operational role. */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.company.id = :companyId AND r.name = :roleName AND r.company IS NULL")
    List<User> findByCompanyIdAndGlobalRoleName(@Param("companyId") Long companyId, @Param("roleName") String roleName);
}
