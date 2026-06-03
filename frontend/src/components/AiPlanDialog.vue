<template>
  <view>
    <!-- 触发按钮 -->
    <view class="ai-float-btn" @click="openDialog">
      <text class="ai-float-icon"></text>
      <text class="ai-float-text">AI方案</text>
    </view>

    <!-- 对话框遮罩 -->
    <view class="ai-mask" v-if="visible" @click="closeDialog"></view>

    <!-- 对话框主体 -->
    <view class="ai-dialog" :class="{ 'ai-dialog-show': visible }">
      <!-- 头部 -->
      <view class="ai-header">
        <view class="ai-header-left">
          <text class="ai-header-icon">🤖</text>
          <view class="ai-model-selector" @click="showModelPicker = !showModelPicker">
            <text class="ai-header-title">{{ currentModelName() }}</text>
            <text class="ai-model-arrow" :class="{ 'ai-model-arrow-up': showModelPicker }">▼</text>
          </view>
        </view>
        <view class="ai-header-close" @click="closeDialog">
          <text>✕</text>
        </view>
      </view>

      <!-- 模型选择下拉 -->
      <view class="ai-model-picker" v-if="showModelPicker">
        <view
          v-for="model in modelList"
          :key="model.id"
          class="ai-model-option"
          :class="{ 'ai-model-active': model.id === selectedModelId }"
          @click="selectModel(model)"
        >
          <text>{{ model.name }}</text>
          <text v-if="model.id === selectedModelId" class="ai-model-check">✓</text>
        </view>
      </view>

      <!-- 消息区域 -->
      <scroll-view class="ai-messages" scroll-y :scroll-top="scrollTop" :scroll-with-animation="true">
        <!-- 欢迎消息 -->
        <view class="ai-message ai-message-system" v-if="messages.length === 0">
          <view class="ai-avatar">🤖</view>
          <view class="ai-bubble">
            <text class="ai-text">你好！我是 AI 教练。我可以根据你的运动数据生成个性化的训练方案。请告诉我你的需求，比如：

• "生成下周训练计划"
• "帮我制定减脂方案"
• "根据数据给些建议"</text>
          </view>
        </view>

        <view v-for="msg in messages" :key="msg.id" :class="['ai-message', msg.role === 'user' ? 'ai-message-user' : 'ai-message-assistant']">
          <view class="ai-avatar">{{ msg.role === 'user' ? '😊' : '🤖' }}</view>
          <view class="ai-bubble">
            <text class="ai-text">{{ msg.displayContent || msg.content }}</text>
            <view v-if="msg.role === 'assistant' && msg.isPlan" class="ai-action-bar">
              <view class="ai-action-btn" @click="copyPlan(msg.displayContent || msg.content)">
                <text>📋 复制</text>
              </view>
              <view class="ai-action-btn ai-action-primary" @click="usePlan(msg.planData || msg.content)">
                <text>✓ 使用此方案</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载中 -->
        <view class="ai-message ai-message-assistant" v-if="loading">
          <view class="ai-avatar">🤖</view>
          <view class="ai-bubble ai-bubble-loading">
            <view class="ai-dots">
              <view class="ai-dot"></view>
              <view class="ai-dot"></view>
              <view class="ai-dot"></view>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 快捷标签 -->
      <view class="ai-quick-tags" v-if="messages.length === 0 && !loading">
        <text class="ai-tag" v-for="tag in quickTags" :key="tag" @click="sendQuick(tag)">{{ tag }}</text>
      </view>

      <!-- 输入区域 -->
      <view class="ai-input-area">
        <input
          class="ai-input"
          v-model="inputText"
          placeholder="输入你的需求..."
          confirm-type="send"
          @confirm="sendMessage"
          :disabled="loading"
        />
        <view class="ai-send-btn" :class="{ 'ai-send-disabled': !inputText.trim() || loading }" @click="sendMessage">
          <text>发送</text>
        </view>
      </view>

      <!-- WebSocket 状态提示 -->
      <view class="ai-ws-status" v-if="visible && !wsConnected && messages.length > 0">
        <text class="ai-ws-text" v-if="!wsError">连接中...</text>
        <text class="ai-ws-error" v-else>{{ wsError }}</text>
        <text class="ai-ws-retry" v-if="wsError" @click="retryConnect">点击重试</text>
      </view>

    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, onUnmounted, onMounted } from 'vue'
import { WS_BASE_URL } from '@/utils/request.js'
import { get, post } from '@/utils/request.js'

