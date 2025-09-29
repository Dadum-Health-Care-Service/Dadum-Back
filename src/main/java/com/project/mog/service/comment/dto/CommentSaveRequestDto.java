package com.project.mog.service.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "댓글 작성 요청 DTO")
public class CommentSaveRequestDto {
    @Schema(description = "댓글 내용", example = "좋은 글이네요!")
    private String content;
}