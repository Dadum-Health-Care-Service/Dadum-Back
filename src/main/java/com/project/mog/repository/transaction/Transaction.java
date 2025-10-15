package com.project.mog.repository.transaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "hour")
    private Integer hour;
    
    @Column(name = "day_of_week")
    private Integer dayOfWeek;
    
    @Column(name = "transaction_count_24h")
    private Integer transactionCount24h;
    
    @Column(name = "avg_amount_7d", precision = 19, scale = 2)
    private BigDecimal avgAmount7d;
    
    @Column(name = "location_distance")
    private Double locationDistance;
    
    @Column(name = "card_age_days")
    private Integer cardAgeDays;
    
    @Column(name = "merchant_category")
    private Integer merchantCategory;
    
    @Column(name = "merchant_id")
    private String merchantId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "device_info")
    private String deviceInfo;
    
    // AI 분석 결과
    @Column(name = "risk_score")
    private Double riskScore;
    
    @Column(name = "is_anomaly")
    private Boolean isAnomaly;
    
    @Column(name = "confidence")
    private Double confidence;
    
    @Column(name = "anomaly_score")
    private Double anomalyScore;
    
    @Column(name = "recommendation", length = 1000)
    private String recommendation;
    
    @Column(name = "ai_error")
    private String aiError;
    
    // 메타데이터
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 편의 메서드들
    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 80.0;
    }
    
    public boolean isMediumRisk() {
        return riskScore != null && riskScore >= 60.0 && riskScore < 80.0;
    }
    
    public boolean isLowRisk() {
        return riskScore != null && riskScore >= 40.0 && riskScore < 60.0;
    }
    
    public boolean isSafe() {
        return riskScore != null && riskScore < 40.0;
    }
    
    public String getRiskLevel() {
        if (isHighRisk()) return "HIGH";
        if (isMediumRisk()) return "MEDIUM";
        if (isLowRisk()) return "LOW";
        if (isSafe()) return "SAFE";
        return "UNKNOWN";
    }
}
