package com.sport.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sport.common.config.AIProviderConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AI 服务 — 支持多模型提供商（Kimi/DeepSeek 等），使用 OkHttp 流式调用
 */
@Slf4j
@Service
public class AIService {

    private final AIProviderConfig providerConfig;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public AIService(AIProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    /**
     * 流式调用 AI（使用默认激活模型）
     */
    public void chatCompletionStream(List<Map<String, String>> messages,
                                     Consumer<String> onDelta,
                                     Consumer<Throwable> onError,
                                     Runnable onComplete) {
        chatCompletionStream(null, messages, onDelta, onError, onComplete);
    }

    /**
     * 流式调用 AI（指定模型）
     */
    public void chatCompletionStream(String modelId,
                                     List<Map<String, String>> messages,
                                     Consumer<String> onDelta,
                                     Consumer<Throwable> onError,
                                     Runnable onComplete) {
        AIProviderConfig.Provider provider = providerConfig.getProvider(modelId);

        if (provider.getApiKey() == null || provider.getApiKey().isBlank()) {
            onError.accept(new IllegalStateException("AI API Key 未配置: " + provider.getName()));
            return;
        }

        String keyMask = maskKey(provider.getApiKey());
        try {
            log.info(">>> 流式请求 AI: provider={}, model={}, keyMask={}", provider.getName(), provider.getModel(), keyMask);
            executeStream(provider, messages, onDelta, onError, onComplete);
        } catch (Exception e) {
            log.error("AI 流式调用失败: provider={}, error={}", provider.getName(), e.getMessage());
            onError.accept(new RuntimeException("AI 服务调用失败: " + e.getMessage(), e));
        }
    }

    private void executeStream(AIProviderConfig.Provider provider, List<Map<String, String>> messages,
                               Consumer<String> onDelta, Consumer<Throwable> onError, Runnable onComplete) throws IOException {
        Map<String, Object> body = Map.of(
                "model", provider.getModel(),
                "messages", messages,
                "temperature", 1.0,  // 保持 1.0 以确保 AI 输出的多样性和创造性
                "stream", true
        );

        Request request = new Request.Builder()
                .url(provider.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + provider.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(JSON.toJSONBytes(body), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "empty";
                throw new IOException("HTTP " + response.code() + ": " + errBody);
            }

            try (BufferedReader reader = new BufferedReader(response.body().charStream())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String json = line.substring(6);
                        if ("[DONE]".equals(json.trim())) {
                            break;
                        }
                        try {
                            JSONObject obj = JSON.parseObject(json);
                            var choices = obj.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                if (delta != null) {
                                    String content = delta.getString("content");
                                    if (content != null && !content.isEmpty()) {
                                        onDelta.accept(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析 SSE chunk 失败: {}", json);
                        }
                    }
                }
            }
            onComplete.run();
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 12) {
            return "***";
        }
        return key.substring(0, 6) + "***" + key.substring(key.length() - 4);
    }
}
