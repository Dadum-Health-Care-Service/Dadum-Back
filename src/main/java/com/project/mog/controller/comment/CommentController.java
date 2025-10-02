package com.project.mog.controller.comment;

import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.comment.CommentService;
import com.project.mog.service.comment.dto.CommentResponseDto;
import com.project.mog.service.comment.dto.CommentSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "댓글 관리", description = "댓글 관련 API")
public class CommentController {
	private final JwtUtil jwtUtil;
    private final CommentService commentService;
    
    @GetMapping("/comments/list")
    @Operation(summary = "전체 댓글 목록 조회", description = "사용자가 작성한 모든 댓글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<CommentResponseDto>> getAllComments(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader){
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
    	return ResponseEntity.ok(commentService.getAllComments(authEmail));
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<List<CommentResponseDto>> getComments(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "특정 게시글에 새로운 댓글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<CommentResponseDto> addComment(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId, @RequestBody CommentSaveRequestDto requestDto) {
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
        CommentResponseDto newComment = commentService.createComment(postId, authEmail, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentSaveRequestDto requestDto
    ) {
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
        CommentResponseDto updated = commentService.updateComment(authEmail,postId,commentId, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다. 본인이 작성한 댓글만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    public ResponseEntity<Void> removeComment(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId, @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId) {
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token); 
        commentService.deleteComment(commentId, authEmail);
        return ResponseEntity.noContent().build();
    }
}