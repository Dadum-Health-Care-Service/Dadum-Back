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

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;
    
    /**
     * 결제 처리 및 주문 생성
     */
    @PostMapping("/process")
    public ResponseEntity<OrderResponseDto> processPayment(
            @RequestBody PaymentRequestDto requestDto,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        OrderResponseDto orderResponse = paymentService.processPayment(requestDto, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
    
    /**
     * 결제 정보 조회
     */
    @GetMapping("/{merchantUid}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable String merchantUid,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        PaymentResponseDto paymentResponse = paymentService.getPaymentByMerchantUid(merchantUid);
        
        return ResponseEntity.ok(paymentResponse);
    }
    
    /**
     * 사용자의 결제 내역 조회
     */
    @GetMapping("/user/payments")
    public ResponseEntity<List<PaymentResponseDto>> getUserPayments(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        List<PaymentResponseDto> payments = paymentService.getUserPayments(userEmail);
        
        return ResponseEntity.ok(payments);
    }
    
    /**
     * 사용자의 주문 내역 조회
     */
    @GetMapping("/user/orders")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        List<OrderResponseDto> orders = paymentService.getUserOrders(userEmail);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 결제 취소
     */
    @DeleteMapping("/{merchantUid}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @PathVariable String merchantUid,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        PaymentResponseDto paymentResponse = paymentService.cancelPayment(merchantUid, userEmail);
        
        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * 환불 요청
     */
    @PostMapping("/{merchantUid}/refund")
    public ResponseEntity<PaymentResponseDto> requestRefund(
            @PathVariable String merchantUid,
            @RequestBody RefundRequestDto refundRequest,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUserEmail(token);
        
        PaymentResponseDto paymentResponse = paymentService.requestRefund(merchantUid, userEmail, refundRequest);
        
        return ResponseEntity.ok(paymentResponse);
    }
}
