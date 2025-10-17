package com.project.mog.controller;

import com.project.mog.repository.payment.OrderEntity;
import com.project.mog.repository.payment.OrderRepository;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "주문 관리", description = "주문 관련 API")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsersRepository usersRepository;

    @Operation(summary = "사용자 주문 내역 조회", description = "현재 로그인한 사용자의 주문 내역을 조회합니다.")
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("주문 내역 조회 요청 - Authorization: " + authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("잘못된 Authorization 헤더: " + authHeader);
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            System.out.println("추출된 토큰: " + token);
            
            String userEmail = jwtUtil.extractUserEmail(token);
            System.out.println("추출된 이메일: " + userEmail);
            
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("사용자 ID: " + userId);
            
            // 사용자의 주문 내역 조회
            List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            System.out.println("조회된 주문 수: " + orders.size());
            
            // 테스트용: 주문이 없으면 더미 데이터 생성
            if (orders.isEmpty()) {
                System.out.println("주문 데이터가 없습니다. 테스트용 더미 데이터를 생성합니다.");
                // 실제 구현에서는 더미 데이터를 생성하지 않음
                // 여기서는 빈 배열을 반환
            }
            
            // 주문 데이터 변환
            List<Map<String, Object>> orderList = orders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getOrderId());
                orderData.put("orderNumber", order.getOrderNumber());
                orderData.put("productName", order.getProductName());
                orderData.put("productCategory", order.getProductCategory());
                orderData.put("quantity", order.getQuantity());
                orderData.put("totalAmount", order.getTotalAmount());
                orderData.put("status", order.getOrderStatus());
                orderData.put("orderDate", order.getCreatedAt());
                orderData.put("deliveryDate", order.getUpdatedAt()); // 배송일은 updatedAt 사용
                orderData.put("merchantUid", order.getOrderNumber()); // 주문번호를 merchantUid로 사용
                orderData.put("shippingAddress", order.getShippingAddress());
                orderData.put("buyerName", order.getUser() != null ? order.getUser().getUsersName() : "알 수 없음");
                orderData.put("buyerPhone", order.getUser() != null ? order.getUser().getPhoneNum() : "알 수 없음");
                orderData.put("orderStatus", order.getOrderStatus());
                orderData.put("createdAt", order.getCreatedAt());
                orderData.put("updatedAt", order.getUpdatedAt());
                return orderData;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderList);
            response.put("totalCount", orderList.size());
            
            System.out.println("주문 데이터 반환: " + response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("주문 내역 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "주문 취소", description = "특정 주문을 취소합니다.")
    @DeleteMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        
        try {
            System.out.println("주문 취소 요청 - Order ID: " + orderId);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            
            // 주문 존재 확인 및 소유자 확인
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
            
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            
            // 주문 상태를 취소로 변경
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "주문이 성공적으로 취소되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("주문 취소 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "환불 요청", description = "특정 주문에 대한 환불을 요청합니다.")
    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<Map<String, Object>> requestRefund(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> refundRequest) {
        
        try {
            System.out.println("환불 요청 - Order ID: " + orderId);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            Long userId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            
            // 주문 존재 확인 및 소유자 확인
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
            
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            
            // 환불 요청 처리 (실제 구현에서는 환불 로직 추가)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "환불 요청이 접수되었습니다. 검토 후 처리됩니다.");
            response.put("refundId", "REF_" + System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("환불 요청 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
