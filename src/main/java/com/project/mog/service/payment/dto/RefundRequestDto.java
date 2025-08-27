package com.project.mog.service.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 환불 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {
    
    /**
     * 환불 사유
     */
    private String refundReason;
    
    /**
     * 환불 금액 (부분 환불 시)
     */
    private Long refundAmount;
    
    /**
     * 환불 계좌 정보 (계좌이체 환불 시)
     */
    private String bankCode;
    private String accountNumber;
    private String accountHolder;
    
    /**
     * 상품 상태
     */
    private String productCondition; // "NEW", "USED", "DAMAGED"
    
    /**
     * 추가 설명
     */
    private String additionalInfo;
}
