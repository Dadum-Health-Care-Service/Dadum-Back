package com.project.mog.controller.seller;

import com.project.mog.repository.users.UsersRepository;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.payment.seller.SellerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller")
@Tag(name = "판매자 대시보드", description = "판매자 대시보드 관련 API")
public class SellerDashboardController {

    @Autowired
    private SellerDashboardService sellerDashboardService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UsersRepository usersRepository;

    @Operation(summary = "판매자 대시보드 통계 조회", description = "현재 로그인한 판매자의 대시보드 통계 데이터를 조회합니다.")
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            System.out.println("판매자 대시보드 요청 - 사용자 이메일: " + userEmail);
            
            // 사용자 이메일로 사용자 ID 조회
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("판매자 ID: " + sellerId);
            
            Map<String, Object> stats = sellerDashboardService.getDashboardStats(sellerId);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("판매자 대시보드 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
