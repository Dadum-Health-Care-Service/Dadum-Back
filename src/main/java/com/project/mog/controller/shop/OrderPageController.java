package com.project.mog.controller.shop;

import com.project.mog.dto.shop.OrderPageDto;
import com.project.mog.service.shop.OrderPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/shop/order")
@RequiredArgsConstructor
@Tag(name = "주문 페이지", description = "주문 페이지 관련 API")
public class OrderPageController {
    
    private final OrderPageService orderPageService;
    
    @GetMapping("/{productId}")
    @Operation(summary = "주문 페이지 데이터 조회", description = "특정 상품의 주문 페이지 데이터를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    public ResponseEntity<OrderPageDto> getOrderPageData(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Parameter(description = "사용자 ID", example = "user123") @RequestParam(required = false) String userId) {
        
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
    @Operation(summary = "내 주문 페이지 데이터 조회", description = "로그인한 사용자의 특정 상품 주문 페이지 데이터를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    public ResponseEntity<OrderPageDto> getMyOrderPageData(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Parameter(description = "사용자 이메일", example = "user@example.com") @RequestParam String email) {
        
        // 이메일로 사용자 식별
        OrderPageDto orderPageData = orderPageService.getOrderPageData(productId, email);
        
        if (orderPageData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderPageData);
    }
}
