package com.project.mog.repository.gathering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GatheringParticipantRepository extends JpaRepository<GatheringParticipantEntity, Long> {
    
    // 특정 모임의 참여자 목록 조회
    @Query("SELECT p FROM GatheringParticipantEntity p WHERE p.gathering.gatheringId = :gatheringId ORDER BY p.joinedAt ASC")
    List<GatheringParticipantEntity> findByGatheringId(@Param("gatheringId") Long gatheringId);
    
    // 사용자가 특정 모임에 참여했는지 확인
    @Query("SELECT p FROM GatheringParticipantEntity p WHERE p.gathering.gatheringId = :gatheringId AND p.user.usersId = :userId")
    Optional<GatheringParticipantEntity> findByGatheringIdAndUserId(@Param("gatheringId") Long gatheringId, @Param("userId") Long userId);
    
    // 사용자가 참여한 모든 모임 조회
    @Query("SELECT p FROM GatheringParticipantEntity p WHERE p.user.usersId = :userId ORDER BY p.joinedAt DESC")
    List<GatheringParticipantEntity> findByUserId(@Param("userId") Long userId);
    
    // 특정 모임의 참여자 수 조회
    @Query("SELECT COUNT(p) FROM GatheringParticipantEntity p WHERE p.gathering.gatheringId = :gatheringId")
    Long countByGatheringId(@Param("gatheringId") Long gatheringId);
    
    // 사용자가 참여한 모임 수 조회
    @Query("SELECT COUNT(p) FROM GatheringParticipantEntity p WHERE p.user.usersId = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
