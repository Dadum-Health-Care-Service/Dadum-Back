package com.project.mog.repository.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {
    
    List<PlaceEntity> findByKeywordAndIsActiveTrueOrderByCreatedAtDesc(String keyword);
    
    Optional<PlaceEntity> findByPlaceUrlAndIsActiveTrue(String placeUrl);
    
    @Query("SELECT p FROM PlaceEntity p WHERE p.isActive = true AND " +
           "(:keyword IS NULL OR p.keyword = :keyword) " +
           "ORDER BY p.createdAt DESC")
    List<PlaceEntity> findActivePlacesByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM PlaceEntity p WHERE p.isActive = true " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(p.latitude)))) <= :radius " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(p.latitude))))")
    List<PlaceEntity> findPlacesNearby(@Param("lat") Double latitude, 
                                      @Param("lng") Double longitude, 
                                      @Param("radius") Double radiusKm);
}
