package com.project.mog.controller.vote;

import com.project.mog.dto.vote.VoteCountResponse;
import com.project.mog.dto.vote.VoteRequest;
import com.project.mog.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/places/vote")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VoteController {
    
    private final VoteService voteService;
    
    @PostMapping
    public ResponseEntity<Void> vote(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VoteRequest request) {
        
        try {
            // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
            Long userId = 1L; // 실제로는 JWT 토큰에서 추출
            
            voteService.vote(userId, request);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("투표 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/count/{placeUrl}")
    public ResponseEntity<VoteCountResponse> getVoteCounts(@PathVariable String placeUrl) {
        try {
            log.info("투표 수 조회: placeUrl={}", placeUrl);
            
            VoteCountResponse response = voteService.getVoteCounts(placeUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("투표 수 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
