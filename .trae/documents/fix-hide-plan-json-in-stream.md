# 修复 AI 对话中 JSON 块在流式输出时可见的问题

## 问题

AI 双模式输出时，`<<<PLAN_JSON>>>...<<<PLAN_JSON_END>>>` 之间的 JSON 内容在流式输出（delta）阶段会逐字显示给用户，虽然 `done` 阶段会用 `stripPlanJson()` 移除，但用户在等待回复完成的过程中会看到 JSON 原文闪过。

## 修改方案

**文件**: `frontend/src/components/AiPlanDialog.vue`

### 修改 `delta` 分支 — 流式输出时隐藏 JSON 块

在 `delta` case 中，追加内容后实时检测是否进入 JSON 区域，如果是则不展示：

```javascript
case 'delta':
  if (currentAssistantIndex.value >= 0) {
    const msg = messages.value[currentAssistantIndex.value]
    msg.content += data.content
    // 流式输出时实时移除 JSON 块，避免用户看到
    msg.displayContent = cleanAiText(stripPlanJson(msg.content))
    throttledScroll()
  }
  break
```

### 修改消息初始化 — 新增 displayContent 字段

```javascript
case 'start':
  loading.value = true
  currentAssistantIndex.value = messages.value.length
  messages.value.push({ 
    id: ..., 
    role: 'assistant', 
    content: '', 
    displayContent: '',  // 新增：用于展示的内容（不含 JSON）
    isPlan: false 
  })
  scrollToBottom()
  break
```

### 修改 `done` 分支

```javascript
case 'done':
  loading.value = false
  if (currentAssistantIndex.value >= 0) {
    const msg = messages.value[currentAssistantIndex.value]
    const rawContent = msg.content
    const planData = parsePlanJson(rawContent)
    msg.displayContent = cleanAiText(stripPlanJson(rawContent))
    msg.isPlan = !!planData || msg.displayContent.includes('计划') || ...
    msg.planData = planData
  }
  currentAssistantIndex.value = -1
  scrollToBottom()
  break
```

### 修改模板 — 使用 displayContent 展示

将消息气泡中的 `msg.content` 改为 `msg.displayContent || msg.content`：

```html
<text class="ai-msg-text">{{ msg.displayContent || msg.content }}</text>
```

这样：
- 流式输出时：`displayContent` 实时过滤 JSON，用户只看到可读文本
- 完成时：`displayContent` 是最终清洗后的文本
- `msg.content` 保留原始内容，供 `parsePlanJson` 提取结构化数据

## 验证

1. 让 AI 生成训练计划，观察流式输出过程中不应看到 `<<<PLAN_JSON>>>` 或 JSON 内容
2. 完成后展示的文本应为纯可读内容
3. 点击"使用此方案"应能正确提取 JSON 数据
