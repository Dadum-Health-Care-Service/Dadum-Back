package com.project.mog.service;

import com.project.mog.dto.location.LocationRequest;
import com.project.mog.dto.location.LocationResponse;
import com.project.mog.dto.location.MiddlePointResponse;
import com.project.mog.repository.location.LocationEntity;
import com.project.mog.repository.location.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    
    private final LocationRepository locationRepository;
    
    @Transactional
    public LocationResponse saveLocation(Long userId, LocationRequest request) {
        log.info("위치 저장 요청: userId={}, address={}", userId, request.getAddress());
        
        LocationEntity location = LocationEntity.builder()
                .userId(userId)
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .build();
        
        LocationEntity savedLocation = locationRepository.save(location);
        
        return LocationResponse.builder()
                .locationId(savedLocation.getLocationId())
                .userId(savedLocation.getUserId())
                .address(savedLocation.getAddress())
                .latitude(savedLocation.getLatitude())
                .longitude(savedLocation.getLongitude())
                .createdAt(savedLocation.getCreatedAt())
                .build();
    }
    
    @Transactional(readOnly = true)
    public MiddlePointResponse getMiddlePoint() {
        log.info("중간 지점 조회 요청");
        
        List<LocationEntity> activeLocations = locationRepository.findAllActiveLocations();
        
        if (activeLocations.isEmpty()) {
            return MiddlePointResponse.builder()
                    .latitude(37.5665) // 서울시청 기본값
                    .longitude(126.9780)
                    .address("서울특별시 중구 세종대로 110")
                    .totalLocations(0)
                    .build();
        }
        
        double avgLat = activeLocations.stream()
                .mapToDouble(LocationEntity::getLatitude)
                .average()
                .orElse(37.5665);
        
        double avgLng = activeLocations.stream()
                .mapToDouble(LocationEntity::getLongitude)
                .average()
                .orElse(126.9780);
        
        return MiddlePointResponse.builder()
                .latitude(avgLat)
                .longitude(avgLng)
                .address("계산된 중간 지점")
                .totalLocations(activeLocations.size())
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<LocationResponse> getUserLocations(Long userId) {
        log.info("사용자 위치 목록 조회: userId={}", userId);
        
        List<LocationEntity> locations = locationRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        
        return locations.stream()
                .map(location -> LocationResponse.builder()
                        .locationId(location.getLocationId())
                        .userId(location.getUserId())
                        .address(location.getAddress())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .createdAt(location.getCreatedAt())
                        .build())
                .toList();
    }
}
