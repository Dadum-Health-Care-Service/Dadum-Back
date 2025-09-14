package com.project.mog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotNull(message = "메시지 목록은 필수입니다")
    @NotEmpty(message = "메시지 목록은 비어있을 수 없습니다")
    @Valid
    private List<ChatMessage> messages;
}

