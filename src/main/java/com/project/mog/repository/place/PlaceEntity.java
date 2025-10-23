package com.project.mog.repository.place;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PLACES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(
    name = "SEQ_PLACE_GENERATOR",
    sequenceName = "SEQ_PLACE",
    allocationSize = 1,
    initialValue = 1
)
public class PlaceEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PLACE_GENERATOR")
    @Column(name = "PLACE_ID")
    private Long placeId;
    
    @Column(name = "PLACE_NAME", nullable = false, length = 200)
    private String placeName;
    
    @Column(name = "CATEGORY_NAME", length = 100)
    private String categoryName;
    
    @Column(name = "ADDRESS_NAME", length = 500)
    private String addressName;
    
    @Column(name = "ROAD_ADDRESS_NAME", length = 500)
    private String roadAddressName;
    
    @Column(name = "PHONE", length = 50)
    private String phone;
    
    @Column(name = "PLACE_URL", length = 1000)
    private String placeUrl;
    
    @Column(name = "LATITUDE", nullable = false)
    private Double latitude;
    
    @Column(name = "LONGITUDE", nullable = false)
    private Double longitude;
    
    @Column(name = "KEYWORD", length = 100)
    private String keyword;
    
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
