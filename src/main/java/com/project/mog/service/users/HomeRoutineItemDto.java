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
public class HomeRoutineItemDto {
    private long id;
    private String title;
    private String time;       // e.g., "15분"
    private String difficulty; // e.g., "쉬움"
    private String icon;       // optional emoji/icon
    private boolean completed; // last completion state
}


