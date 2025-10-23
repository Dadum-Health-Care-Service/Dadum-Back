package com.project.mog.repository.vote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "VOTES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(
    name = "SEQ_VOTE_GENERATOR",
    sequenceName = "SEQ_VOTE",
    allocationSize = 1,
    initialValue = 1
)
public class VoteEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VOTE_GENERATOR")
    @Column(name = "VOTE_ID")
    private Long voteId;
    
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    
    @Column(name = "PLACE_URL", nullable = false, length = 1000)
    private String placeUrl;
    
    @Column(name = "IS_UPVOTE", nullable = false)
    private Boolean isUpvote;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    // 복합 유니크 제약조건: 사용자별 장소별로 하나의 투표만 가능
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"USER_ID", "PLACE_URL"})
    })
    public static class VoteEntityConstraints {}
}
