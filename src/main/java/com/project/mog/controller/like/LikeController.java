package com.project.mog.controller.like;

import com.project.mog.service.like.LikeService;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.like.LikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/likes")
@Tag(name = "좋아요 관리", description = "게시글 좋아요 관련 API")
public class LikeController {
	private final JwtUtil jwtUtil;
    private final LikeService likeService;
    
    @GetMapping
    @Operation(summary = "좋아요 정보 조회", description = "특정 게시글의 좋아요 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<LikeResponseDto> getLikes(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId){
    	LikeResponseDto response = likeService.getLikes(postId);
    	return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "좋아요 토글", description = "게시글에 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<LikeResponseDto> toggleLike(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
        LikeResponseDto response = likeService.toggleLike(postId, authEmail);
        return ResponseEntity.ok(response);
    }
}