const props = defineProps({
  // 运动统计数据，作为上下文传给 AI
  statsData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['usePlan'])

// 模型切换
const modelList = ref([])
const selectedModelId = ref(uni.getStorageSync('ai_selected_model') || '')
const showModelPicker = ref(false)

const fetchModels = async () => {
  try {
    const res = await get('/ai/models')
    if (res && Array.isArray(res)) {
      modelList.value = res
      // 如果没有选过模型，默认选激活的
      if (!selectedModelId.value) {
        const active = res.find(m => m.active)
        if (active) selectedModelId.value = active.id
      }
    }
  } catch (e) {
    console.warn('获取模型列表失败:', e)
  }
}

const selectModel = async (model) => {
  selectedModelId.value = model.id
  uni.setStorageSync('ai_selected_model', model.id)
  showModelPicker.value = false

  try {
    await post('/ai/switch-model', { modelId: model.id })
    uni.showToast({ title: `已切换为 ${model.name}`, icon: 'none', duration: 1500 })
  } catch (e) {
    console.error('切换模型失败:', e)
    uni.showToast({ title: '切换失败', icon: 'none', duration: 1500 })
  }
}

const currentModelName = () => {
  const m = modelList.value.find(m => m.id === selectedModelId.value)
  return m ? m.name : 'AI 教练'
}

const visible = ref(false)
const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const scrollTop = ref(0)
const wsConnected = ref(false)
const wsError = ref('')
const wsConnecting = ref(false)
const currentAssistantIndex = ref(-1)
let connectTimer = null
let wsSessionId = 0
let inJsonBlock = false   // 是否正在 JSON 区域内
let jsonBuffer = ''       // JSON 区域的累积内容

const quickTags = [
  '生成下周训练计划',
  '制定减脂方案',
  '增肌训练建议',
  '分析运动数据'
]


// 打开对话框
const openDialog = () => {
  visible.value = true
  connectWebSocket()
}

// 关闭对话框
const closeDialog = () => {
  visible.value = false
  disconnectWebSocket()
}

function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000
    return Date.now() >= exp
  } catch {
    return false
  }
}

// 连接 WebSocket（全局 API 模式，兼容微信小程序）
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
  const sessionId = ++wsSessionId

  const url = `${WS_BASE_URL}?token=${token}`
  console.log('正在连接 WebSocket:', url)

  uni.onSocketOpen(() => {
    if (sessionId !== wsSessionId) return
    clearTimeout(connectTimer)
    connectTimer = null
    wsConnecting.value = false
    wsConnected.value = true
    wsError.value = ''
    console.log('WebSocket 连接成功')
  })

  uni.onSocketMessage((res) => {
    if (sessionId !== wsSessionId) return
    try {
      const data = JSON.parse(res.data)
      handleWsMessage(data)
    } catch (e) {
      console.error('WebSocket 消息解析失败:', res.data)
    }
  })

  uni.onSocketError((err) => {
    if (sessionId !== wsSessionId) return
    clearTimeout(connectTimer)
    connectTimer = null
    wsConnecting.value = false
    wsConnected.value = false
    wsError.value = 'AI 连接失败，请检查网络或后端服务是否启动'
    console.error('WebSocket 错误:', err)
  })

  uni.onSocketClose(() => {
    if (sessionId !== wsSessionId) return
    clearTimeout(connectTimer)
    connectTimer = null
    wsConnecting.value = false
    wsConnected.value = false
    console.log('WebSocket 关闭')
  })

  uni.connectSocket({ url })

  connectTimer = setTimeout(() => {
    if (sessionId !== wsSessionId) return
    if (!wsConnected.value) {
      wsSessionId++
      uni.closeSocket()
      wsConnecting.value = false
      wsConnected.value = false
      wsError.value = 'AI 连接超时，请检查网络或尝试重新登录'
    }
  }, 10000)
}

// 断开 WebSocket
const disconnectWebSocket = () => {
  clearTimeout(connectTimer)
  connectTimer = null
  wsSessionId++
  inJsonBlock = false
  jsonBuffer = ''
  uni.closeSocket()
  wsConnecting.value = false
  wsConnected.value = false
  wsError.value = ''
}

// 重试连接
const retryConnect = () => {
  disconnectWebSocket()
  connectWebSocket()
}

