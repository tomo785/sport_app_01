package com.sport.api.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI WebSocket 处理器 — 流式推送 AI 回复（支持多模型提供商）
 */
@Slf4j
@Component
public class AIWebSocketHandler extends TextWebSocketHandler {

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public AIWebSocketHandler(AIService aiService, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket 连接建立: sessionId={}, userId={}", session.getId(), userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: sessionId={}, payloadLength={}", session.getId(), payload.length());

        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText();

            if (!"chat".equals(type)) {
                sendJson(session, Map.of("type", "error", "message", "未知消息类型: " + type));
                return;
            }

            // 解析 messages
            List<Map<String, String>> messages = new ArrayList<>();
            JsonNode messagesNode = root.path("messages");
            if (messagesNode.isArray()) {
                for (JsonNode msg : messagesNode) {
                    String role = msg.path("role").asText();
                    String content = msg.path("content").asText();
                    Map<String, String> m = new HashMap<>();
                    m.put("role", role);
                    m.put("content", content);
                    messages.add(m);
                }
            }

            // 解析 modelId
            String modelId = root.path("modelId").asText(null);
            String customApiKey = root.path("apiKey").asText(null);

            if (messages.isEmpty()) {
                sendJson(session, Map.of("type", "error", "message", "messages 不能为空"));
                return;
            }

            // 发送开始标记
            sendJson(session, Map.of("type", "start"));

            // 调用流式 API（同步阻塞读取，但在 WebSocket 线程中执行）
            aiService.chatCompletionStream(
                    modelId,
                    customApiKey,
                    messages,
                    delta -> {
                        if (session.isOpen()) {
                            sendJson(session, Map.of("type", "delta", "content", delta));
                        }
                    },
                    error -> {
                        log.error("流式调用异常: sessionId={}", session.getId(), error);
                        if (session.isOpen()) {
                            sendJson(session, Map.of("type", "error", "message", error.getMessage()));
                        }
                    },
                    () -> {
                        log.debug("流式调用完成: sessionId={}", session.getId());
                        if (session.isOpen()) {
                            sendJson(session, Map.of("type", "done"));
                        }
                    }
            );

        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
            sendJson(session, Map.of("type", "error", "message", "消息处理失败: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 连接关闭: sessionId={}, status={}", session.getId(), status);
    }

    private void sendJson(WebSocketSession session, Map<String, Object> data) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
        } catch (IOException e) {
            log.error("发送 WebSocket 消息失败: sessionId={}", session.getId(), e);
        }
    }
}
