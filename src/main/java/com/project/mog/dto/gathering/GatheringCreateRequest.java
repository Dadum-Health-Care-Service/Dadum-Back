package com.project.mog.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringCreateRequest {
    
    private String title;
    private String description;
    private String category;
    private Integer maxParticipants;
    private Double latitude;
    private Double longitude;
    private String address;
    private String scheduleType;
    private String scheduleDetails;
    private String nextMeetingDate;
    private String meetingTime;
    private String dayOfWeek;
}
