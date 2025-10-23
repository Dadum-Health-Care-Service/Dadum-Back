package com.project.mog.repository.gathering;

import com.project.mog.repository.users.UsersEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "gathering_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringParticipantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private GatheringEntity gathering;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @PrePersist
    public void setJoinedAt() {
        this.joinedAt = LocalDateTime.now();
    }
}