// 清洗 AI 输出的 Markdown 标记（仅处理可读文本部分）
const cleanAiText = (text) => {
  if (!text) return ''
  return text
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/__(.+?)__/g, '$1')
    .replace(/`{3}[\s\S]*?`{3}/g, '')
    .replace(/`(.+?)`/g, '$1')
    .replace(/^###\s*/gm, '▶ ')
    .replace(/^##\s*/gm, '▶▶ ')
    .replace(/^#\s*/gm, '▶▶▶ ')
    .replace(/^[-*]\s/gm, '• ')
}

// 从 AI 回复中提取结构化计划 JSON
const parsePlanJson = (text) => {
  if (!text) return null
  const startMarker = '<<<PLAN_JSON>>>'
  const endMarker = '<<<PLAN_JSON_END>>>'
  const startIdx = text.indexOf(startMarker)
  const endIdx = text.indexOf(endMarker)
  if (startIdx === -1 || endIdx === -1 || endIdx <= startIdx) return null

  const jsonStr = text.substring(startIdx + startMarker.length, endIdx).trim()
  try {
    const plan = JSON.parse(jsonStr)
    if (plan.name && plan.courses && Array.isArray(plan.courses)) {
      return plan
    }
  } catch (e) {
    console.error('[AI] JSON 解析失败:', e)
  }
  return null
}

// 从可读文本中移除 JSON 块（仅展示纯文本部分）
const stripPlanJson = (text) => {
  if (!text) return ''
  return text.replace(/<<<PLAN_JSON>>>[\s\S]*?<<<PLAN_JSON_END>>>/g, '').trim()
}

// 滚动节流
let scrollTimer = null
const throttledScroll = () => {
  if (scrollTimer) return
  scrollTimer = setTimeout(() => {
    scrollToBottom()
    scrollTimer = null
  }, 300)
}

// 处理 WebSocket 消息
const handleWsMessage = (data) => {
  switch (data.type) {
    case 'start':
      loading.value = true
      currentAssistantIndex.value = messages.value.length
      messages.value.push({ id: Date.now() + '_' + Math.random().toString(36).slice(2, 7), role: 'assistant', content: '', displayContent: '', isPlan: false })
      scrollToBottom()
      break

    case 'delta':
      if (currentAssistantIndex.value >= 0) {
        const msg = messages.value[currentAssistantIndex.value]
        const chunk = data.content

        if (!inJsonBlock) {
          msg.content += chunk
          // 检查是否出现 JSON 开始标记
          if (msg.content.includes('<<<PLAN_JSON>>>')) {
            inJsonBlock = true
            const idx = msg.content.indexOf('<<<PLAN_JSON>>>')
            jsonBuffer = msg.content.substring(idx)
            msg.content = msg.content.substring(0, idx)
            msg.displayContent = cleanAiText(msg.content)
          } else {
            msg.displayContent = cleanAiText(msg.content)
          }
        } else {
          // JSON 区域内：只累积，不展示
          jsonBuffer += chunk
          if (jsonBuffer.includes('<<<PLAN_JSON_END>>>')) {
            inJsonBlock = false
            jsonBuffer = ''
          }
        }
        throttledScroll()
      }
      break

    case 'done':
      loading.value = false
      if (currentAssistantIndex.value >= 0) {
        const msg = messages.value[currentAssistantIndex.value]
        // 重置状态
        inJsonBlock = false
        // 拼回完整原始内容用于解析 JSON
        const rawContent = msg.content + jsonBuffer
        jsonBuffer = ''
        // 提取结构化 JSON
        const planData = parsePlanJson(rawContent)
        // 最终清洗展示内容
        msg.displayContent = cleanAiText(msg.content)
        msg.isPlan = !!planData || msg.displayContent.includes('计划') || msg.displayContent.includes('方案') || msg.displayContent.includes('训练')
        msg.planData = planData
      }
      currentAssistantIndex.value = -1
      scrollToBottom()
      break

    case 'error':
      loading.value = false
      currentAssistantIndex.value = -1
      let errMsg = data.message || '请求失败'
      if (errMsg.includes('401') || errMsg.includes('Unauthorized')) {
        errMsg = 'AI 认证失败，请检查后端配置的 API Key 是否有效或已过期。'
      }
      messages.value.push({ id: Date.now() + '_' + Math.random().toString(36).slice(2, 7), role: 'assistant', content: `错误: ${errMsg}` })
      scrollToBottom()
      break
  }
}

// 快捷发送
const sendQuick = (text) => {
  inputText.value = text
  sendMessage()
}

// 构建系统提示词
const buildSystemPrompt = () => {
  const s = props.statsData || {}
  const summary = s.summary || {}
  const today = s.todayStats || {}
  const trend = s.trendData || {}
  const recordSummary = s.recordSummary || null

  const totalDist = trend.dates?.length
    ? (trend.distances || []).reduce((a, b) => a + b, 0)
    : 0
  const totalDur = trend.dates?.length
    ? (trend.durations || []).reduce((a, b) => a + b, 0)
    : 0
  const totalCal = trend.dates?.length
    ? (trend.calories || []).reduce((a, b) => a + b, 0)
    : 0

  const typeMap = { 1: '跑步', 2: '健走', 3: '骑行' }
  const typeLines = recordSummary
    ? Object.entries(recordSummary.typeCount || {})
        .map(([type, count]) => `${typeMap[type] || '运动'}：${count} 次`)
        .join('\n')
    : ''

  return `你是一位专业的运动健身 AI 教练，擅长根据用户的运动数据制定个性化的训练方案。请用中文回复，语气亲切专业。

【用户运动数据】
总运动次数：${summary.totalWorkouts || 0} 次
总距离：${summary.totalDistanceStr || '0km'}
总消耗：${summary.totalCaloriesStr || '0千卡'}
连续运动天数：${summary.streakDays || 0} 天
本周运动：${summary.weeklyWorkouts || 0} 次
本月运动：${summary.monthlyWorkouts || 0} 次

${today.statDate ? `【今日运动】
距离：${today.distanceStr || '0m'}
时长：${today.durationStr || '0分'}
消耗：${today.caloriesStr || '0千卡'}
次数：${today.recordCount || 0} 次

` : ''}${trend.dates?.length ? `【近期趋势】
统计周期：${trend.dates.length} 个数据点
周期总距离：${totalDist} 米
周期总时长：${totalDur} 秒
周期总消耗：${totalCal} 千卡

` : ''}${recordSummary ? `【记录详情】
总距离：${(recordSummary.totalDistance / 1000).toFixed(2)} km
总时长：${Math.floor(recordSummary.totalDuration / 60)} 分钟
总消耗：${recordSummary.totalCalories} 千卡
${typeLines}

` : ''}请根据以上数据，为用户提供专业、可行的运动方案或建议。

【输出格式要求 — 双模式】
你的回复必须包含两部分：

第一部分：可读文本
- 用中文序号（一、二、三 或 1. 2. 3.）做层级分隔
- 用空行分隔不同段落，保持排版整洁
- 禁止使用 Markdown 语法（如 **加粗**、- 列表、# 标题）
- 禁止使用 emoji
- 禁止使用代码块（除了下面的 JSON 块）

第二部分：结构化数据（仅当用户要求生成训练计划时输出）
- 在文本末尾另起一行，输出 <<<PLAN_JSON>>> 标记
- 然后输出一个 JSON 对象，严格遵循下面的格式
- JSON 结束后另起一行输出 <<<PLAN_JSON_END>>> 标记
- 如果用户只是咨询建议而非生成计划，则不需要输出 JSON 部分

JSON 格式如下：
{
  "name": "计划名称",
  "description": "计划简介",
  "totalWeeks": 4,
  "level": 2,
  "courses": [
    {
      "week": 1,
      "day": 1,
      "name": "课程名称",
      "type": 1,
      "duration": 45,
      "description": "课程描述",
      "exercises": [
        {
          "name": "动作名称",
          "type": 1,
          "sets": 3,
          "reps": 12,
          "duration": 0,
          "distance": 0,
          "restTime": 60,
          "description": "动作说明"
        }
      ]
    }
  ]
}

字段说明：
- type: 1=有氧(跑步), 2=力量, 3=拉伸, 4=HIIT, 5=综合, 6=休息
- level: 1=入门, 2=初级, 3=中级, 4=高级, 5=精英
- duration: 分钟
- distance: 米
- restTime: 秒
- day: 1=周一, 2=周二, ..., 7=周日
- 休息日的 exercises 为空数组，type 为 6`
}

// 发送消息
const sendMessage = () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  if (!wsConnected.value) {
    uni.showToast({ title: 'AI 连接中，请稍候', icon: 'none' })
    return
  }

  // 添加用户消息
  messages.value.push({ id: Date.now() + '_' + Math.random().toString(36).slice(2, 7), role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  // 构建系统提示词
  const systemPrompt = buildSystemPrompt()
  const chatMessages = [
    { role: 'system', content: systemPrompt },
    ...messages.value.map(m => ({ role: m.role, content: m.content }))
  ]

  // 通过 WebSocket 发送
  const payload = JSON.stringify({ type: 'chat', messages: chatMessages, modelId: selectedModelId.value || undefined })
  uni.sendSocketMessage({ data: payload })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = messages.value.length * 9999
  })
}

// 复制方案
const copyPlan = (content) => {
  uni.setClipboardData({
    data: content,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

// 使用方案
const usePlan = (planData) => {
  emit('usePlan', planData)
  uni.showToast({ title: '已应用方案', icon: 'success' })
  closeDialog()
}

// 组件挂载时获取模型列表
onMounted(() => {
  fetchModels()
})

// 组件卸载时断开连接
onUnmounted(() => {
  disconnectWebSocket()
})

// 暴露方法
defineExpose({ openDialog })
</script>

<style lang="scss" scoped>
// 浮动按钮
.ai-float-btn {
  position: fixed;
  right: 30rpx;
  bottom: 220rpx;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  z-index: 99;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.92);
  }
}

.ai-float-icon {
  font-size: 44rpx;
  margin-bottom: 2rpx;
}

.ai-float-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 500;
}

// 遮罩
.ai-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
}

