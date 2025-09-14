package com.project.mog.controller;

import com.project.mog.dto.ChatRequest;
import com.project.mog.service.OpenAIClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final OpenAIClient openAIClient;
    
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest chatRequest) {
        if (!openAIClient.isApiKeyValid()) {
            log.error("OpenAI API 키가 설정되지 않았습니다");
            return Flux.just("data: {\"error\": \"OpenAI API 키가 설정되지 않았습니다.\"}\n\n");
        }
        
        return openAIClient.streamChatCompletion(chatRequest)
                .map(this::formatSSE)
                .onErrorResume(error -> {
                    log.error("챗봇 스트리밍 중 오류 발생", error);
                    return Flux.just("data: {\"error\": \"챗봇 응답 생성 중 오류가 발생했습니다.\"}\n\n");
                });
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getChatbotStatus() {
        log.info("챗봇 상태 확인 요청");
        
        boolean isApiKeyValid = openAIClient.isApiKeyValid();
        
        Map<String, Object> status = Map.of(
            "status", "running",
            "apiKeyConfigured", isApiKeyValid,
            "message", isApiKeyValid ? "챗봇이 정상적으로 작동 중입니다" : "OpenAI API 키가 설정되지 않았습니다",
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(status);
    }
    
    private String formatSSE(String data) {
        if (data == null || data.trim().isEmpty()) {
            return "data: \n\n";
        }
        
        // [DONE] 신호는 그대로 전송
        if (data.equals("[DONE]")) {
            return "data: [DONE]\n\n";
        }
        
        // 순수 텍스트는 data: 접두사 없이 전송
        return data + "\n";
    }
}
