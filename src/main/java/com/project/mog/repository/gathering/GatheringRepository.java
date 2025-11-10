package com.project.mog.repository.gathering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatheringRepository extends JpaRepository<GatheringEntity, Long> {
    
    // 활성 모임만 조회
    @Query("SELECT g FROM GatheringEntity g WHERE g.status = 'ACTIVE' ORDER BY g.createdAt DESC")
    List<GatheringEntity> findActiveGatherings();
    
    // 카테고리별 모임 조회
    @Query("SELECT g FROM GatheringEntity g WHERE g.status = 'ACTIVE' AND g.category = :category ORDER BY g.createdAt DESC")
    List<GatheringEntity> findActiveGatheringsByCategory(@Param("category") String category);
    
    // 사용자가 생성한 모임 조회
    @Query("SELECT g FROM GatheringEntity g WHERE g.creator.usersId = :userId ORDER BY g.createdAt DESC")
    List<GatheringEntity> findByCreatorId(@Param("userId") Long userId);
    
    // 사용자가 참여한 모임 조회
    @Query("SELECT g FROM GatheringEntity g " +
           "JOIN g.participants p " +
           "WHERE p.user.usersId = :userId AND g.status = 'ACTIVE' " +
           "ORDER BY p.joinedAt DESC")
    List<GatheringEntity> findParticipatedGatherings(@Param("userId") Long userId);
    
    // 지역 기반 모임 조회 (위도/경도 범위)
    @Query("SELECT g FROM GatheringEntity g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.latitude BETWEEN :minLat AND :maxLat " +
           "AND g.longitude BETWEEN :minLng AND :maxLng " +
           "ORDER BY g.createdAt DESC")
    List<GatheringEntity> findGatheringsByLocation(
        @Param("minLat") Double minLat, 
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng, 
        @Param("maxLng") Double maxLng
    );
}
