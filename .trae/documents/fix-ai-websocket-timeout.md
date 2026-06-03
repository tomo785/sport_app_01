# 修复 AI WebSocket 按钮点击后 timeout 错误

## 问题摘要

微信小程序中点击首页「AI方案」浮动按钮时，出现 `Error: timeout at WAServiceMainContext.js` 错误，界面卡在"连接中..."状态。

## 诊断结论

### 根本原因

Spring Boot 的 `TokenHandshakeInterceptor` 校验 JWT Token 失败时返回 `false`，Spring 拒绝 WebSocket 握手（返回非 101 HTTP 状态码）。微信小程序的 `uni.connectSocket` API 无法正确处理这种被拒绝的握手响应，不会触发 `onError` 或 `onClose` 回调，而是持续等待直到超时，最终抛出通用的 `Error: timeout`。

**最常见触发场景**：JWT Token 已过期（默认 24h）、Token 无效、或用户重新登录后 Token 不匹配。

### 次级问题

1. `request.js` 中 `uni.request` 无 `timeout` 配置，默认 60s 超时
2. `AiPlanDialog.vue` 中 `uni.connectSocket` 无连接超时机制
3. 缺少前端 Token 预校验（可在连接前检查 Token 是否明显过期）
4. `.env` 中 `VITE_WS_URL` 仍为旧路径 `ws://localhost:8080/ws/ai`（缺少 `/api/v1`），AiPlanDialog 未使用 env 变量

---

## 变更计划

### 1. 后端：TokenHandshakeInterceptor — 返回明确错误状态

**文件**: `backend/src/main/java/com/sport/common/config/TokenHandshakeInterceptor.java`

**问题**: 当前 `beforeHandshake` 返回 `false` 时，Spring 不发送任何有意义的 HTTP 响应码，客户端只能等到超时。

**方案**: 在返回 `false` 前，通过 `ServerHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED)` 设置 401 状态码，让微信小程序能更快检测到连接失败。

```java
@Override
public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
        String token = servletRequest.getServletRequest().getParameter("token");
        if (token != null && !token.isBlank() && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            attributes.put("userId", userId);
            log.debug("WebSocket 握手成功: userId={}", userId);
            return true;
        }
    }
    log.warn("WebSocket 握手失败: Token 无效或缺失");
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return false;
}
```

### 2. 前端：request.js — 添加 timeout 配置

**文件**: `frontend/src/utils/request.js`

**问题**: `uni.request` 无 `timeout` 配置，默认 60s，超时时显示通用"网络连接失败"提示，不利于排查。

**方案**: 添加 `timeout: 15000`（15 秒），并区分 timeout 错误给出明确提示。

```javascript
uni.request({
  url: BASE_URL + options.url,
  method: options.method || 'GET',
  data: options.data || {},
  header: headers,
  timeout: 15000,  // 15秒超时
  // ... existing ...
  fail: (err) => {
    const msg = err.errMsg && err.errMsg.includes('timeout')
      ? '请求超时，请检查网络或后端服务'
      : '网络连接失败'
    uni.showToast({ title: msg, icon: 'none', duration: 2000 })
    reject(err)
  }
})
```

### 3. 前端：AiPlanDialog.vue — WebSocket 连接超时 + Token 预校验

**文件**: `frontend/src/components/AiPlanDialog.vue`

**问题汇总**:
- WebSocket 连接无超时机制，Token 过期时永远等不到连接成功
- 无 Token 有效性预检
- 硬编码 WS URL，未使用 env 变量
- 连接失败后状态栏永远显示"连接中..."

**方案**:

#### 3a. 添加 Token 预校验
在 `connectWebSocket()` 中，连接前先检查 Token 是否有效：

```javascript
const connectWebSocket = () => {
  if (wsTask.value) return

  const token = uni.getStorageSync('token')
  if (!token) {
    wsError.value = '请先登录后再使用 AI 功能'
    return
  }

  // 预校验 Token 是否明显过期
  if (isTokenExpired(token)) {
    wsError.value = '登录已过期，请重新登录'
    return
  }

  // ... 继续连接
}
```

#### 3b. 添加连接超时（10 秒）
```javascript
// 连接超时计时器
let connectTimer = null

const connectWebSocket = () => {
  // ...
  wsTask.value = uni.connectSocket({ url })

  // 10 秒连接超时
  connectTimer = setTimeout(() => {
    if (!wsConnected.value && wsTask.value) {
      wsTask.value.close()
      wsTask.value = null
      wsConnected.value = false
      wsError.value = 'AI 连接超时，请检查网络或尝试重新登录'
    }
  }, 10000)

  wsTask.value.onOpen(() => {
    clearTimeout(connectTimer)
    // ...
  })
}
```

#### 3c. 使用 env 变量替换硬编码 WS URL
```javascript
const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/api/v1/ws/ai'
```

#### 3d. 添加 Token 过期检查工具函数
```javascript
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000
    return Date.now() >= exp
  } catch {
    return false
  }
}
```

### 4. 前端：.env — 修正 WS URL

**文件**: `frontend/.env`

```diff
- VITE_WS_URL=ws://localhost:8080/ws/ai
+ VITE_WS_URL=ws://localhost:8080/api/v1/ws/ai
```

---

## 涉及的修改文件

| 文件 | 修改类型 | 说明 |
|------|---------|------|
| `backend/src/main/java/com/sport/common/config/TokenHandshakeInterceptor.java` | 修改 | 握手失败时设置 401 状态码 |
| `frontend/src/utils/request.js` | 修改 | 添加 timeout 配置和超时错误提示 |
| `frontend/src/components/AiPlanDialog.vue` | 修改 | Token 预校验 + 连接超时 + env 变量 |
| `frontend/.env` | 修改 | 修正 VITE_WS_URL |

---

## 验证步骤

1. **正常场景测试**：用有效 Token 登录后点击 AI 按钮，应正常连接并显示 Kimi 对话界面
2. **过期 Token 测试**：手动清除 Token 或使用过期 Token，点击 AI 按钮，应在 ~10 秒内收到明确错误提示（而非永久卡在"连接中..."），提示内容为"登录已过期，请重新登录"或"AI 连接超时"
3. **后端未启动测试**：停止后端服务后点击 AI 按钮，应在 15 秒内收到"请求超时，请检查网络或后端服务"提示
4. **验证超时后重试功能**：连接失败后应出现"点击重试"按钮，点击后应重新尝试连接

---

## 假设与约束

- 微信小程序开发者工具的 WebSocket 调试功能正常（非工具 bug）
- 后端 Spring Boot 服务正常运行在 `localhost:8080`
- 用户已通过密码登录获得有效 JWT Token
- JWT 过期时间配置为 24 小时（`jwt.expiration: 86400000`）
