package com.project.mog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챗봇 대화 요청 DTO")
public class ChatRequest {
    @NotNull(message = "메시지 목록은 필수입니다")
    @NotEmpty(message = "메시지 목록은 비어있을 수 없습니다")
    @Valid
    @Schema(description = "대화 메시지 목록", required = true)
    private List<ChatMessage> messages;
}

