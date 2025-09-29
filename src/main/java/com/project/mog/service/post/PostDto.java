package com.project.mog.service.post;

import lombok.*;
import java.time.LocalDateTime;

import com.project.mog.repository.post.PostEntity;
import com.project.mog.service.users.UsersDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Data               
@NoArgsConstructor  
@AllArgsConstructor 
@Builder            
public class PostDto {
    @Schema(hidden = true)
    private Long postId;        // 글 번호  
    private String postTitle;   // 제목
    private String postContent; // 내용
    private String postImage;   // 이미지 URL
    @Schema(hidden = true)
    private LocalDateTime postRegDate; // 등록 시각
    @Schema(hidden = true)
    private LocalDateTime postUpDate;  // 수정 시각
    @Schema(hidden = true)
    private Long usersId;        // 작성자 ID


    public PostEntity toEntity() {
    return PostEntity.builder()
            .postTitle(this.postTitle)
            .postContent(this.postContent)
            .postImage(this.postImage)
            .postRegDate(LocalDateTime.now())  // 등록 시각 자동 세팅
            .postUpDate(null)                  // 수정 시각은 처음엔 null
            .build();
    }
    
    public static PostDto toDto(PostEntity postEntity) {
    	if (postEntity==null) return null;
    	return PostDto.builder()
    			.postId(postEntity.getPostId())
    			.usersId(postEntity.getUser().getUsersId())
    			.postTitle(postEntity.getPostTitle())
    			.postContent(postEntity.getPostContent())
    			.postImage(postEntity.getPostImage())
    			.postRegDate(postEntity.getPostRegDate())
    			.postUpDate(postEntity.getPostUpDate())
    			.build();
    }
}
