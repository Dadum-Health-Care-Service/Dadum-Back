package com.project.mog.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Long locationId;
    private Long userId;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
}
