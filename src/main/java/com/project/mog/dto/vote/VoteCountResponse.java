package com.project.mog.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteCountResponse {
    private String placeUrl;
    private Long upvotes;
    private Long downvotes;
}
