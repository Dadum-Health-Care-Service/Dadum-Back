package com.project.mog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 토픽 프리픽스 설정
        config.enableSimpleBroker("/topic", "/queue");
        // 서버로 메시지를 보낼 때 사용할 프리픽스 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws/fraud-monitor")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 모임 기능을 위한 웹소켓 엔드포인트 추가
        registry.addEndpoint("/ws/gathering")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
