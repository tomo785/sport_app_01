# 支持切换 DeepSeek 模型 Spec

## Why
当前 AI 服务硬编码使用 Kimi（Moonshot）模型，无法切换到其他大模型。用户希望支持 DeepSeek 模型，并能在前端切换模型，以获得不同的 AI 体验和更灵活的配置。

## What Changes
- 后端 `application.yml` 新增 DeepSeek 配置项（api-key、base-url、model）
- 后端新增 `AIProvider` 配置类，统一管理多模型配置和当前激活模型
- 后端 `AIService` 重构为支持多模型提供商，根据当前激活模型动态选择配置
- 后端新增 `/ai/models` 接口，返回可用模型列表
- 后端新增 `/ai/switch-model` 接口，切换当前激活模型
- 后端 WebSocket 消息支持携带 `model` 字段指定模型
- 前端 `AiPlanDialog.vue` 新增模型切换下拉选择器
- 前端发送消息时携带选中的模型标识

## Impact
- Affected code: `AIService.java`, `AIController.java`, `AIWebSocketHandler.java`, `application.yml`, `AiPlanDialog.vue`
- 向后兼容：默认仍使用 Kimi 模型，不影响现有功能

## ADDED Requirements

### Requirement: 多模型配置管理
系统 SHALL 在 `application.yml` 中支持配置多个 AI 模型提供商，每个提供商包含 api-key、base-url、model 名称。

#### Scenario: 配置多个模型
- **GIVEN** application.yml 中配置了 kimi 和 deepseek 两个提供商
- **WHEN** 应用启动
- **THEN** 系统加载所有提供商配置，默认激活第一个配置的模型

### Requirement: 模型切换 API
系统 SHALL 提供 REST API 查询可用模型列表和切换当前激活模型。

#### Scenario: 查询可用模型
- **WHEN** 前端调用 `GET /ai/models`
- **THEN** 返回所有已配置的模型列表，包含 id、name、active 字段

#### Scenario: 切换模型
- **WHEN** 前端调用 `POST /ai/switch-model`，body 为 `{ "modelId": "deepseek" }`
- **THEN** 系统将激活模型切换为 deepseek，后续 AI 调用使用 deepseek 配置

### Requirement: WebSocket 消息指定模型
系统 SHALL 支持在 WebSocket 聊天消息中指定使用的模型。

#### Scenario: 消息携带模型
- **WHEN** 前端发送 `{ "type": "chat", "messages": [...], "modelId": "deepseek" }`
- **THEN** 后端使用 deepseek 配置调用 AI API
- **WHEN** 前端未指定 modelId
- **THEN** 后端使用当前默认激活模型

### Requirement: 前端模型切换 UI
系统 SHALL 在 AI 对话框顶部提供模型切换选择器。

#### Scenario: 用户切换模型
- **WHEN** 用户在 AI 对话框顶部点击模型选择器
- **THEN** 显示可用模型列表（如 "Kimi K2.6"、"DeepSeek V3"）
- **WHEN** 用户选择某个模型
- **THEN** 后续对话使用该模型，选择状态持久化到 localStorage
