package com.project.mog.service.post;

import com.project.mog.repository.post.PostEntity;
import com.project.mog.repository.post.PostRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service                        
@RequiredArgsConstructor       
public class PostService {
	private final UsersRepository usersRepository;
    private final PostRepository postRepository;

    // 게시글 저장
    public PostDto create(String email,PostDto dto) {
    	UsersEntity user = usersRepository.findByEmail(email).orElseThrow();
    	PostEntity post = dto.toEntity();
    	post.setUser(user);
        // 저장하고 영속화된 Entity 리턴
        PostEntity saved = postRepository.save(post);
        // Entity → DTO 변환 후 리턴
        return PostDto.toDto(saved);
    }

    // 모든 게시글 조회
    public List<PostDto> listAll(String email) {
    	UsersEntity user = usersRepository.findByEmail(email).orElseThrow();
    	List<PostEntity> post = postRepository.findAllOrderByRegDate(user.getUsersId());
        return post.stream().map(PostDto::toDto).collect(Collectors.toList());
    }
    //게시들 단일 조회
    public PostDto getById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다: id=" + id));

        return PostDto.toDto(post);
    }
    //게시글 수정
    public PostDto update(Long id, PostDto dto) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));

        post.setPostTitle(dto.getPostTitle());
        post.setPostContent(dto.getPostContent());
        post.setPostImage(dto.getPostImage());
        post.setPostUpDate(java.time.LocalDateTime.now());

        PostEntity updated = postRepository.save(post);

        return PostDto.toDto(updated);
    }
    //게시글 삭제
    public PostDto delete(Long id) {
    	PostEntity post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));;
    	postRepository.delete(post);
        return PostDto.toDto(post);
    }
    
    public void deleteByUsersId(Long usersId) {
    	UsersEntity usersEntity = usersRepository.findById(usersId)
    			.orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. usersID: "+usersId));
    	List<PostEntity> posts = postRepository.findAllOrderByRegDate(usersId);
    	postRepository.deleteAll(posts);
    }

	public List<PostDto> totalListAll() {
		List<PostEntity> posts = postRepository.findAll();
		return posts.stream().map(PostDto::toDto).toList();
	}
    
}
