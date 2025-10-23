package com.project.mog.controller.place;

import com.project.mog.dto.place.PlaceRequest;
import com.project.mog.dto.place.PlaceResponse;
import com.project.mog.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PlaceController {
    
    private final PlaceService placeService;
    
    @PostMapping("/recommend")
    public ResponseEntity<List<PlaceResponse>> recommendPlaces(@RequestBody PlaceRequest request) {
        try {
            log.info("장소 추천 요청: {}", request);
            
            List<PlaceResponse> response = placeService.recommendPlaces(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("장소 추천 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<PlaceResponse> savePlace(@RequestBody PlaceRequest request) {
        try {
            log.info("장소 저장 요청: {}", request);
            
            PlaceResponse response = placeService.savePlace(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("장소 저장 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
