package com.project.mog.controller.chatbot;

import com.project.mog.dto.ChatRequest;
import com.project.mog.service.chatbot.OpenAIClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "챗봇", description = "AI 챗봇 관련 API")
public class ChatController {
    
    private final OpenAIClient openAIClient;
    
    // SSE 헤더를 상수로 분리
    private static final String CONTENT_TYPE_SSE = "text/event-stream; charset=utf-8";
    private static final String CACHE_CONTROL = "no-cache, no-transform";
    private static final String CONNECTION = "keep-alive";
    
    // 에러 메시지를 상수로 분리
    private static final String API_KEY_ERROR_MESSAGE = "data: {\"error\": \"OpenAI API 키가 설정되지 않았습니다.\"}\n\n";
    private static final String STREAM_ERROR_MESSAGE = "data: {\"error\": \"챗봇 응답 생성 중 오류가 발생했습니다.\"}\n\n";
    
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "챗봇 스트리밍 대화", description = "OpenAI API를 사용한 실시간 스트리밍 대화를 제공합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "스트리밍 시작"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Flux<String>> streamChat(@Valid @RequestBody ChatRequest chatRequest) {
        if (!openAIClient.isApiKeyValid()) {
            log.error("OpenAI API 키가 설정되지 않았습니다");
            return createSSEResponse(Flux.just(API_KEY_ERROR_MESSAGE));
        }
        
        Flux<String> stream = openAIClient.streamChatCompletion(chatRequest)
                .map(this::formatSSE)
                .onErrorResume(error -> {
                    log.error("챗봇 스트리밍 중 오류 발생", error);
                    return Flux.just(STREAM_ERROR_MESSAGE);
                });
        
        return createSSEResponse(stream);
    }
    
    @GetMapping("/status")
    @Operation(summary = "챗봇 상태 확인", description = "챗봇 서비스의 현재 상태를 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 조회 성공")
    })
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
    
    /**
     * SSE 응답을 생성하는 공통 메서드
     */
    private ResponseEntity<Flux<String>> createSSEResponse(Flux<String> body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_SSE)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL)
                .header("Connection", CONNECTION)
                .body(body);
    }
    
    /**
     * SSE 형식으로 데이터 포맷팅
     */
    private String formatSSE(String data) {
        if (data == null || data.trim().isEmpty()) {
            return "data: \n\n";
        }
        
        // [DONE] 신호는 그대로 전송
        if (data.equals("[DONE]")) {
            return "data: [DONE]\n\n";
        }
        
        // 모든 텍스트에 data: 접두사 추가 (SSE 표준 형식)
        return "data: " + data + "\n\n";
    }
}