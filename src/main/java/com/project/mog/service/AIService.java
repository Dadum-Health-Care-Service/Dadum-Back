package com.project.mog.service;

import com.project.mog.dto.FraudDetectionRequest;
import com.project.mog.dto.FraudDetectionResponse;
import com.project.mog.repository.TransactionRepository;
import com.project.mog.repository.transaction.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RealtimeNotificationService notificationService;
    
    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Value("${ai.service.timeout:30000}")
    private int timeout;
    
    /**
     * 이상거래 탐지 수행 (데이터베이스 저장 포함)
     */
    public FraudDetectionResponse detectFraud(FraudDetectionRequest request) {
        FraudDetectionResponse aiResponse;
        
        try {
            // AI 서버 호출
            aiResponse = callAIService(request);
            
            // 데이터베이스에 거래 정보 저장 (별도 트랜잭션)
            saveTransactionToDatabase(request, aiResponse);
            
            return aiResponse;
            
        } catch (Exception e) {
            // 오류 발생 시에도 기본 응답 생성
            aiResponse = createErrorResponse(request.getTransactionId(), 
                "AI 서비스 오류: " + e.getMessage());
            
            // 오류 정보도 데이터베이스에 저장
            try {
                saveTransactionToDatabase(request, aiResponse);
            } catch (Exception dbException) {
                // 데이터베이스 저장 실패는 로그만 남기고 계속 진행
                System.err.println("데이터베이스 저장 실패: " + dbException.getMessage());
            }
            
            return aiResponse;
        }
    }
    
    /**
     * AI 서버 호출 (트랜잭션 없음)
     */
    private FraudDetectionResponse callAIService(FraudDetectionRequest request) {
        try {
            String url = aiServiceUrl + "/ai/detect-fraud";

            
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("transaction_id", request.getTransactionId());
            requestData.put("amount", request.getAmount());
            requestData.put("user_id", request.getUserId());
            requestData.put("timestamp", request.getTimestamp());
            requestData.put("hour", request.getHour());
            requestData.put("day_of_week", request.getDayOfWeek());
            requestData.put("transaction_count_24h", request.getTransactionCount24h());
            requestData.put("avg_amount_7d", request.getAvgAmount7d());
            requestData.put("location_distance", request.getLocationDistance());
            requestData.put("card_age_days", request.getCardAgeDays());
            requestData.put("merchant_category", request.getMerchantCategory());
            requestData.put("merchant_id", request.getMerchantId());
            requestData.put("ip_address", request.getIpAddress());
            requestData.put("device_info", request.getDeviceInfo());
            
            // 디버깅을 위한 로그 추가
            System.out.println("=== AI 서버로 보내는 요청 데이터 ===");
            System.out.println("transaction_id: " + request.getTransactionId());
            System.out.println("amount: " + request.getAmount());
            System.out.println("user_id: " + request.getUserId());
            System.out.println("timestamp: " + request.getTimestamp());
            System.out.println("hour: " + request.getHour());
            System.out.println("day_of_week: " + request.getDayOfWeek());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // AI 서버 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            System.out.println("=== AI 서버 응답 ===");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                
                System.out.println("=== AI 분석 결과 ===");
                System.out.println("risk_score: " + responseBody.get("risk_score"));
                System.out.println("is_anomaly: " + responseBody.get("is_anomaly"));
                System.out.println("confidence: " + responseBody.get("confidence"));
                System.out.println("recommendation: " + responseBody.get("recommendation"));
                
                // AI 분석 결과 생성
                return FraudDetectionResponse.builder()
                    .transactionId(request.getTransactionId())
                    .riskScore(Double.parseDouble(responseBody.get("risk_score").toString()))
                    .isAnomaly(Boolean.parseBoolean(responseBody.get("is_anomaly").toString()))
                    .confidence(Double.parseDouble(responseBody.get("confidence").toString()))
                    .anomalyScore(responseBody.get("anomaly_score") != null ? 
                        Double.parseDouble(responseBody.get("anomaly_score").toString()) : null)
                    .recommendation(responseBody.get("recommendation").toString())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .error(responseBody.get("error") != null ? responseBody.get("error").toString() : null)
                    .build();
            } else {
                throw new RuntimeException("AI 서버 응답 오류");
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("AI 서버 HTTP 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 HTTP 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            System.err.println("AI 서버 연결 실패: " + e.getMessage());
            throw new RuntimeException("AI 서버 연결 실패: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("AI 서비스 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 서비스 오류: " + e.getMessage());
        }
    }
    
    /**
     * AI 서버 상태 확인
     */
//    public boolean isAIServiceHealthy() {
//        try {
//            String url = aiServiceUrl + "/health/";
//            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//            return response.getStatusCode().is2xxSuccessful();
//        } catch (Exception e) {
//            return false;
//        }
//    }
    
    /**
     * AI 모델 상태 확인
     */
//    public Map<String, Object> getModelStatus() {
//        try {
//            String url = aiServiceUrl + "/ai/model-status";
//            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//            return response.getBody();
//        } catch (Exception e) {
//            Map<String, Object> errorStatus = new HashMap<>();
//            errorStatus.put("error", "AI 모델 상태 확인 실패: " + e.getMessage());
//            errorStatus.put("is_trained", false);
//            return errorStatus;
//        }
//    }
    
    /**
     * AI 모델 훈련 요청
     */
//    public boolean trainModel() {
//        try {
//            String url = aiServiceUrl + "/ai/train-model";
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
//            return response.getStatusCode().is2xxSuccessful();
//        } catch (Exception e) {
//            return false;
//        }
//    }
    
    /**
     * 거래 정보를 데이터베이스에 저장
     */
    @Transactional
    private void saveTransactionToDatabase(FraudDetectionRequest request, FraudDetectionResponse response) {
        try {
            Transaction transaction = Transaction.builder()
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .timestamp(request.getTimestamp() != null ? 
                    LocalDateTime.parse(request.getTimestamp()) : LocalDateTime.now())
                .hour(request.getHour())
                .dayOfWeek(request.getDayOfWeek())
                .transactionCount24h(request.getTransactionCount24h())
                .avgAmount7d(request.getAvgAmount7d())
                .locationDistance(request.getLocationDistance())
                .cardAgeDays(request.getCardAgeDays())
                .merchantCategory(request.getMerchantCategory())
                .merchantId(request.getMerchantId())
                .ipAddress(request.getIpAddress())
                .deviceInfo(request.getDeviceInfo())
                .riskScore(response.getRiskScore())
                .isAnomaly(response.getIsAnomaly())
                .confidence(response.getConfidence())
                .anomalyScore(response.getAnomalyScore())
                .recommendation(response.getRecommendation())
                .aiError(response.getError())
                .build();
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // 이상거래인 경우 실시간 알림 전송
            if (response.getIsAnomaly() && response.getRiskScore() != null && response.getRiskScore() >= 60) {
                try {
                    notificationService.sendFraudAlert(savedTransaction);
                } catch (Exception notificationError) {
                    System.err.println("실시간 알림 전송 실패: " + notificationError.getMessage());
                }
            }
            
        } catch (Exception e) {
            // 데이터베이스 저장 실패 시 로그만 남기고 계속 진행
            System.err.println("거래 정보 저장 실패: " + e.getMessage());
        }
    }
    
    /**
     * AI 서비스 상태 확인
     */
    public boolean isAIServiceHealthy() {
        try {
            // AI 서버 연결 테스트
            String url = aiServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("AI 서비스 상태 확인 응답: " + response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("AI 서비스 상태 확인 실패: " + e.getMessage());
            System.err.println("AI 서비스 URL: " + aiServiceUrl);
            return false;
        }
    }
    
    /**
     * AI 모델 상태 확인
     */
    public Map<String, Object> getModelStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            String url = aiServiceUrl + "/model-status";
            System.out.println("AI 모델 상태 확인 URL: " + url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            System.out.println("AI 모델 상태 확인 응답: " + response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                status.putAll(response.getBody());

                System.out.println("AI 모델 상태: " + response.getBody());
            } else {
                status.put("is_trained", false);
                status.put("model_loaded", false);
                status.put("error", "AI 서버에서 모델 상태를 가져올 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("모델 상태 확인 실패: " + e.getMessage());
            System.err.println("AI 서비스 URL: " + aiServiceUrl);
            status.put("is_trained", false);
            status.put("model_loaded", false);
            status.put("error", "AI 서버 연결 실패: " + e.getMessage());
        }
        return status;
    }
    
    /**
     * AI 모델 훈련
     */
    public boolean trainModel() {
        try {
            String url = aiServiceUrl + "/ai/train-model";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("모델 훈련 요청 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * AI 통계 조회
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 데이터베이스에서 통계 계산
            long totalTransactions = transactionRepository.count();
            long anomalyTransactions = transactionRepository.countByIsAnomalyTrue();
            long normalTransactions = transactionRepository.countByIsAnomalyFalse();
            
            stats.put("total_transactions", totalTransactions);
            stats.put("anomaly_transactions", anomalyTransactions);
            stats.put("normal_transactions", normalTransactions);
            stats.put("anomaly_rate", totalTransactions > 0 ? (double) anomalyTransactions / totalTransactions : 0.0);
            stats.put("timestamp", java.time.LocalDateTime.now().toString());
            
        } catch (Exception e) {
            System.err.println("통계 조회 실패: " + e.getMessage());
            stats.put("error", "통계 조회 실패: " + e.getMessage());
        }
        return stats;
    }

    private FraudDetectionResponse createErrorResponse(String transactionId, String errorMessage) {
        return FraudDetectionResponse.builder()
            .transactionId(transactionId)
            .riskScore(0.0)
            .isAnomaly(false)
            .confidence(0.0)
            .recommendation("AI 서비스 오류로 인해 거래를 수동으로 검토해주세요.")
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .error(errorMessage)
            .build();
    }
}
