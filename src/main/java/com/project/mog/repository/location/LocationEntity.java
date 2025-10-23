package com.project.mog.repository.location;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOCATIONS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(
    name = "SEQ_LOCATION_GENERATOR",
    sequenceName = "SEQ_LOCATION",
    allocationSize = 1,
    initialValue = 1
)
public class LocationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LOCATION_GENERATOR")
    @Column(name = "LOCATION_ID")
    private Long locationId;
    
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    
    @Column(name = "ADDRESS", nullable = false, length = 500)
    private String address;
    
    @Column(name = "LATITUDE", nullable = false)
    private Double latitude;
    
    @Column(name = "LONGITUDE", nullable = false)
    private Double longitude;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
