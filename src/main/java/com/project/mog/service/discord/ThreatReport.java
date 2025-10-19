package com.project.mog.service.discord;

import java.util.List;

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
public class ThreatReport {
    private ThreatLevel threat_level;
    private String summary;
    private String reason;
    private List<String> recommendations;

    public enum ThreatLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}