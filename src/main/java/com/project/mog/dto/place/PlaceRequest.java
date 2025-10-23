package com.project.mog.dto.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceRequest {
    private String keyword;
    private Double latitude;
    private Double longitude;
    private Double radius;
}
