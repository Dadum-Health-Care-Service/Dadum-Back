package com.project.mog.controller.seller;

import com.project.mog.repository.payment.RefundEntity;
import com.project.mog.service.payment.seller.RefundManagementService;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.repository.users.UsersRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/seller/refunds")
@Tag(name = "환불 관리", description = "판매자 환불 관리 관련 API")
public class RefundManagementController {

    @Autowired
    private RefundManagementService refundManagementService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UsersRepository usersRepository;

    @Operation(summary = "환불 목록 조회", description = "판매자의 환불 목록을 페이징으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRefunds(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<RefundEntity> refunds;
            
            if (status != null && !status.equals("all")) {
                RefundEntity.RefundStatus refundStatus = RefundEntity.RefundStatus.valueOf(status);
                List<RefundEntity> refundList = refundManagementService.getRefundsByStatus(sellerId, refundStatus);
                // List를 Page로 변환 (간단한 구현)
                refunds = new org.springframework.data.domain.PageImpl<>(refundList, pageable, refundList.size());
            } else {
                refunds = refundManagementService.getRefundsBySeller(sellerId, pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("refunds", refunds.getContent());
            response.put("totalPages", refunds.getTotalPages());
            response.put("currentPage", refunds.getNumber());
            response.put("totalElements", refunds.getTotalElements());
            response.put("size", refunds.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "환불 상세 조회", description = "특정 환불의 상세 정보를 조회합니다.")
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundEntity> getRefund(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long refundId) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Optional<RefundEntity> refund = refundManagementService.getRefundById(refundId, sellerId);
            return refund.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "환불 승인", description = "환불을 승인합니다.")
    @PutMapping("/{refundId}/approve")
    public ResponseEntity<RefundEntity> approveRefund(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long refundId,
            @RequestBody(required = false) Map<String, String> requestData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            String adminComment = requestData != null ? requestData.get("adminComment") : null;
            RefundEntity refund = refundManagementService.approveRefund(refundId, sellerId, adminComment);
            return ResponseEntity.ok(refund);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "환불 거부", description = "환불을 거부합니다.")
    @PutMapping("/{refundId}/reject")
    public ResponseEntity<RefundEntity> rejectRefund(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long refundId,
            @RequestBody(required = false) Map<String, String> requestData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            String adminComment = requestData != null ? requestData.get("adminComment") : null;
            RefundEntity refund = refundManagementService.rejectRefund(refundId, sellerId, adminComment);
            return ResponseEntity.ok(refund);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "환불 처리중", description = "환불을 처리중으로 변경합니다.")
    @PutMapping("/{refundId}/process")
    public ResponseEntity<RefundEntity> processRefund(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long refundId) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            RefundEntity refund = refundManagementService.processRefund(refundId, sellerId);
            return ResponseEntity.ok(refund);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "환불 통계", description = "환불 관련 통계를 조회합니다.")
    @GetMapping("/statistics")
    public ResponseEntity<RefundManagementService.RefundStatistics> getRefundStatistics(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            RefundManagementService.RefundStatistics statistics = 
                refundManagementService.getRefundStatistics(sellerId);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
