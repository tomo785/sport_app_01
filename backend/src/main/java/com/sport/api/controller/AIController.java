package com.sport.api.controller;

import com.sport.api.dto.KimiChatRequest;
import com.sport.common.config.AIProviderConfig;
import com.sport.common.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import org.springframework.http.*;

/**
 * AI 控制器 — 支持多模型提供商
 */
@Slf4j
@Tag(name = "AI", description = "AI 教练：基于大模型生成训练方案")
@RestController
@RequestMapping("/ai")
public class AIController {

    private final RestTemplate restTemplate;
    private final AIProviderConfig providerConfig;

    public AIController(RestTemplate restTemplate, AIProviderConfig providerConfig) {
        this.restTemplate = restTemplate;
        this.providerConfig = providerConfig;
    }

    @Operation(summary = "获取可用模型列表")
    @GetMapping("/models")
    public Result<List<AIProviderConfig.ProviderInfo>> getModels() {
        return Result.success(providerConfig.getAllProviders());
    }

    @Operation(summary = "切换激活模型")
    @PostMapping("/switch-model")
    public Result<String> switchModel(@RequestBody Map<String, String> body) {
        String modelId = body.get("modelId");
        if (modelId == null || modelId.isBlank()) {
            return Result.fail("modelId 不能为空");
        }
        try {
            providerConfig.switchProvider(modelId);
            log.info("AI 模型已切换为: {}", modelId);
            return Result.success("已切换为 " + providerConfig.getActiveProvider().getName());
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "生成训练方案")
    @PostMapping("/generate-plan")
    public Result<Map<String, Object>> generatePlan(@RequestBody KimiChatRequest request) {
        AIProviderConfig.Provider provider = providerConfig.getActiveProvider();

        if (provider.getApiKey() == null || provider.getApiKey().isBlank()) {
            log.warn("AI API Key 未配置: {}", provider.getName());
            return Result.fail("AI 服务未配置，请联系管理员");
        }

        String url = provider.getBaseUrl() + "/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", provider.getModel());
        body.put("messages", request.getMessages());
        body.put("temperature", 1.0);  // 保持 1.0 以确保 AI 输出的多样性和创造性

        String keyMask = maskKey(provider.getApiKey());
        try {
            log.info(">>> 请求 AI: provider={}, model={}, keyMask={}", provider.getName(), provider.getModel(), keyMask);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(provider.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            log.info("<<< AI 响应成功: status={}", response.getStatusCode());
            return Result.success(response.getBody());

        } catch (Exception e) {
            log.error("调用 AI API 失败: provider={}, error={}", provider.getName(), e.getMessage());
            String msg = e.getMessage();
            if (msg != null && (msg.contains("401") || msg.contains("Unauthorized"))) {
                return Result.fail("AI 认证失败，请检查后端配置的 API Key 是否有效");
            }
            return Result.fail("AI 服务调用失败: " + msg);
        }
    }

    /**
     * 测试 AI 连接（支持自定义 API Key）
     */
    @Operation(summary = "测试 AI 连接")
    @PostMapping("/test-connection")
    public Result<String> testConnection(@RequestBody Map<String, String> body) {
        String modelId = body.get("modelId");
        String customApiKey = body.get("apiKey");

        AIProviderConfig.Provider provider = providerConfig.getProvider(modelId);
        String apiKey = (customApiKey != null && !customApiKey.isBlank()) ? customApiKey : provider.getApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            return Result.fail("API Key 未配置");
        }

        String url = provider.getBaseUrl() + "/chat/completions";
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", provider.getModel());
        reqBody.put("messages", List.of(Map.of("role", "user", "content", "hi")));
        reqBody.put("max_tokens", 5);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return Result.success("连接成功");
            } else {
                return Result.fail("连接失败: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("测试 AI 连接失败: provider={}, error={}", provider.getName(), e.getMessage());
            String msg = e.getMessage();
            if (msg != null && (msg.contains("401") || msg.contains("Unauthorized"))) {
                return Result.fail("认证失败，请检查 API Key 是否有效");
            }
            return Result.fail("连接失败: " + msg);
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 12) {
            return "***";
        }
        return key.substring(0, 6) + "***" + key.substring(key.length() - 4);
    }
}
