package com.project.mog.controller.post;

import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.comment.dto.CommentResponseDto;
import com.project.mog.service.post.PostDto;
import com.project.mog.service.post.PostService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "게시글 관리", description = "게시글 관련 API")
public class PostController {
	private final JwtUtil jwtUtil;
    private final PostService postService;

    // 1) 게시글 등록
    @PostMapping
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 작성 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<PostDto> create(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,@RequestBody PostDto dto) {
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
    	PostDto created = postService.create(authEmail,dto);
        return ResponseEntity.ok(created);
    }

    // 2) 회원 게시글 목록 조회 
    @GetMapping
    @Operation(summary = "내 게시글 목록 조회", description = "로그인한 사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<PostDto>> list(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    	String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
        return ResponseEntity.ok(postService.listAll(authEmail));
    }

    // 3) 게시글 수정
    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<PostDto> updatePost(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long id, @RequestBody PostDto dto) {
        PostDto updated = postService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // 4) 회원 게시글 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<PostDto> getPost(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long id) {
        PostDto post = postService.getById(id);
        return ResponseEntity.ok(post);
    }
    
    //5) 게시글 전체 조회 (모든 게시글)
    @GetMapping("/list")
    @Operation(summary = "전체 게시글 목록 조회", description = "모든 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<PostDto>> getTotalList() {
        return ResponseEntity.ok(postService.totalListAll());
    }


    // 6) 게시글 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<PostDto> deletePost(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long id) {
        PostDto post = postService.delete(id);
        return ResponseEntity.ok(post); 
    }

    // 7) 이미지 업로드 API
    @PostMapping("/upload")
    @Operation(summary = "이미지 업로드", description = "게시글에 첨부할 이미지를 업로드합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업로드 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일"),
        @ApiResponse(responseCode = "500", description = "업로드 실패")
    })
    public ResponseEntity<String> uploadImage(@Parameter(description = "업로드할 이미지 파일") @RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "C:/uploads";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File saveFile = new File(uploadDir, fileName);
            file.transferTo(saveFile);

            String imageUrl = "/images/" + fileName;
            System.out.println("✔ 이미지 저장 성공: " + saveFile.getAbsolutePath());
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            System.out.println("❌ 이미지 저장 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("이미지 업로드 실패");
        }
    }
    
}