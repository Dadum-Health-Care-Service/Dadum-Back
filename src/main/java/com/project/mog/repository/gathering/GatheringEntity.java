package com.project.mog.repository.gathering;

import com.project.mog.repository.users.UsersEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gatherings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gatheringId;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false, length = 20)
    private String category;
    
    @Column(nullable = false)
    private Integer maxParticipants;
    
    @Column(nullable = false)
    private Integer currentParticipants;
    
    @Column(nullable = false, length = 200)
    private String address;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType scheduleType;
    
    @Column(length = 200)
    private String scheduleDetails; // 일정 상세 정보 (예: "매주 화요일", "2024-01-15", "매월 첫째 주 일요일")
    
    @Column
    private LocalDateTime nextMeetingDate; // 다음 모임 일시
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UsersEntity creator;
    
    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<GatheringParticipantEntity> participants = new ArrayList<>();
    
    @PrePersist
    public void setCreateDate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentParticipants = 0;
        this.status = "ACTIVE";
        if (this.scheduleType == null) {
            this.scheduleType = ScheduleType.ONE_TIME;
        }
    }
    
    @PreUpdate
    public void setUpdateDate() {
        this.updatedAt = LocalDateTime.now();
    }
}
