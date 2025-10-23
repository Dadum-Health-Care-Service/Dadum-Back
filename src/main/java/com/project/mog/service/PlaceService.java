package com.project.mog.service;

import com.project.mog.dto.place.PlaceRequest;
import com.project.mog.dto.place.PlaceResponse;
import com.project.mog.repository.place.PlaceEntity;
import com.project.mog.repository.place.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {
    
    private final PlaceRepository placeRepository;
    
    @Transactional(readOnly = true)
    public List<PlaceResponse> recommendPlaces(PlaceRequest request) {
        log.info("장소 추천 요청: keyword={}, lat={}, lng={}", 
                request.getKeyword(), request.getLatitude(), request.getLongitude());
        
        List<PlaceEntity> places;
        
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadius() != null) {
            // 반경 내 장소 검색
            places = placeRepository.findPlacesNearby(
                    request.getLatitude(), 
                    request.getLongitude(), 
                    request.getRadius()
            );
        } else {
            // 키워드로 검색
            places = placeRepository.findActivePlacesByKeyword(request.getKeyword());
        }
        
        return places.stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    @Transactional
    public PlaceResponse savePlace(PlaceRequest request) {
        log.info("장소 저장 요청: placeName={}, keyword={}", 
                request.getKeyword(), request.getKeyword());
        
        // 기존 장소가 있는지 확인
        Optional<PlaceEntity> existingPlace = placeRepository.findByPlaceUrlAndIsActiveTrue(
                request.getKeyword() // 임시로 keyword를 placeUrl로 사용
        );
        
        if (existingPlace.isPresent()) {
            return convertToResponse(existingPlace.get());
        }
        
        PlaceEntity place = PlaceEntity.builder()
                .placeName(request.getKeyword())
                .keyword(request.getKeyword())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .build();
        
        PlaceEntity savedPlace = placeRepository.save(place);
        
        return convertToResponse(savedPlace);
    }
    
    private PlaceResponse convertToResponse(PlaceEntity entity) {
        return PlaceResponse.builder()
                .placeId(entity.getPlaceId())
                .placeName(entity.getPlaceName())
                .categoryName(entity.getCategoryName())
                .addressName(entity.getAddressName())
                .roadAddressName(entity.getRoadAddressName())
                .phone(entity.getPhone())
                .placeUrl(entity.getPlaceUrl())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .keyword(entity.getKeyword())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
