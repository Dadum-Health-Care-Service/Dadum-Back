package com.project.mog.repository.post;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Clob;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.project.mog.repository.comment.CommentEntity;
import com.project.mog.repository.like.LikeEntity;
import com.project.mog.repository.users.UsersEntity;

@Entity                         
@Table(name = "posts")
@Getter @Setter                 
@NoArgsConstructor              
@AllArgsConstructor             
@Builder                        
public class PostEntity {
    @Id
    @SequenceGenerator(name = "SEQ_POST_GENERATOR",sequenceName = "SEQ_POST",allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="SEQ_POST_GENERATOR" )
    private Long postId;         // 글 고유번호 (PK)

    @Column(length = 100, nullable = false)
    private String postTitle;    // 글 제목

    @Column(length = 2000, nullable = false)
    private String postContent;  // 글 내용
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "CLOB")
    private String postImage;    // 이미지 URL (nullable)

    private LocalDateTime postRegDate; // 등록 시각
    private LocalDateTime postUpDate;  // 수정 시각
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usersId", referencedColumnName = "usersId", nullable = true)
	private UsersEntity user;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeEntity> likes = new ArrayList<>();
 }
