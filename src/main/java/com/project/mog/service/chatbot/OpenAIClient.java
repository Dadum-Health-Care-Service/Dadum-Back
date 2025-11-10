package com.project.mog.service.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.mog.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIClient {

    private final WebClient openaiWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api.timeout:30}")
    private int timeoutSeconds;

    // OpenAI API 설정 상수
    private static final String MODEL = "gpt-4o-mini";
    private static final double TEMPERATURE = 0.4; // 규칙 준수 향상을 위해 낮춤
    private static final int MAX_TOKENS = 600; // 응답 속도 향상을 위해 단축
    private static final String DONE_SIGNAL = "[DONE]";
    private static final String API_KEY_ERROR_MESSAGE = "{\"error\": \"OpenAI API 키가 설정되지 않았습니다.\"}";
    private static final String API_CALL_ERROR_MESSAGE = "{\"error\": \"OpenAI API 호출 중 오류가 발생했습니다.\"}";

    public boolean isApiKeyValid() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-openai-api-key-here");
    }

    public Flux<String> streamChatCompletion(ChatRequest chatRequest) {
        log.info("OpenAI API 호출 시작 - 메시지 수: {}", chatRequest.getMessages().size());
        
        if (!isApiKeyValid()) {
            log.error("OpenAI API 키가 설정되지 않았습니다");
            return Flux.just(API_KEY_ERROR_MESSAGE);
        }

        Map<String, Object> requestBody = Map.of(
            "model", MODEL,
            "messages", chatRequest.getMessages(),
            "stream", true,
            "temperature", TEMPERATURE,
            "max_tokens", MAX_TOKENS
        );
        
        return openaiWebClient
                .post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractContent)
                .filter(content -> !content.isEmpty())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(error -> {
                    log.error("OpenAI API 호출 실패: {}", error.getMessage(), error);
                    return Flux.just(API_CALL_ERROR_MESSAGE);
                });
    }

    /**
     * OpenAI 응답에서 실제 콘텐츠 추출
     */
    private String extractContent(String chunk) {
        // [DONE] 체크
        if (DONE_SIGNAL.equals(chunk.trim())) {
            return DONE_SIGNAL;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(chunk);
            
            if (jsonNode.has("choices") && jsonNode.get("choices").isArray() && jsonNode.get("choices").size() > 0) {
                JsonNode choice = jsonNode.get("choices").get(0);
                
                if (choice.has("delta") && choice.get("delta").has("content")) {
                    String content = choice.get("delta").get("content").asText();
                    
                    if (content != null) {
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            // JSON 파싱 실패는 정상적인 경우일 수 있음 (빈 청크 등)
        }

        return "";
    }
}