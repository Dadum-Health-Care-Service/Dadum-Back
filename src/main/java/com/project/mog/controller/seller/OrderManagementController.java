package com.project.mog.controller.seller;

import com.project.mog.repository.payment.OrderEntity;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.payment.seller.OrderManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/seller/orders")
@Tag(name = "주문 관리", description = "판매자 주문 관리 관련 API")
public class OrderManagementController {

    @Autowired
    private OrderManagementService orderManagementService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "주문 목록 조회", description = "판매자의 주문 목록을 페이징으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L; // TODO: 사용자 이메일로 판매자 ID 조회
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderEntity> orders = orderManagementService.getOrdersBySeller(sellerId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders.getContent());
            response.put("totalPages", orders.getTotalPages());
            response.put("currentPage", orders.getNumber());
            response.put("totalElements", orders.getTotalElements());
            response.put("size", orders.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderEntity> getOrder(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            
            Optional<OrderEntity> order = orderManagementService.getOrderById(orderId, sellerId);
            return order.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "주문 상태별 조회", description = "특정 상태의 주문들을 조회합니다.")
    @GetMapping("/status/{orderStatus}")
    public ResponseEntity<List<OrderEntity>> getOrdersByStatus(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable String orderStatus) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            
            List<OrderEntity> orders = orderManagementService.getOrdersByStatus(sellerId, orderStatus);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "날짜 범위별 주문 조회", description = "특정 날짜 범위의 주문들을 조회합니다.")
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderEntity>> getOrdersByDateRange(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            
            List<OrderEntity> orders = orderManagementService.getOrdersByDateRange(sellerId, startDate, endDate);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "주문 상태 업데이트", description = "주문의 상태를 변경합니다.")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderEntity> updateOrderStatus(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            String newStatus = statusData.get("orderStatus");
            
            OrderEntity order = orderManagementService.updateOrderStatus(orderId, sellerId, newStatus);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "배송지 업데이트", description = "주문의 배송지를 업데이트합니다.")
    @PutMapping("/{orderId}/shipping-address")
    public ResponseEntity<OrderEntity> updateShippingAddress(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> addressData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            String shippingAddress = addressData.get("shippingAddress");
            
            OrderEntity order = orderManagementService.updateShippingAddress(orderId, sellerId, shippingAddress);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "주문 메모 업데이트", description = "주문의 메모를 업데이트합니다.")
    @PutMapping("/{orderId}/notes")
    public ResponseEntity<OrderEntity> updateOrderNotes(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> notesData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            String orderNotes = notesData.get("orderNotes");
            
            OrderEntity order = orderManagementService.updateOrderNotes(orderId, sellerId, orderNotes);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderEntity> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> cancelData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            String cancelReason = cancelData.get("cancelReason");
            
            OrderEntity order = orderManagementService.cancelOrder(orderId, sellerId, cancelReason);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "최근 주문 조회", description = "최근 N개의 주문을 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<List<OrderEntity>> getRecentOrders(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = 1L;
            
            List<OrderEntity> orders = orderManagementService.getRecentOrders(sellerId, limit);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
