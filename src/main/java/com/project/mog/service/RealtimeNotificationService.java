package com.project.mog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.project.mog.repository.transaction.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class RealtimeNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 이상거래 알림 전송
     */
    public void sendFraudAlert(Transaction transaction) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "fraud_alert");
        alert.put("transactionId", transaction.getTransactionId());
        alert.put("userId", transaction.getUserId());
        alert.put("amount", transaction.getAmount());
        alert.put("riskScore", transaction.getRiskScore());
        alert.put("message", transaction.getRecommendation());
        alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 모든 구독자에게 알림 전송
        messagingTemplate.convertAndSend("/topic/fraud-alerts", alert);
        
        System.out.println("🚨 이상거래 알림 전송: " + transaction.getTransactionId());
    }

    /**
     * 통계 업데이트 알림 전송
     */
    public void sendStatsUpdate(Map<String, Object> stats) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "stats_update");
        update.putAll(stats);
        update.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 모든 구독자에게 통계 업데이트 전송
        messagingTemplate.convertAndSend("/topic/stats-updates", update);
    }

    /**
     * 시스템 상태 알림 전송
     */
    public void sendSystemStatus(String status, String message) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("type", "system_status");
        statusUpdate.put("status", status);
        statusUpdate.put("message", message);
        statusUpdate.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 모든 구독자에게 시스템 상태 전송
        messagingTemplate.convertAndSend("/topic/system-status", statusUpdate);
    }

    /**
     * 특정 사용자에게 개인 알림 전송
     */
    public void sendPersonalAlert(String userId, Map<String, Object> alert) {
        alert.put("type", "personal_alert");
        alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 특정 사용자에게만 알림 전송
        messagingTemplate.convertAndSendToUser(userId, "/queue/personal-alerts", alert);
    }
}
