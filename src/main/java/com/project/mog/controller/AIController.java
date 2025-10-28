package com.project.mog.controller;

import com.project.mog.dto.FraudDetectionRequest;
import com.project.mog.dto.FraudDetectionResponse;
import com.project.mog.service.AIService;
import com.project.mog.repository.TransactionRepository;
import com.project.mog.repository.transaction.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private TransactionRepository transactionRepository;

    // FastAPI 연동 설정 (application.yml 사용)
    // ai.service.url: http://localhost:9000   (주의: 여기에는 /ai 안 붙음)
    // ai.service.timeout: 30000
    @Value("${ai.service.url:http://localhost:9000}")
    private String aiServiceUrl;

    @Value("${ai.service.timeout:30000}")
    private long aiTimeoutMs;

    private final RestTemplate rest = new RestTemplate();

    // ==== YOLO용 DTO ====
    public static class ImageRequest { public String image; }
    public static class SegmentResponse { public String image; }
    public static class Keypoint { public int id; public float x; public float y; public float conf; }
    public static class PoseResponse {
        public String image;
        public List<Keypoint> keypoints;
        public float[] bbox;
    }

    // ==== YOLO 세그멘테이션 프록시 ====
    @PostMapping(value = "/segment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SegmentResponse> segment(@RequestBody ImageRequest req) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>)
                    rest.postForEntity(aiServiceUrl + "/ai/segment", req, Map.class).getBody();

            SegmentResponse out = new SegmentResponse();
            if (body != null) {
                Object img = body.get("image");
                if (img == null) img = body.get("base64");
                if (img instanceof String s) {
                    out.image = s.startsWith("data:") ? s : "data:image/jpeg;base64," + s;
                }
            }
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            SegmentResponse out = new SegmentResponse(); // null image 로 200 응답(팀 컨벤션)
            return ResponseEntity.ok(out);
        }
    }

    // ==== YOLO 포즈 프록시 ====
    @PostMapping(value = "/pose", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PoseResponse> pose(@RequestBody ImageRequest req) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>)
                    rest.postForEntity(aiServiceUrl + "/ai/pose", req, Map.class).getBody();

            PoseResponse out = new PoseResponse();
            out.keypoints = new ArrayList<>();

            if (body != null) {
                // image
                Object img = body.get("image");
                if (img == null) img = body.get("base64");
                if (img instanceof String s) {
                    out.image = s.startsWith("data:") ? s : "data:image/jpeg;base64," + s;
                }

                // keypoints (제네릭 안전 캐스팅 + 수동 기본값)
                Object kp = body.get("keypoints");
                if (kp instanceof List<?> list) {
                    for (int i = 0; i < list.size(); i++) {
                        Object o = list.get(i);
                        if (o instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> m = (Map<String, Object>) o;

                            Keypoint k = new Keypoint();

                            Object idObj = m.get("id");
                            k.id = (idObj instanceof Number) ? ((Number) idObj).intValue() : i;

                            Object xObj = m.get("x");
                            Object yObj = m.get("y");
                            Object cObj = m.get("conf");

                            k.x = (xObj instanceof Number) ? ((Number) xObj).floatValue() : 0f;
                            k.y = (yObj instanceof Number) ? ((Number) yObj).floatValue() : 0f;
                            k.conf = (cObj instanceof Number) ? ((Number) cObj).floatValue() : 0f;

                            out.keypoints.add(k);
                        }
                    }
                }

                // bbox
                Object bbObj = body.get("bbox");
                if (bbObj instanceof List<?> bb && bb.size() == 4) {
                    out.bbox = new float[]{
                            ((Number) bb.get(0)).floatValue(),
                            ((Number) bb.get(1)).floatValue(),
                            ((Number) bb.get(2)).floatValue(),
                            ((Number) bb.get(3)).floatValue()
                    };
                }
            }
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            PoseResponse out = new PoseResponse();
            out.image = null;
            out.keypoints = new ArrayList<>();
            out.bbox = null;
            return ResponseEntity.ok(out);
        }
    }


    /**
     * 이상거래 탐지 API
     */
    @PostMapping("/detect-fraud")
    public ResponseEntity<FraudDetectionResponse> detectFraud(@RequestBody FraudDetectionRequest request) {
        try {
            System.out.println("=== 이상거래 탐지 API 호출됨 ===");
            System.out.println("요청 데이터: " + request);
            FraudDetectionResponse response = aiService.detectFraud(request);
            System.out.println("탐지 결과: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("이상거래 탐지 오류: " + e.getMessage());
            e.printStackTrace();
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

        try {
            boolean isHealthy = aiService.isAIServiceHealthy();
            response.put("ai_service_healthy", isHealthy);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            if (isHealthy) {
                response.put("status", "healthy");
                response.put("message", "AI 서비스가 정상적으로 작동 중입니다.");
            } else {
                response.put("status", "unhealthy");
                response.put("message", "AI 서비스에 연결할 수 없습니다. 폴링 모드로 전환됩니다.");
            }

            System.out.println("AI Health Check Response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("AI Health Check Error: " + e.getMessage());
            response.put("ai_service_healthy", false);
            response.put("status", "error");
            response.put("message", "AI 서비스 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response); // 오류가 있어도 200 응답으로 반환
        }
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
            errorStatus.put("model_loaded", false);
            errorStatus.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(errorStatus); // 오류가 있어도 200 응답으로 반환
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

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
                transactions = transactionRepository.findAll(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("transactions", transactions.getContent());
            response.put("totalElements", transactions.getTotalElements());
            response.put("totalPages", transactions.getTotalPages());
            response.put("currentPage", transactions.getNumber());
            response.put("size", transactions.getSize());

            System.out.println("거래 히스토리 조회 응답: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("거래 히스토리 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "거래 히스토리 조회 실패: " + e.getMessage());
            errorResponse.put("transactions", new java.util.ArrayList<>());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", 0);
            errorResponse.put("size", 0);
            return ResponseEntity.ok(errorResponse); // 오류가 있어도 200 응답으로 반환
        }
    }

    /**
     * AI 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = aiService.getStatistics();
            System.out.println("AI 통계 조회 응답: " + stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("통계 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "통계 조회 실패: " + e.getMessage());
            errorResponse.put("total_transactions", 0);
            errorResponse.put("anomaly_transactions", 0);
            errorResponse.put("normal_transactions", 0);
            errorResponse.put("anomaly_rate", 0.0);
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(errorResponse); // 오류가 있어도 200 응답으로 반환
        }
    }
}
