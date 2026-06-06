package com.sport.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Kimi 聊天请求 DTO
 */
@Data
@Schema(description = "Kimi AI 对话请求")
public class KimiChatRequest {

    @Schema(description = "对话消息列表")
    private List<Map<String, String>> messages;

    @Schema(description = "模型提供商ID")
    private String modelId;

    @Schema(description = "临时 API Key，优先于后端配置")
    private String apiKey;
}
