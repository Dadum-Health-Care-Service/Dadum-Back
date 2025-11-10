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

import java.util.HashMap;
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

    // ===== 매출 분석 API 엔드포인트 =====
    
    @Operation(summary = "매출 요약 조회", description = "기간별 매출 요약 데이터를 조회합니다.")
    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getSalesSummary(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "all") String category) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Map<String, Object> summary = sellerDashboardService.getSalesSummary(sellerId, period, category);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            System.err.println("매출 요약 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "일별 매출 조회", description = "기간별 일별 매출 데이터를 조회합니다.")
    @GetMapping("/analytics/daily-sales")
    public ResponseEntity<Map<String, Object>> getDailySales(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "all") String category) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Map<String, Object> dailySales = sellerDashboardService.getDailySales(sellerId, period, category);
            return ResponseEntity.ok(dailySales);
            
        } catch (Exception e) {
            System.err.println("일별 매출 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "카테고리별 매출 조회", description = "기간별 카테고리별 매출 데이터를 조회합니다.")
    @GetMapping("/analytics/category-sales")
    public ResponseEntity<Map<String, Object>> getCategorySales(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "all") String category) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Map<String, Object> categorySales = sellerDashboardService.getCategorySales(sellerId, period, category);
            return ResponseEntity.ok(categorySales);
            
        } catch (Exception e) {
            System.err.println("카테고리별 매출 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "인기 상품 조회", description = "기간별 인기 상품 데이터를 조회합니다.")
    @GetMapping("/analytics/top-products")
    public ResponseEntity<Map<String, Object>> getTopProducts(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "5") int limit) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Map<String, Object> topProducts = sellerDashboardService.getTopProducts(sellerId, period, category, limit);
            return ResponseEntity.ok(topProducts);
            
        } catch (Exception e) {
            System.err.println("인기 상품 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== 판매자 설정 API 엔드포인트 =====
    
    @Operation(summary = "판매자 프로필 설정 조회", description = "판매자의 프로필 설정 정보를 조회합니다.")
    @GetMapping("/settings/profile")
    public ResponseEntity<Map<String, Object>> getProfileSettings(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("프로필 설정 조회 요청 - Authorization: " + authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("잘못된 Authorization 헤더: " + authHeader);
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            System.out.println("추출된 토큰: " + token);
            
            String userEmail = jwtUtil.extractUserEmail(token);
            System.out.println("추출된 이메일: " + userEmail);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("판매자 ID: " + sellerId);
            
            // 임시 더미 데이터 반환
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("businessName", "샘플 사업체");
            profileData.put("businessNumber", "123-45-67890");
            profileData.put("representativeName", "홍길동");
            profileData.put("address", "서울시 강남구 테헤란로 123");
            profileData.put("zipCode", "06292");
            profileData.put("detailAddress", "456호");
            profileData.put("phoneNumber", "02-1234-5678");
            profileData.put("email", userEmail);
            profileData.put("bankAccount", "123456789012");
            profileData.put("bankName", "국민은행");
            profileData.put("accountHolder", "홍길동");
            
            System.out.println("프로필 데이터 반환: " + profileData);
            return ResponseEntity.ok(profileData);
            
        } catch (Exception e) {
            System.err.println("프로필 설정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @Operation(summary = "스토어 설정 조회", description = "스토어 설정 정보를 조회합니다.")
    @GetMapping("/settings/store")
    public ResponseEntity<Map<String, Object>> getStoreSettings(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("스토어 설정 조회 요청 - Authorization: " + authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("잘못된 Authorization 헤더: " + authHeader);
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            System.out.println("추출된 토큰: " + token);
            
            String userEmail = jwtUtil.extractUserEmail(token);
            System.out.println("추출된 이메일: " + userEmail);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("판매자 ID: " + sellerId);
            
            // 임시 더미 데이터 반환
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("storeName", "샘플 스토어");
            storeData.put("storeDescription", "고품질 운동용품을 판매하는 스토어입니다.");
            storeData.put("storeLogo", "");
            storeData.put("operatingHours", "09:00-18:00");
            storeData.put("deliveryFee", 3000);
            storeData.put("freeShippingThreshold", 50000);
            storeData.put("shippingPolicy", "주문 후 1-2일 내 발송");
            
            System.out.println("스토어 데이터 반환: " + storeData);
            return ResponseEntity.ok(storeData);
            
        } catch (Exception e) {
            System.err.println("스토어 설정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @Operation(summary = "정산 설정 조회", description = "정산 관련 설정 정보를 조회합니다.")
    @GetMapping("/settings/billing")
    public ResponseEntity<Map<String, Object>> getBillingSettings(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("정산 설정 조회 요청 - Authorization: " + authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("잘못된 Authorization 헤더: " + authHeader);
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            System.out.println("추출된 토큰: " + token);
            
            String userEmail = jwtUtil.extractUserEmail(token);
            System.out.println("추출된 이메일: " + userEmail);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("판매자 ID: " + sellerId);
            
            // 임시 더미 데이터 반환
            Map<String, Object> billingData = new HashMap<>();
            billingData.put("settlementCycle", "weekly");
            billingData.put("settlementAccount", "123456789012");
            billingData.put("taxInvoiceEmail", userEmail);
            billingData.put("platformFee", 3.5);
            billingData.put("paymentFee", 2.9);
            
            System.out.println("정산 데이터 반환: " + billingData);
            return ResponseEntity.ok(billingData);
            
        } catch (Exception e) {
            System.err.println("정산 설정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @Operation(summary = "알림 설정 조회", description = "알림 관련 설정 정보를 조회합니다.")
    @GetMapping("/settings/notifications")
    public ResponseEntity<Map<String, Object>> getNotificationSettings(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("알림 설정 조회 요청 - Authorization: " + authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("잘못된 Authorization 헤더: " + authHeader);
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            System.out.println("추출된 토큰: " + token);
            
            String userEmail = jwtUtil.extractUserEmail(token);
            System.out.println("추출된 이메일: " + userEmail);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("판매자 ID: " + sellerId);
            
            // 임시 더미 데이터 반환
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("orderNotification", true);
            notificationData.put("shippingNotification", true);
            notificationData.put("refundNotification", true);
            notificationData.put("autoResponse", false);
            notificationData.put("responseTemplate", "안녕하세요. 문의해주셔서 감사합니다.");
            notificationData.put("announcement", "");
            
            System.out.println("알림 데이터 반환: " + notificationData);
            return ResponseEntity.ok(notificationData);
            
        } catch (Exception e) {
            System.err.println("알림 설정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @Operation(summary = "설정 저장", description = "판매자 설정을 저장합니다.")
    @PutMapping("/settings/{section}")
    public ResponseEntity<Map<String, Object>> saveSettings(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable String section,
            @RequestBody Map<String, Object> settingsData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            // 실제 구현에서는 데이터베이스에 저장
            System.out.println("설정 저장 - 섹션: " + section + ", 데이터: " + settingsData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", section + " 설정이 저장되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("설정 저장 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
