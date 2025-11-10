package com.project.mog.service.discord;

import com.project.mog.service.discord.ThreatReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookService {
    
    private final WebClient discordWebClient;
    
    @Value("${discord.webhook.url:}")
    private String webhookUrl;
    
    public Mono<String> sendThreatReport(ThreatReport threatReport) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("디스코드 웹훅 URL이 설정되지 않았습니다.");
            return Mono.just("웹훅 URL 미설정");
        }
        
        String message = formatThreatMessage(threatReport);
        
        DiscordWebhookPayload payload = new DiscordWebhookPayload();
        payload.setContent(message);
        
        return discordWebClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("디스코드 메시지 전송 성공"))
                .doOnError(error -> log.error("디스코드 메시지 전송 실패: {}", error.getMessage()));
    }
    
    private String formatThreatMessage(ThreatReport threatReport) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 **보안 위협 감지** 🚨\n\n");
        message.append("**위협 수준**: ").append(threatReport.getThreat_level()).append("\n");
        message.append("**요약**: ").append(threatReport.getSummary()).append("\n");
        message.append("**사유**: ").append(threatReport.getReason()).append("\n\n");
        
        if (threatReport.getRecommendations() != null && !threatReport.getRecommendations().isEmpty()) {
            message.append("**권장 조치사항**:\n");
            for (int i = 0; i < threatReport.getRecommendations().size(); i++) {
                message.append(i + 1).append(". ").append(threatReport.getRecommendations().get(i)).append("\n");
            }
        }
        
        return message.toString();
    }
    
    // 디스코드 웹훅 페이로드 내부 클래스
    private static class DiscordWebhookPayload {
        private String content;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}