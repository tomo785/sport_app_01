# Tasks

- [x] Task 1: 后端配置 — application.yml 新增 DeepSeek 配置项
  - [x] 在 `application.yml` 中新增 `ai.providers` 配置结构，包含 kimi 和 deepseek 两个提供商
  - [x] 每个提供商包含 id、name、api-key、base-url、model 字段
  - [x] 新增 `ai.default-provider` 字段指定默认激活模型

- [x] Task 2: 后端配置类 — 新增 AIProviderConfig
  - [x] 创建 `AIProviderConfig.java`，使用 `@ConfigurationProperties` 绑定 `ai.providers` 和 `ai.default-provider`
  - [x] 提供 `getActiveProvider()` 方法返回当前激活的提供商配置
  - [x] 提供 `switchProvider(String id)` 方法切换激活提供商
  - [x] 提供 `getAllProviders()` 方法返回所有提供商列表（api-key 脱敏）

- [x] Task 3: 后端重构 AIService — 支持多模型
  - [x] 修改 `AIService.java`，注入 `AIProviderConfig`
  - [x] `chatCompletionStream` 方法增加可选 `modelId` 参数，根据 modelId 获取对应提供商配置
  - [x] 未指定 modelId 时使用当前激活提供商
  - [x] 日志中输出当前使用的模型信息

- [x] Task 4: 后端新增 API — 模型列表和切换
  - [x] `AIController.java` 新增 `GET /ai/models` 接口，返回可用模型列表
  - [x] `AIController.java` 新增 `POST /ai/switch-model` 接口，切换激活模型
  - [x] 修改 `POST /ai/generate-plan` 支持可选 modelId 参数

- [x] Task 5: 后端 WebSocket — 支持消息指定模型
  - [x] 修改 `AIWebSocketHandler.java`，解析消息中的 `modelId` 字段
  - [x] 将 modelId 传递给 `AIService.chatCompletionStream`

- [x] Task 6: 前端 — AiPlanDialog 模型切换 UI
  - [x] 在对话框顶部标题栏添加模型切换下拉选择器
  - [x] 页面加载时调用 `GET /ai/models` 获取可用模型列表
  - [x] 选择模型后持久化到 localStorage
  - [x] 发送消息时在 WebSocket 消息中携带 modelId

# Task Dependencies
- Task 2 depends on Task 1
- Task 3 depends on Task 2
- Task 4 depends on Task 2
- Task 5 depends on Task 3
- Task 6 depends on Task 4, Task 5
