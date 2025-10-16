package com.project.mog.service.like;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "좋아요 응답 DTO")
public class LikeResponseDto {
    @Schema(hidden = true)
    private final Long postId;
    @Schema(hidden = true)
    private final Long userId;
    private final boolean isLiked;
    private final long likeCount;

    public LikeResponseDto(Long postId, Long userId, boolean isLiked, long likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.isLiked = isLiked;
        this.likeCount = likeCount;
    }
}