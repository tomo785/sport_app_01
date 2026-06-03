# 修复 AiPlanDialog WebSocket `onOpen is not a function` 错误

## 问题摘要

微信小程序中点击「AI方案」按钮后报错：
```
TypeError: p.value.onOpen is not a function
    at k (AiPlanDialog.js? [sm]:1)
```
后续还触发了 `p.value.close is not a function` 以及 `Error: timeout`。

## 根因分析（Phase 1 探索结论）

### 根因 1：SocketTask API 在微信小程序中不可靠

`uni.connectSocket()` 在 H5/APP 端返回 `SocketTask` 对象，可调用 `.onOpen()`、`.onMessage()` 等方法。但在微信小程序环境中，经过 UniApp 编译后，这个 SocketTask 对象的方法可能不被正确映射，导致 `wsTask.value.onOpen is not a function`。

**微信小程序兼容的正确做法**是使用 UniApp 全局监听 API：
- `uni.onSocketOpen(callback)` 替代 `socketTask.onOpen(callback)`
- `uni.onSocketMessage(callback)` 替代 `socketTask.onMessage(callback)`  
- `uni.onSocketError(callback)` 替代 `socketTask.onError(callback)`
- `uni.onSocketClose(callback)` 替代 `socketTask.onClose(callback)`
- `uni.sendSocketMessage({data})` 替代 `socketTask.send({data})`
- `uni.closeSocket()` 替代 `socketTask.close()`

### 根因 2：`WS_BASE_URL` 导入无效

当前代码第 100 行：
```javascript
import { WS_BASE_URL } from '@/utils/request.js'
```
`request.js` 只导出 `get`、`post`、`put`、`del`、`default`，**没有导出 `WS_BASE_URL`**。这导致 `WS_BASE_URL` 为 `undefined`，WebSocket 实际连接 `undefined?token=xxx`，必然失败。

### 根因 3：全局 `uni.onSocket*` 回调泄漏

当前代码每次调用 `connectWebSocket()` 都会重新注册全局回调，但从不调用 `uni.offSocket*()` 清理。多次打开/关闭对话框会导致回调堆积。

---

## 变更计划

### 1. `request.js` — 导出 WS_BASE_URL

**文件**: `frontend/src/utils/request.js`

在文件末尾添加 WS_BASE_URL 导出：
```javascript
export const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/api/v1/ws/ai'
```

### 2. `AiPlanDialog.vue` — 完全切换到 `uni.onSocket*` 全局 API

**文件**: `frontend/src/components/AiPlanDialog.vue`

#### 2a. 移除 wsTask ref，改用 wsConnecting 布尔标志

```javascript
// 旧: const wsTask = ref(null)
// 新:
const wsConnecting = ref(false)
```

#### 2b. 重写 connectWebSocket — 全局 API + 清理旧监听器

```javascript
const connectWebSocket = () => {
  if (wsConnecting.value || wsConnected.value) return

  const token = uni.getStorageSync('token')
  if (!token) {
    wsError.value = '请先登录后再使用 AI 功能'
    return
  }

  if (isTokenExpired(token)) {
    wsError.value = '登录已过期，请重新登录'
    return
  }

  wsConnecting.value = true

  const url = `${WS_BASE_URL}?token=${token}`
  console.log('正在连接 WebSocket:', url)

  // 先清除旧的全局监听器，防止重复注册
  uni.offSocketOpen()
  uni.offSocketMessage()
  uni.offSocketError()
  uni.offSocketClose()

  // 注册新的全局监听器
  uni.onSocketOpen(() => {
    clearTimeout(connectTimer)
    connectTimer = null
    wsConnecting.value = false
    wsConnected.value = true
    wsError.value = ''
    console.log('WebSocket 连接成功')
  })

  uni.onSocketMessage((res) => {
    try {
      const data = JSON.parse(res.data)
      handleWsMessage(data)
    } catch (e) {
      console.error('WebSocket 消息解析失败:', res.data)
    }
  })

  uni.onSocketError((err) => {
    clearTimeout(connectTimer)
    connectTimer = null
    wsConnecting.value = false
    wsConnected.value = false
    wsError.value = 'AI 连接失败，请检查网络或后端服务是否启动'
    console.error('WebSocket 错误:', err)
  })

  uni.onSocketClose(() => {
    clearTimeout(connectTimer)
    connectTimer = null
    wsConnecting.value = false
    wsConnected.value = false
    console.log('WebSocket 关闭')
  })

  // 发起连接
  uni.connectSocket({ url })

  // 10 秒连接超时
  connectTimer = setTimeout(() => {
    if (!wsConnected.value) {
      uni.closeSocket()
      wsConnecting.value = false
      wsConnected.value = false
      wsError.value = 'AI 连接超时，请检查网络或尝试重新登录'
    }
  }, 10000)
}
```

**关键差异说明**：

| | 旧代码 | 新代码 |
|---|---|---|
| SocketTask 方法 | `wsTask.value.onOpen(fn)` ❌ 微信MP不支持 | `uni.onSocketOpen(fn)` ✅ |
| 发起连接 | `wsTask.value = uni.connectSocket()` 再设回调 | 先设回调，再调 `uni.connectSocket()` |
| 防止重连 | `if (wsTask.value) return` | `if (wsConnecting.value \|\| wsConnected.value) return` |
| 回调清理 | 无 | `uni.offSocketOpen/Message/Error/Close()` |
| 发送消息 | `uni.sendSocketMessage({ data: payload })` 保持不变 | 不变 |

#### 2c. 重写 disconnectWebSocket

```javascript
const disconnectWebSocket = () => {
  clearTimeout(connectTimer)
  connectTimer = null
  uni.closeSocket()
  uni.offSocketOpen()
  uni.offSocketMessage()
  uni.offSocketError()
  uni.offSocketClose()
  wsConnecting.value = false
  wsConnected.value = false
  wsError.value = ''
}
```

#### 2d. 保持 sendMessage 不变

`sendMessage` 中的 `uni.sendSocketMessage({ data: payload })` 在全局 API 模式下同样适用，无需修改。

---

## 涉及的修改文件

| 文件 | 修改类型 | 说明 |
|------|---------|------|
| `frontend/src/utils/request.js` | 修改 | 新增 `WS_BASE_URL` 导出 |
| `frontend/src/components/AiPlanDialog.vue` | 重写 | 完全切换到 `uni.onSocket*` 全局 API，移除 SocketTask 模式 |

---

## 验证步骤

1. **执行 clean rebuild**：删除 `frontend/dist` 目录，重新运行 `npm run dev` 编译微信小程序
2. **正常连接测试**：登录后点击 AI 按钮，应正常建立连接并显示 Kimi 对话界面
3. **Token 过期测试**：应直接提示"登录已过期，请重新登录"，不发起连接
4. **多次开关对话框测试**：打开→关闭→再打开 AI 对话框，不应出现回调重复注册错误
5. **后端未启动测试**：应在 10 秒内提示"AI 连接超时"

---

## 假设与约束

- 微信小程序 UniApp 版本为 `3.0.0-alpha-5000720260327001`
- 当前 `uni.onSocket*` / `uni.offSocket*` / `uni.connectSocket` / `uni.closeSocket` 在该版本微信小程序中可用
- 后端 `TokenHandshakeInterceptor` 的 401 状态码修改已生效
- `.env` 中 `VITE_WS_URL` 已修正为包含 `/api/v1` 的路径
- 用户将执行一次 clean rebuild 以确保没有旧编译缓存残留
