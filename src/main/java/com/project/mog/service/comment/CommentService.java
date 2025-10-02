package com.project.mog.service.comment;

import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.auth.AuthRepository;
import com.project.mog.repository.comment.CommentEntity;
import com.project.mog.repository.comment.CommentRepository;
import com.project.mog.repository.post.PostEntity;
import com.project.mog.repository.post.PostRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.comment.dto.CommentResponseDto;
import com.project.mog.service.comment.dto.CommentSaveRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthRepository authRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, String email, CommentSaveRequestDto requestDto) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));
        
        UsersEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + email));

        CommentEntity comment = CommentEntity.of(post, user, requestDto.getContent());
        CommentEntity savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }
    
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPost(Long postId) {
        List<CommentEntity> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments(String authEmail) {
    	UsersEntity user = usersRepository.findByEmail(authEmail).orElseThrow();
        List<CommentEntity> comments = commentRepository.findByUserId(user.getUsersId());
        return comments.stream().map(CommentResponseDto::new).collect(Collectors.toList());
	}
    
    @Transactional
    public void deleteComment(Long commentId, String email) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        if (comment.getUser().getEmail() != email) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

	public CommentResponseDto updateComment(String authEmail, Long postId, Long commentId, CommentSaveRequestDto requestDto) {
		PostEntity post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));
		UsersEntity user = usersRepository.findByEmail(authEmail)
	            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + authEmail));
		CommentEntity comment = commentRepository.findByUserIdAndPostId(user.getUsersId(),post.getPostId());
		comment.setContent(requestDto.getContent());
		CommentEntity savedComment = commentRepository.save(comment);
		return new CommentResponseDto(savedComment);
	}
    
    
	
}