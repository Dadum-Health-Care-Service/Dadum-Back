package com.project.mog.service.payment.dto;

import java.time.LocalDateTime;

import com.project.mog.repository.payment.PaymentEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    
    @Schema(hidden = true)
    private Long paymentId;
    private String merchantUid;
    private String impUid;
    private String productName;
    private Integer amount;
    private String paymentMethod;
    private String paymentStatus;
    @Schema(hidden = true)
    private LocalDateTime createdAt;
    
    public static PaymentResponseDto fromEntity(PaymentEntity entity) {
        return PaymentResponseDto.builder()
                .paymentId(entity.getPaymentId())
                .merchantUid(entity.getMerchantUid())
                .impUid(entity.getImpUid())
                .productName(entity.getProductName())
                .amount(entity.getAmount())
                .paymentMethod(entity.getPaymentMethod())
                .paymentStatus(entity.getPaymentStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
