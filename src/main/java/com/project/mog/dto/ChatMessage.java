package com.project.mog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챗봇 메시지 DTO")
public class ChatMessage {
    @Schema(description = "메시지 역할", example = "user", allowableValues = {"user", "assistant", "system"})
    private String role;    // "user", "assistant", "system"
    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content; // 메시지 내용
}