// 对话框
.ai-dialog {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 0;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  z-index: 101;
  display: flex;
  flex-direction: column;
  transition: height 0.3s ease;
  overflow: hidden;

  &.ai-dialog-show {
    height: 80vh;
  }
}

// 头部
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.ai-header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-header-icon {
  font-size: 40rpx;
}

.ai-header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.ai-header-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;

  &:active {
    background: #e0e0e0;
  }
}

// 模型选择器
.ai-model-selector {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: #f0f0f7;
  cursor: pointer;

  &:active {
    background: #e0e0f0;
  }
}

.ai-model-arrow {
  font-size: 18rpx;
  color: #999;
  transition: transform 0.2s;
}

.ai-model-arrow-up {
  transform: rotate(180deg);
}

// 模型选择下拉
.ai-model-picker {
  position: absolute;
  top: 100rpx;
  left: 30rpx;
  right: 30rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  z-index: 10;
  overflow: hidden;
}

.ai-model-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  font-size: 28rpx;
  color: #333;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f8f8fc;
  }
}

.ai-model-active {
  color: #667eea;
  font-weight: 500;
}

.ai-model-check {
  color: #667eea;
  font-size: 28rpx;
}

// 消息区域
.ai-messages {
  flex: 1;
  padding: 20rpx 24rpx;
  overflow-y: auto;
}

