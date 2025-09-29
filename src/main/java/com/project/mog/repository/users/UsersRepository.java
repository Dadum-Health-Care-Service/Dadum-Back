package com.project.mog.repository.users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UsersRepository extends JpaRepository<UsersEntity, Long>{

	@Query(nativeQuery = true,value = "SELECT USERS.* FROM USERS JOIN AUTH ON (USERS.USERSID=AUTH.USERSID) WHERE USERS.EMAIL=?1 AND AUTH.PASSWORD=?2")
	Optional<UsersEntity> findByEmailAndPassword(String email, String password);

	
	// 사용자 기본 정보만 조회 (가벼운 조회용)
	@Query(nativeQuery = true,value="SELECT * FROM USERS WHERE USERS.EMAIL=?1")
	Optional<UsersEntity> findByEmail(String email);

	@Query(nativeQuery = true, value="SELECT * FROM USERS WHERE USERS.USERSNAME=?1 AND PHONENUM=?2")
	Optional<UsersEntity> findByUsersNameAndPhoneNum(String usersName, String phoneNum);

	// 사용자 정보와 역할 정보를 함께 조회 (권한 확인 등 필요한 조회용)
	@Query("SELECT DISTINCT u FROM UsersEntity u LEFT JOIN FETCH u.roleAssignments ra LEFT JOIN FETCH ra.role WHERE u.email = ?1")
	Optional<UsersEntity> findByEmailWithRole(String email);
	
	// 사용자 ID로 사용자 정보와 역할 정보를 함께 조회
	@Query("SELECT DISTINCT u FROM UsersEntity u LEFT JOIN FETCH u.roleAssignments ra LEFT JOIN FETCH ra.role WHERE u.usersId = ?1")
	Optional<UsersEntity> findByIdWithRole(Long usersId);
	
}