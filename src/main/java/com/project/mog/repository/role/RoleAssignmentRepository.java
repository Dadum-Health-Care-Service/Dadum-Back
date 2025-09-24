package com.project.mog.repository.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, Long> {
    @Query(nativeQuery = true,value="SELECT * FROM ROLEASSIGNMENT WHERE ROLEASSIGNMENT.USERSID=?1")
    RoleAssignmentEntity findByUsersId(Long usersId);
}
