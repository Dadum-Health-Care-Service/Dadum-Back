package com.project.mog.repository.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, Long> {
    @Query(nativeQuery = true,value="SELECT * FROM ROLEASSIGNMENT WHERE ROLEASSIGNMENT.USERSID=?1")
    List<RoleAssignmentEntity> findByUsersId(Long usersId);

    @Modifying
    @Query(nativeQuery = true,value="DELETE FROM ROLEASSIGNMENT WHERE ROLEASSIGNMENT.USERSID=?1 AND ROLEASSIGNMENT.ASSIGNMENTID=?2")
    void deleteByUserIdAndAssignmentId(Long userId, Long assignmentId);
}
