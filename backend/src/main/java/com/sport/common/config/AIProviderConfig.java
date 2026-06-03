package com.sport.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIProviderConfig {

    private String defaultProvider = "kimi";
    private Map<String, Provider> providers = new HashMap<>();

    // 当前激活的提供商 ID（运行时可切换）
    private volatile String activeProviderId;

    public String getActiveProviderId() {
        return activeProviderId != null ? activeProviderId : defaultProvider;
    }

    /**
     * 获取当前激活的提供商配置
     */
    public Provider getActiveProvider() {
        String id = getActiveProviderId();
        Provider provider = providers.get(id);
        if (provider == null) {
            throw new IllegalStateException("AI 提供商未配置: " + id);
        }
        return provider;
    }

    /**
     * 切换激活的提供商
     */
    public void switchProvider(String id) {
        if (!providers.containsKey(id)) {
            throw new IllegalArgumentException("未知的 AI 提供商: " + id);
        }
        this.activeProviderId = id;
    }

    /**
     * 获取指定 ID 的提供商配置
     */
    public Provider getProvider(String id) {
        if (id == null || id.isBlank()) {
            return getActiveProvider();
        }
        Provider provider = providers.get(id);
        if (provider == null) {
            return getActiveProvider();
        }
        return provider;
    }

    /**
     * 获取所有提供商列表（API Key 脱敏）
     */
    public List<ProviderInfo> getAllProviders() {
        return providers.entrySet().stream()
                .map(e -> new ProviderInfo(
                        e.getKey(),
                        e.getValue().getName(),
                        e.getValue().getModel(),
                        e.getKey().equals(getActiveProviderId())
                ))
                .toList();
    }

    @Data
    public static class Provider {
        private String name;
        private String apiKey;
        private String baseUrl;
        private String model;
    }

    @Data
    public static class ProviderInfo {
        private final String id;
        private final String name;
        private final String model;
        private final boolean active;
    }
}
