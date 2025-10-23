package com.project.mog.repository.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    
    List<LocationEntity> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT l FROM LocationEntity l WHERE l.isActive = true")
    List<LocationEntity> findAllActiveLocations();
    
    @Query("SELECT AVG(l.latitude) as avgLat, AVG(l.longitude) as avgLng FROM LocationEntity l WHERE l.isActive = true")
    Optional<Object[]> findAverageCoordinates();
    
    Optional<LocationEntity> findByLocationIdAndIsActiveTrue(Long locationId);
}
