package com.sport.common.config;

import com.sport.api.ws.AIWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置 — 用于 AI 流式对话
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AIWebSocketHandler aiWebSocketHandler;
    private final TokenHandshakeInterceptor tokenHandshakeInterceptor;

    public WebSocketConfig(AIWebSocketHandler aiWebSocketHandler,
                           TokenHandshakeInterceptor tokenHandshakeInterceptor) {
        this.aiWebSocketHandler = aiWebSocketHandler;
        this.tokenHandshakeInterceptor = tokenHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(aiWebSocketHandler, "/ws/ai")
                .setAllowedOrigins("*")
                .addInterceptors(tokenHandshakeInterceptor);
    }
}