.ai-message {
  display: flex;
  margin-bottom: 24rpx;
  align-items: flex-start;
}

.ai-message-user {
  flex-direction: row-reverse;
}

.ai-message-system {
  .ai-bubble {
    background: #f0f7ff;
    border: 1rpx solid #d0e3ff;
  }
}

.ai-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}

.ai-bubble {
  max-width: 70%;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  margin: 0 16rpx;
  background: #f5f5f5;
}

.ai-message-user .ai-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-message-user .ai-text {
  color: #fff;
}

// 加载动画
.ai-bubble-loading {
  padding: 24rpx 32rpx;
}

.ai-dots {
  display: flex;
  gap: 12rpx;
}

.ai-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #999;
  animation: ai-dot-bounce 1.4s infinite ease-in-out both;

  &:nth-child(1) { animation-delay: -0.32s; }
  &:nth-child(2) { animation-delay: -0.16s; }
}

@keyframes ai-dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

// 操作栏
.ai-action-bar {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
}

.ai-action-btn {
  padding: 10rpx 20rpx;
  border-radius: 28rpx;
  background: #fff;
  border: 1rpx solid #ddd;
  font-size: 24rpx;
  color: #666;

  &:active {
    background: #f0f0f0;
  }
}

.ai-action-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;

  &:active {
    opacity: 0.85;
  }
}

// 快捷标签
.ai-quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 0 24rpx 16rpx;
  flex-shrink: 0;
}

.ai-tag {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: #f0f0f7;
  font-size: 26rpx;
  color: #667eea;
  border: 1rpx solid #e0e0f0;

  &:active {
    background: #e0e0f0;
  }
}

// 输入区域
.ai-input-area {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid #f0f0f0;
  background: #fafafa;
  flex-shrink: 0;
}

.ai-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  background: #fff;
  border-radius: 36rpx;
  font-size: 28rpx;
  border: 1rpx solid #e0e0e0;
}

.ai-send-btn {
  padding: 16rpx 32rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 28rpx;
  color: #fff;
  font-weight: 500;

  &:active {
    opacity: 0.85;
  }
}

.ai-send-disabled {
  opacity: 0.5;
  pointer-events: none;
}

// WebSocket 状态
.ai-ws-status {
  text-align: center;
  padding: 12rpx 0;
  font-size: 22rpx;
  color: #999;
  background: #fafafa;
  border-top: 1rpx solid #f0f0f0;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.ai-ws-text {
  color: #999;
}

.ai-ws-error {
  color: #e74c3c;
  font-size: 22rpx;
}

.ai-ws-retry {
  color: #667eea;
  font-size: 22rpx;
  text-decoration: underline;
  padding: 4rpx 8rpx;

  &:active {
    opacity: 0.7;
  }
}

</style>
