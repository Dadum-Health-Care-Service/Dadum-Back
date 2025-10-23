package com.project.mog.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponse {
    
    private Long participantId;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String role;
    private LocalDateTime joinedAt;
}
