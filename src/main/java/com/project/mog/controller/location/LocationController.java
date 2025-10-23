package com.project.mog.controller.location;

import com.project.mog.dto.location.LocationRequest;
import com.project.mog.dto.location.LocationResponse;
import com.project.mog.dto.location.MiddlePointResponse;
import com.project.mog.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LocationController {
    
    private final LocationService locationService;
    
    @PostMapping
    public ResponseEntity<LocationResponse> saveLocation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LocationRequest request) {
        
        try {
            // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
            Long userId = 1L; // 실제로는 JWT 토큰에서 추출
            
            LocationResponse response = locationService.saveLocation(userId, request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("위치 저장 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/middle-point")
    public ResponseEntity<MiddlePointResponse> getMiddlePoint() {
        try {
            MiddlePointResponse response = locationService.getMiddlePoint();
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("중간 지점 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getUserLocations(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
            Long userId = 1L; // 실제로는 JWT 토큰에서 추출
            
            List<LocationResponse> response = locationService.getUserLocations(userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 위치 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
