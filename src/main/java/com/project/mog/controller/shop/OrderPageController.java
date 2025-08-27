package com.project.mog.controller.shop;

import com.project.mog.dto.shop.OrderPageDto;
import com.project.mog.service.shop.OrderPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop/order")
@RequiredArgsConstructor
public class OrderPageController {
    
    private final OrderPageService orderPageService;
    
    @GetMapping("/{productId}")
    public ResponseEntity<OrderPageDto> getOrderPageData(
            @PathVariable Long productId,
            @RequestParam(required = false) String userId) {
        
        // userId가 없으면 기본값 사용 (테스트용)
        String currentUserId = userId != null ? userId : "default_user";
        
        OrderPageDto orderPageData = orderPageService.getOrderPageData(productId, currentUserId);
        
        if (orderPageData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderPageData);
    }
    
    // 실제 로그인된 사용자의 주문 페이지 데이터 조회
    @GetMapping("/{productId}/my")
    public ResponseEntity<OrderPageDto> getMyOrderPageData(
            @PathVariable Long productId,
            @RequestParam String email) {
        
        // 이메일로 사용자 식별
        OrderPageDto orderPageData = orderPageService.getOrderPageData(productId, email);
        
        if (orderPageData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderPageData);
    }
}
