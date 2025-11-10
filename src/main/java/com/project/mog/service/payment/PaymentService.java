package com.project.mog.service.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.mog.repository.payment.OrderEntity;
import com.project.mog.repository.payment.OrderRepository;
import com.project.mog.repository.payment.PaymentEntity;
import com.project.mog.repository.payment.PaymentRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.payment.dto.OrderResponseDto;
import com.project.mog.service.payment.dto.PaymentRequestDto;
import com.project.mog.service.payment.dto.PaymentResponseDto;
import com.project.mog.service.payment.dto.RefundRequestDto;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    
    /**
     * 결제 처리 및 주문 생성
     */
    public OrderResponseDto processPayment(PaymentRequestDto requestDto, String userEmail) {
        // 사용자 정보 조회
        UsersEntity user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 결제 정보 저장 (사용자 정보는 UsersEntity 참조로 대체)
        PaymentEntity payment = PaymentEntity.builder()
                .user(user)
                .merchantUid(requestDto.getMerchantUid())
                .impUid(requestDto.getImpUid())
                .productName(requestDto.getProductName())
                .amount(requestDto.getAmount())
                .paymentMethod(requestDto.getPaymentMethod())
                .paymentStatus("SUCCESS")
                .build();
        
        PaymentEntity savedPayment = paymentRepository.save(payment);
        
        // 주문 정보 생성 (배송 전화번호는 UsersEntity.phoneNum 사용)
        String orderNumber = generateOrderNumber();
        OrderEntity order = OrderEntity.builder()
                .user(user)
                .payment(savedPayment)
                .orderNumber(orderNumber)
                .productName(requestDto.getProductName())
                .productCategory(requestDto.getProductCategory())
                .quantity(requestDto.getQuantity() != null ? requestDto.getQuantity() : 1)
                .totalAmount(requestDto.getAmount())
                .orderStatus("CONFIRMED")
                .shippingAddress(requestDto.getShippingAddress())
                // .shippingPhone(requestDto.getShippingPhone()) - UsersEntity.phoneNum 사용
                .orderNotes(requestDto.getOrderNotes())
                .updatedAt(LocalDateTime.now())
                .build();
        
        OrderEntity savedOrder = orderRepository.save(order);
        
        return OrderResponseDto.fromEntity(savedOrder);
    }
    
    /**
     * 결제 정보 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByMerchantUid(String merchantUid) {
        PaymentEntity payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        
        return PaymentResponseDto.fromEntity(payment);
    }
    
    /**
     * 사용자의 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getUserPayments(String userEmail) {
        UsersEntity user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        List<PaymentEntity> payments = paymentRepository.findByUser(user);
        
        return payments.stream()
                .map(PaymentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 주문 내역 조회
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrders(String userEmail) {
        UsersEntity user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        List<OrderEntity> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        return orders.stream()
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 주문 번호 생성
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 결제 취소 처리
     */
    public PaymentResponseDto cancelPayment(String merchantUid, String userEmail) {
        UsersEntity user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        PaymentEntity payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        
        // 권한 확인
        if (payment.getUser().getUsersId() != user.getUsersId()) {
            throw new IllegalArgumentException("결제 취소 권한이 없습니다.");
        }
        
        try {
            // 1. Iamport V2 API로 실제 결제 취소 요청
            String accessToken = getIamportAccessToken();
            cancelIamportPayment(accessToken, merchantUid, payment.getImpUid());
            
            // 2. 성공 시 DB 상태 변경
            payment.setPaymentStatus("CANCELLED");
            PaymentEntity updatedPayment = paymentRepository.save(payment);
            
            return PaymentResponseDto.fromEntity(updatedPayment);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("PG사 결제 취소에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 환불 요청 처리
     */
    public PaymentResponseDto requestRefund(String merchantUid, String userEmail, RefundRequestDto refundRequest) {
        UsersEntity user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        PaymentEntity payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        
        // 권한 확인
        if (payment.getUser().getUsersId() != user.getUsersId()) {
            throw new IllegalArgumentException("환불 요청 권한이 없습니다.");
        }
        
        // 환불 가능 상태 확인
        if (!"SUCCESS".equals(payment.getPaymentStatus())) {
            throw new IllegalArgumentException("환불 요청이 불가능한 결제 상태입니다.");
        }
        
        // 환불 상태로 변경
        payment.setPaymentStatus("REFUND_REQUESTED");
        
        // 환불 정보 저장 (추후 RefundEntity로 분리 가능)
        // TODO: 환불 정보를 별도 테이블에 저장
        
        PaymentEntity updatedPayment = paymentRepository.save(payment);
        
        return PaymentResponseDto.fromEntity(updatedPayment);
    }

    /**
     * Iamport V2 API Access Token 발급
     */
    private String getIamportAccessToken() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Iamport V2 API Secret으로 Access Token 발급
        String requestBody = "{\"imp_key\":\"imp45586541\",\"imp_secret\":\"hRILlnbJnma5kNc1GFc6EBzCiL89Dch8vNV23hXw3274QoXAE7ft2B8cdgQtRM99PZiL2TVSZxFQTb3M\"}";
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.iamport.kr/users/getToken",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        
        if (root.get("code").asInt() == 0) {
            return root.get("response").get("access_token").asText();
        } else {
            throw new Exception("Iamport Access Token 발급 실패: " + root.get("message").asText());
        }
    }

    /**
     * Iamport V2 API로 결제 취소 요청
     */
    private void cancelIamportPayment(String accessToken, String merchantUid, String impUid) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        // 결제 취소 요청 데이터
        String requestBody = String.format(
            "{\"merchant_uid\":\"%s\",\"imp_uid\":\"%s\",\"reason\":\"사용자 요청\"}",
            merchantUid, impUid
        );
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.iamport.kr/payments/cancel",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        
        if (root.get("code").asInt() != 0) {
            throw new Exception("Iamport 결제 취소 실패: " + root.get("message").asText());
        }
    }
}
