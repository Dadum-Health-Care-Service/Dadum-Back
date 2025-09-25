package com.project.mog.service.users;

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
public class HomeStatsDto {
    private int consecutiveDays;
    private int totalRoutines;
    private String totalTime;
    private String consecutiveMessage; // e.g., "지금 시작해보세요"
    private String routinesMessage;    // e.g., "루틴을 만들어 보세요"
    private String timeMessage;        // e.g., "지금 시작해보세요"
}


