package com.project.mog.service.comment.dto;

import com.project.mog.repository.comment.CommentEntity;
import lombok.Getter;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
public class CommentResponseDto {
    @Schema(hidden = true)
    private final Long commentId;
    private final String content;
    private final String userName;
    @Schema(hidden = true)
    private final Long userId;
    @Schema(hidden = true)
    private final LocalDateTime createdAt;
    @Schema(hidden = true)
    private final Long postId;

    public CommentResponseDto(CommentEntity comment) { 
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.userName = comment.getUser().getUsersName();
        this.userId = comment.getUser().getUsersId();
        this.createdAt = comment.getCreatedAt();
        this.postId = comment.getPost().getPostId();
    }
}