package com.project.mog.controller.discord;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mog.service.discord.ThreatReport;
import com.project.mog.service.discord.DiscordWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/discord")
@RequiredArgsConstructor
@Slf4j
public class DiscordController {
    private final DiscordWebhookService discordWebhookService;

    @PostMapping("/threat-report")
    public ResponseEntity<Mono<String>> receiveThreatReport(@RequestBody ThreatReport threatReport) {
        log.info("위협 보고서 수신: {}", threatReport.getThreat_level());
        
        Mono<String> result = discordWebhookService.sendThreatReport(threatReport)
                .doOnSuccess(response -> log.info("디스코드 전송 완료: {}", response))
                .doOnError(error -> log.error("디스코드 전송 실패: {}", error.getMessage()));
        
        return ResponseEntity.ok(result);
    }

}