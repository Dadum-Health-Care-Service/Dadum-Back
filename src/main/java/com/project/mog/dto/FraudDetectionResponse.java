package com.project.mog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResponse {
    
    private String transactionId;
    private Double riskScore;
    private Boolean isAnomaly;
    private Double confidence;
    private Double anomalyScore;
    private String recommendation;
    private String timestamp;
    private String error;
    
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
    
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}
