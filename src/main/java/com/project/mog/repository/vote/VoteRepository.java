package com.project.mog.repository.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity, Long> {
    
    Optional<VoteEntity> findByUserIdAndPlaceUrlAndIsActiveTrue(Long userId, String placeUrl);
    
    List<VoteEntity> findByPlaceUrlAndIsActiveTrue(String placeUrl);
    
    @Query("SELECT COUNT(v) FROM VoteEntity v WHERE v.placeUrl = :placeUrl AND v.isUpvote = true AND v.isActive = true")
    Long countUpvotesByPlaceUrl(@Param("placeUrl") String placeUrl);
    
    @Query("SELECT COUNT(v) FROM VoteEntity v WHERE v.placeUrl = :placeUrl AND v.isUpvote = false AND v.isActive = true")
    Long countDownvotesByPlaceUrl(@Param("placeUrl") String placeUrl);
    
    @Query("SELECT v.placeUrl, " +
           "SUM(CASE WHEN v.isUpvote = true THEN 1 ELSE 0 END) as upvotes, " +
           "SUM(CASE WHEN v.isUpvote = false THEN 1 ELSE 0 END) as downvotes " +
           "FROM VoteEntity v WHERE v.placeUrl = :placeUrl AND v.isActive = true " +
           "GROUP BY v.placeUrl")
    Optional<Object[]> getVoteCountsByPlaceUrl(@Param("placeUrl") String placeUrl);
    
    List<VoteEntity> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
}
