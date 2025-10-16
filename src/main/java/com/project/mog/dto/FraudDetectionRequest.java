package com.project.mog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionRequest {
    
    private String transactionId;
    private BigDecimal amount;
    private String userId;
    private String timestamp;
    private Integer hour;
    private Integer dayOfWeek;
    private Integer transactionCount24h;
    private BigDecimal avgAmount7d;
    private Double locationDistance;
    private Integer cardAgeDays;
    private Integer merchantCategory;
    private String merchantId;
    private String ipAddress;
    private String deviceInfo;
    
    // 편의 메서드들
    public static FraudDetectionRequest fromTransaction(String transactionId, BigDecimal amount, String userId) {
        LocalDateTime now = LocalDateTime.now();
        return FraudDetectionRequest.builder()
            .transactionId(transactionId)
            .amount(amount)
            .userId(userId)
            .timestamp(now.toString())
            .hour(now.getHour())
            .dayOfWeek(now.getDayOfWeek().getValue() - 1) // 0-6으로 변환
            .transactionCount24h(1) // 기본값
            .avgAmount7d(amount) // 기본값
            .locationDistance(0.0) // 기본값
            .cardAgeDays(365) // 기본값
            .merchantCategory(1) // 기본값
            .build();
    }
}
