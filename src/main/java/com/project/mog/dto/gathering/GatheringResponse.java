package com.project.mog.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringResponse {
    
    private Long gatheringId;
    private String title;
    private String description;
    private String category;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String address;
    private Double latitude;
    private Double longitude;
    private String status;
    private String scheduleType;
    private String scheduleDetails;
    private LocalDateTime nextMeetingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long creatorId;
    private String creatorName;
    private String creatorNickname;
    private List<ParticipantResponse> participants;
}
