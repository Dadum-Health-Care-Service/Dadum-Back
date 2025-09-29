package com.project.mog.controller.payment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mog.service.payment.PaymentService;
import com.project.mog.service.payment.dto.OrderResponseDto;
import com.project.mog.service.payment.dto.PaymentRequestDto;
import com.project.mog.service.payment.dto.PaymentResponseDto;
import com.project.mog.service.payment.dto.RefundRequestDto;
import com.project.mog.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "결제 관리", description = "결제 및 주문 관련 API")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;
    
    /**
     * 결제 처리 및 주문 생성
     */
    @PostMapping("/process")
    @Operation(summary = "결제 처리", description = "결제를 처리하고 주문을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "결제 처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<OrderResponseDto> processPayment(
            @RequestBody PaymentRequestDto requestDto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        OrderResponseDto orderResponse = paymentService.processPayment(requestDto, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
    
    /**
     * 결제 정보 조회
     */
    @GetMapping("/{merchantUid}")
    @Operation(summary = "결제 정보 조회", description = "주문번호로 결제 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    public ResponseEntity<PaymentResponseDto> getPayment(
            @Parameter(description = "주문번호", example = "ORDER_1234567890") @PathVariable String merchantUid,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        PaymentResponseDto paymentResponse = paymentService.getPaymentByMerchantUid(merchantUid);
        
        return ResponseEntity.ok(paymentResponse);
    }
    
    /**
     * 사용자의 결제 내역 조회
     */
    @GetMapping("/user/payments")
    @Operation(summary = "사용자 결제 내역 조회", description = "로그인한 사용자의 모든 결제 내역을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<PaymentResponseDto>> getUserPayments(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        List<PaymentResponseDto> payments = paymentService.getUserPayments(userEmail);
        
        return ResponseEntity.ok(payments);
    }
    
    /**
     * 사용자의 주문 내역 조회
     */
    @GetMapping("/user/orders")
    @Operation(summary = "사용자 주문 내역 조회", description = "로그인한 사용자의 모든 주문 내역을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        List<OrderResponseDto> orders = paymentService.getUserOrders(userEmail);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 결제 취소
     */
    @DeleteMapping("/{merchantUid}/cancel")
    @Operation(summary = "결제 취소", description = "주문번호로 결제를 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "취소 불가능한 상태")
    })
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @Parameter(description = "주문번호", example = "ORDER_1234567890") @PathVariable String merchantUid,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        PaymentResponseDto paymentResponse = paymentService.cancelPayment(merchantUid, userEmail);
        
        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * 환불 요청
     */
    @PostMapping("/{merchantUid}/refund")
    @Operation(summary = "환불 요청", description = "주문번호로 환불을 요청합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "환불 요청 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "환불 불가능한 상태")
    })
    public ResponseEntity<PaymentResponseDto> requestRefund(
            @Parameter(description = "주문번호", example = "ORDER_1234567890") @PathVariable String merchantUid,
            @RequestBody RefundRequestDto refundRequest,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        PaymentResponseDto paymentResponse = paymentService.requestRefund(merchantUid, userEmail, refundRequest);
        
        return ResponseEntity.ok(paymentResponse);
    }
}
