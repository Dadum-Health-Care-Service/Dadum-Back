package com.project.mog.repository.healthConnect;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthConnectRepository extends JpaRepository<HealthConnectEntity,Long>{
	
	List<HealthConnectEntity> findByUser_usersId(long userId);
	
	@Query(nativeQuery = true, value = "SELECT * FROM health_connect WHERE usersId=?1 ORDER BY healthdataid DESC LIMIT 1")
	Optional<HealthConnectEntity> findLatestByUsersId(Long usersId);
	
}
