package com.project.mog.controller;

import com.project.mog.service.RealtimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private RealtimeNotificationService notificationService;

    /**
     * 클라이언트에서 서버로 메시지 전송 시 처리
     */
    @MessageMapping("/fraud-monitor/connect")
    @SendTo("/topic/connection-status")
    public Map<String, Object> handleConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "connection_established");
        response.put("message", "실시간 모니터링 연결됨");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        System.out.println("🔗 실시간 모니터링 클라이언트 연결됨");
        return response;
    }

    /**
     * 클라이언트에서 통계 요청 시 처리
     */
    @MessageMapping("/fraud-monitor/request-stats")
    @SendTo("/topic/stats-response")
    public Map<String, Object> handleStatsRequest() {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "stats_response");
        response.put("message", "통계 데이터 요청됨");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 실제로는 여기서 통계 데이터를 조회하여 전송
        // notificationService.sendStatsUpdate(stats);
        
        return response;
    }

    /**
     * 클라이언트에서 알림 설정 요청 시 처리
     */
    @MessageMapping("/fraud-monitor/notification-settings")
    @SendTo("/topic/notification-settings")
    public Map<String, Object> handleNotificationSettings(Map<String, Object> settings) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "notification_settings_updated");
        response.put("settings", settings);
        response.put("message", "알림 설정이 업데이트되었습니다");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        System.out.println("🔔 알림 설정 업데이트: " + settings);
        return response;
    }
}
