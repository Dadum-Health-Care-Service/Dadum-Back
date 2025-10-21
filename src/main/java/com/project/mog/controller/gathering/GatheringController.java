package com.project.mog.controller.gathering;

import com.project.mog.dto.gathering.*;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.GatheringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gatherings")
@Tag(name = "모임 관리", description = "모임 관련 API")
public class GatheringController {
    
    @Autowired
    private GatheringService gatheringService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGathering(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestBody GatheringCreateRequest request) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createErrorResponse("인증이 필요합니다."));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            GatheringResponse gathering = gatheringService.createGathering(request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모임이 성공적으로 생성되었습니다.");
            response.put("gathering", gathering);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "모임 목록 조회", description = "활성 모임 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllGatherings(
            @RequestParam(required = false) String category) {
        
        try {
            List<GatheringResponse> gatherings;
            
            if (category != null && !category.isEmpty() && !"all".equals(category)) {
                gatherings = gatheringService.getGatheringsByCategory(category);
            } else {
                gatherings = gatheringService.getAllActiveGatherings();
            }
            
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gatherings", gatherings);
            response.put("totalCount", gatherings.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("모임 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }
    
    
    @Operation(summary = "모임 상세 조회", description = "특정 모임의 상세 정보를 조회합니다.")
    @GetMapping("/{gatheringId}")
    public ResponseEntity<Map<String, Object>> getGatheringById(
            @PathVariable Long gatheringId) {
        
        try {
            GatheringResponse gathering = gatheringService.getGatheringById(gatheringId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gathering", gathering);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(404).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "모임 참여", description = "특정 모임에 참여합니다.")
    @PostMapping("/{gatheringId}/join")
    public ResponseEntity<Map<String, Object>> joinGathering(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long gatheringId) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createErrorResponse("인증이 필요합니다."));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            gatheringService.joinGathering(gatheringId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모임에 성공적으로 참여했습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "모임 나가기", description = "특정 모임에서 나갑니다.")
    @PostMapping("/{gatheringId}/leave")
    public ResponseEntity<Map<String, Object>> leaveGathering(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long gatheringId) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createErrorResponse("인증이 필요합니다."));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            gatheringService.leaveGathering(gatheringId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모임에서 성공적으로 나갔습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "모임 참여자 목록 조회", description = "특정 모임의 참여자 목록을 조회합니다.")
    @GetMapping("/{gatheringId}/participants")
    public ResponseEntity<Map<String, Object>> getGatheringParticipants(
            @PathVariable Long gatheringId) {
        
        try {
            List<ParticipantResponse> participants = gatheringService.getGatheringParticipants(gatheringId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("participants", participants);
            response.put("totalCount", participants.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(404).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "모임 참여 여부 확인", description = "사용자가 특정 모임에 참여했는지 확인합니다.")
    @GetMapping("/{gatheringId}/participants/check")
    public ResponseEntity<Map<String, Object>> checkParticipation(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long gatheringId) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createErrorResponse("인증이 필요합니다."));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            boolean isParticipant = gatheringService.isParticipant(gatheringId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isParticipant", isParticipant);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "모임 삭제", description = "모임을 삭제합니다. (생성자만 가능)")
    @DeleteMapping("/{gatheringId}")
    public ResponseEntity<Map<String, Object>> deleteGathering(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long gatheringId) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createErrorResponse("인증이 필요합니다."));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            gatheringService.deleteGathering(gatheringId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모임이 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "참여한 모임 조회", description = "사용자가 참여한 모임 목록을 조회합니다.")
    @GetMapping("/participated")
    public ResponseEntity<Map<String, Object>> getParticipatedGatherings(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createErrorResponse("인증이 필요합니다."));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            List<GatheringResponse> participatedGatherings = gatheringService.getParticipatedGatherings(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gatherings", participatedGatherings);
            response.put("totalCount", participatedGatherings.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }

    @Operation(summary = "모임 참여자 수 동기화", description = "모든 모임의 참여자 수를 실제 참여자 수로 동기화합니다.")
    @PostMapping("/sync-participants")
    public ResponseEntity<Map<String, Object>> syncAllGatheringParticipants() {
        try {
            gatheringService.syncAllGatheringParticipants();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모든 모임의 참여자 수가 동기화되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Operation(summary = "기존 모임 일정 업데이트", description = "기존 모임들의 nextMeetingDate를 업데이트합니다.")
    @PostMapping("/update-schedules")
    public ResponseEntity<Map<String, Object>> updateExistingGatheringsSchedule() {
        try {
            gatheringService.updateExistingGatheringsSchedule();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "기존 모임들의 일정이 업데이트되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }

    @Operation(summary = "매일 일정 업데이트", description = "매주/매월 모임의 다음 일정을 수동으로 업데이트합니다.")
    @PostMapping("/update-daily-schedules")
    public ResponseEntity<Map<String, Object>> updateDailySchedules() {
        try {
            // ScheduleUpdateService를 직접 호출하는 대신, GatheringService에 메서드 추가
            gatheringService.updateDailySchedules();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "매일 일정이 업데이트되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}
