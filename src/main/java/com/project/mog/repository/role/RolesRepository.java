package com.project.mog.repository.role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<RolesEntity, Long> {
    Optional<RolesEntity> findByRoleName(String roleName);
}
