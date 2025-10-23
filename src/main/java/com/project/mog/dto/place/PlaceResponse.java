package com.project.mog.dto.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponse {
    private Long placeId;
    private String placeName;
    private String categoryName;
    private String addressName;
    private String roadAddressName;
    private String phone;
    private String placeUrl;
    private Double latitude;
    private Double longitude;
    private String keyword;
    private LocalDateTime createdAt;
}
