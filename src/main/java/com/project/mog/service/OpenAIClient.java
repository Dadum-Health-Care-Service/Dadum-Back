package com.project.mog.service;

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

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api.timeout:30}")
    private int timeoutSeconds;

    public boolean isApiKeyValid() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-openai-api-key-here");
    }

    public Flux<String> streamChatCompletion(com.project.mog.dto.ChatRequest chatRequest) {
        log.info("OpenAI API 호출 시작 - 메시지: {}", chatRequest.getMessages());
        
        if (!isApiKeyValid()) {
            log.error("OpenAI API 키가 설정되지 않았습니다");
            return Flux.just("data: {\"error\": \"OpenAI API 키가 설정되지 않았습니다.\"}\n\n");
        }

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4o",
            "messages", chatRequest.getMessages(),
            "stream", true,
            "temperature", 0.7,
            "max_tokens", 1000
        );

        log.info("OpenAI API 요청 본문: {}", requestBody);
        log.info("OpenAI API 키 확인: {}", apiKey.substring(0, 10) + "...");
        
        return openaiWebClient
                .post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> log.info("OpenAI 응답 청크: {}", chunk))
                .map(this::extractContent)
                .doOnNext(content -> log.info("추출된 내용: {}", content))
                .filter(content -> !content.isEmpty())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(error -> {
                    log.error("OpenAI API 호출 실패: {}", error.getMessage(), error);
                    return Flux.just("data: {\"error\": \"OpenAI API 호출 중 오류가 발생했습니다.\"}\n\n");
                });
    }

    private String extractContent(String chunk) {
        log.info("extractContent 입력: {}", chunk);
        
        // [DONE] 체크
        if (chunk.trim().equals("[DONE]")) {
            log.info("DONE 신호 감지");
            return "[DONE]";
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(chunk);
            log.info("JSON 파싱 성공: {}", jsonNode);

            if (jsonNode.has("choices") && jsonNode.get("choices").isArray() && jsonNode.get("choices").size() > 0) {
                com.fasterxml.jackson.databind.JsonNode choice = jsonNode.get("choices").get(0);
                log.info("choice: {}", choice);
                
                if (choice.has("delta")) {
                    com.fasterxml.jackson.databind.JsonNode delta = choice.get("delta");
                    log.info("delta: {}", delta);
                    
                    if (delta.has("content")) {
                        String content = delta.get("content").asText();
                        log.info("추출된 content: '{}'", content);
                        
                        if (!content.isEmpty()) {
                            log.info("최종 결과: {}", content);
                            return content;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage(), e);
        }

        log.info("추출 실패, 빈 문자열 반환");
        return "";
    }
}
