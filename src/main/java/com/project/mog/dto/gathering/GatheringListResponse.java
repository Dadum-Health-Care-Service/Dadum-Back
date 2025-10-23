package com.project.mog.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringListResponse {
    
    private List<GatheringResponse> gatherings;
    private Long totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
}
