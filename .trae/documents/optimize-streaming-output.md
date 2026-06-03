# 优化 AI 流式输出方案

## 当前问题

`delta` 阶段每次收到一个 token 片段，都对**整个累积内容**执行 `cleanAiText(stripPlanJson(msg.content))`：
- `stripPlanJson`：正则匹配 `<<<PLAN_JSON>>>...<<<PLAN_JSON_END>>>`，O(n) 扫描全文
- `cleanAiText`：8 个正则替换，每个 O(n) 扫描全文
- delta 每秒可能触发 10-30 次，内容越长越慢

**性能问题**：长回复（如 2000 字 + JSON）时，每次 delta 都对全文做 9 轮正则，可能造成卡顿。

## 优化方案：状态机 + 增量处理

核心思路：用状态机追踪当前是否在 JSON 区域内，只对新增的增量文本做处理。

### 状态定义

```javascript
let inJsonBlock = false    // 是否正在 JSON 区域内
let jsonBuffer = ''        // JSON 区域的累积内容
```

### delta 处理逻辑

```javascript
case 'delta':
  if (currentAssistantIndex.value >= 0) {
    const msg = messages.value[currentAssistantIndex.value]
    const chunk = data.content

    // 检测是否进入/离开 JSON 区域
    if (!inJsonBlock) {
      msg.content += chunk
      // 检查累积内容是否出现了 <<<PLAN_JSON>>>
      if (msg.content.includes('<<<PLAN_JSON>>>')) {
        inJsonBlock = true
        // 把 <<<PLAN_JSON>>> 之后的内容移入 jsonBuffer
        const idx = msg.content.indexOf('<<<PLAN_JSON>>>')
        jsonBuffer = msg.content.substring(idx)
        msg.content = msg.content.substring(0, idx)
        msg.displayContent = cleanAiText(msg.content)
      } else {
        // 正常文本区域：增量清洗
        msg.displayContent = cleanAiText(msg.content)
      }
    } else {
      // JSON 区域内：只累积到 jsonBuffer，不展示
      jsonBuffer += chunk
      // 检查是否出现了结束标记
      if (jsonBuffer.includes('<<<PLAN_JSON_END>>>')) {
        inJsonBlock = false
        jsonBuffer = ''  // 清空
      }
      // displayContent 保持不变（不更新）
    }
    throttledScroll()
  }
  break
```

### done 处理逻辑

```javascript
case 'done':
  loading.value = false
  if (currentAssistantIndex.value >= 0) {
    const msg = messages.value[currentAssistantIndex.value]
    // 重置状态
    inJsonBlock = false
    // 从 jsonBuffer + content 拼回完整原始内容用于解析
    const rawContent = msg.content + jsonBuffer
    jsonBuffer = ''
    // 提取 JSON
    const planData = parsePlanJson(rawContent)
    // 最终清洗展示内容
    msg.displayContent = cleanAiText(msg.content)
    msg.isPlan = !!planData || msg.displayContent.includes('计划') || msg.displayContent.includes('方案') || msg.displayContent.includes('训练')
    msg.planData = planData
  }
  currentAssistantIndex.value = -1
  scrollToBottom()
  break
```

### 优势对比

| | 当前方案 | 优化方案 |
|---|---|---|
| JSON 区域内 delta | 仍对全文做 9 轮正则 | 直接跳过，零开销 |
| 正常文本 delta | 对全文做 9 轮正则 | 对全文做 9 轮正则（相同） |
| 内存 | content 保存全文 | content 只保存可读部分，jsonBuffer 保存 JSON 部分 |
| 复杂度 | 简单 | 略增（状态机） |

### 进一步优化（可选）：增量 cleanAiText

对正常文本区域，也可以只对增量部分做清洗，而非全文：

```javascript
// 增量清洗：只处理新增的 chunk
const cleanedChunk = cleanChunk(chunk)  // 简化版清洗
msg.displayContent += cleanedChunk
```

但这需要处理跨 chunk 的 Markdown 标记（如 `**` 被拆成两个 chunk），复杂度较高，且当前文本区域通常不会太长，收益有限。**建议暂不实施增量清洗**。

## 修改文件

| 文件 | 修改 |
|------|------|
| `frontend/src/components/AiPlanDialog.vue` | 新增 `inJsonBlock`/`jsonBuffer` 状态变量，重写 `delta` 和 `done` 分支 |

## 验证

1. AI 输出纯文本（无 JSON）→ 流式显示正常
2. AI 输出文本 + JSON → JSON 区域不展示，文本正常流式显示
3. AI 输出完成后 → "使用此方案"按钮正常提取 JSON
4. 长回复（2000+ 字）→ 无卡顿
