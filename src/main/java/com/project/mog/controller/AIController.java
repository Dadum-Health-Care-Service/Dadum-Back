package com.project.mog.controller;

import com.project.mog.dto.FraudDetectionRequest;
import com.project.mog.dto.FraudDetectionResponse;
import com.project.mog.service.AIService;
import com.project.mog.repository.TransactionRepository;
import com.project.mog.repository.transaction.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * 이상거래 탐지 API
     */
    @PostMapping("/detect-fraud")
    public ResponseEntity<FraudDetectionResponse> detectFraud(@RequestBody FraudDetectionRequest request) {
        try {
            FraudDetectionResponse response = aiService.detectFraud(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            FraudDetectionResponse errorResponse = FraudDetectionResponse.builder()
                .transactionId(request.getTransactionId())
                .riskScore(0.0)
                .isAnomaly(false)
                .confidence(0.0)
                .recommendation("AI 서비스 오류로 인해 거래를 수동으로 검토해주세요.")
                .error("서버 오류: " + e.getMessage())
                .build();
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * AI 서버 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        
        boolean isHealthy = aiService.isAIServiceHealthy();
        response.put("ai_service_healthy", isHealthy);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (isHealthy) {
            response.put("status", "healthy");
            response.put("message", "AI 서비스가 정상적으로 작동 중입니다.");
        } else {
            response.put("status", "unhealthy");
            response.put("message", "AI 서비스에 연결할 수 없습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * AI 모델 상태 확인
     */
    @GetMapping("/model-status")
    public ResponseEntity<Map<String, Object>> getModelStatus() {
        System.out.println("=== model-status API 호출됨 ===");
        try {
            Map<String, Object> status = aiService.getModelStatus();
            System.out.println("모델 상태: " + status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            System.err.println("model-status 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("error", "모델 상태 확인 실패: " + e.getMessage());
            errorStatus.put("is_trained", false);
            return ResponseEntity.ok(errorStatus);
        }
    }
    
    /**
     * AI 모델 훈련 요청
     */
    @PostMapping("/train-model")
    public ResponseEntity<Map<String, Object>> trainModel() {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = aiService.trainModel();
        
        if (success) {
            response.put("success", true);
            response.put("message", "AI 모델 훈련이 시작되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "AI 모델 훈련 요청에 실패했습니다.");
        }
        
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 간단한 이상거래 탐지 (거래 ID, 금액, 사용자 ID만으로)
     */
    @PostMapping("/detect-fraud-simple")
    public ResponseEntity<FraudDetectionResponse> detectFraudSimple(
            @RequestParam String transactionId,
            @RequestParam Double amount,
            @RequestParam String userId) {
        
        System.out.println("=== detect-fraud-simple API 호출됨 ===");
        System.out.println("transactionId: " + transactionId);
        System.out.println("amount: " + amount);
        System.out.println("userId: " + userId);
        
        try {
            FraudDetectionRequest request = FraudDetectionRequest.fromTransaction(
                transactionId, 
                java.math.BigDecimal.valueOf(amount), 
                userId
            );
            
            FraudDetectionResponse response = aiService.detectFraud(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== detect-fraud-simple 오류 발생 ===");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            FraudDetectionResponse errorResponse = FraudDetectionResponse.builder()
                .transactionId(transactionId)
                .riskScore(0.0)
                .isAnomaly(false)
                .confidence(0.0)
                .recommendation("AI 서비스 오류로 인해 거래를 수동으로 검토해주세요.")
                .error("서버 오류: " + e.getMessage())
                .build();
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 거래 히스토리 조회
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Boolean isAnomaly) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions;
        
        if (userId != null && isAnomaly != null) {
            if (isAnomaly) {
                transactions = transactionRepository.findByUserIdAndIsAnomalyTrueOrderByCreatedAtDesc(userId, pageable);
            } else {
                transactions = transactionRepository.findByUserIdAndIsAnomalyFalseOrderByCreatedAtDesc(userId, pageable);
            }
        } else if (userId != null) {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else if (isAnomaly != null) {
            if (isAnomaly) {
                transactions = transactionRepository.findByIsAnomalyTrueOrderByCreatedAtDesc(pageable);
            } else {
                transactions = transactionRepository.findByIsAnomalyFalseOrderByCreatedAtDesc(pageable);
            }
        } else {
            // Pageable에 정렬 추가
            Pageable sortedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            transactions = transactionRepository.findAll(sortedPageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions.getContent());
        response.put("totalElements", transactions.getTotalElements());
        response.put("totalPages", transactions.getTotalPages());
        response.put("currentPage", transactions.getNumber());
        response.put("size", transactions.getSize());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 거래 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 기본 통계
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("anomalyCount", transactionRepository.countAnomalies());
        stats.put("normalCount", transactionRepository.countNormalTransactions());
        stats.put("averageRiskScore", transactionRepository.getAverageRiskScore());
        
        // 위험도 분포
        stats.put("riskDistribution", transactionRepository.getRiskDistribution());
        
        // 시간대별 통계
        stats.put("transactionsByHour", transactionRepository.getTransactionCountByHour());
        
        // 요일별 통계
        stats.put("transactionsByDayOfWeek", transactionRepository.getTransactionCountByDayOfWeek());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 특정 거래 상세 조회
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
