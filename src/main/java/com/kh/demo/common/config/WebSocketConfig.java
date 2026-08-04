package com.kh.demo.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

// 거래 허브 종목 채팅용 WebSocket(STOMP) 설정
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                // HttpSession의 attribute(로그인 회원 정보 포함)를 WebSocket 세션으로 그대로 복사.
                // 별도 토큰 인증 없이 기존 세션 로그인 상태를 그대로 재사용하기 위함
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독하는 목적지 prefix. 종목별 방은 /topic/chat/{stockCode}
        registry.enableSimpleBroker("/topic");
        // 클라이언트가 서버로 보낼 때 붙는 prefix. StockChatController의 @MessageMapping과 연결됨
        registry.setApplicationDestinationPrefixes("/app");
    }
}
