<template>
  <view class="home">
    <!-- ===== 状态栏占位 ===== -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- ===== 问候语 ===== -->
    <view class="greeting-row">
      <view class="greeting-left">
        <text class="greeting-hi">Hi, {{ greetingText }}</text>
        <text class="greeting-sub">今天也要加油运动哦</text>
      </view>
      <view class="greeting-right">
        <text class="greeting-emoji">{{ greetingEmoji }}</text>
      </view>
    </view>

    <!-- ===== 天气卡片 ===== -->
    <view class="weather-card">
      <view class="weather-left">
        <text class="weather-temp">{{ weather.temp }}°</text>
        <text class="weather-desc">{{ weather.text }} · 体感 {{ weather.feelsLike }}°</text>
      </view>
      <view class="weather-right">
        <view class="weather-loc-aqi-row">
          <text class="weather-loc">📍 {{ weather.city || '获取中' }}</text>
          <view class="weather-aqi" v-if="weather.aqi">
            <view class="aqi-dot" :style="{ background: weather.aqiColor || '#22c55e' }"></view>
            <text class="aqi-text">空气{{ weather.aqiLevel }}</text>
            <text class="aqi-num">{{ weather.aqi }}</text>
          </view>
        </view>
        <view class="weather-sport" v-if="weather.sportCategory">
          <text class="sport-index-text">{{ weather.sportCategory }} · {{ weather.sportText }}</text>
        </view>
      </view>
    </view>

    <!-- ===== 今日活动卡片 ===== -->
    <view class="activity-card">
      <view class="ac-top">
        <text class="ac-title">今日步数</text>
        <text class="ac-date">{{ todayDate }}</text>
      </view>
      <view class="ac-main">
        <!-- 圆形进度环 + 鞋子图标 -->
        <view class="steps-ring-wrap">
          <svg class="steps-svg" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="52" fill="none" stroke="#f1f5f9" stroke-width="8" />
            <circle cx="60" cy="60" r="52" fill="none" stroke="#22c55e" stroke-width="8"
              stroke-linecap="round"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="circumference - (stepsProgress / 100) * circumference"
              transform="rotate(-90 60 60)" />
          </svg>
          <view class="steps-icon-box">
            <text class="steps-icon">👟</text>
          </view>
        </view>
        <view class="steps-info">
          <text class="steps-num">{{ stepsCount }}</text>
          <text class="steps-label">步</text>
          <text class="steps-pct">{{ stepsProgress }}%</text>
          <text class="steps-yesterday">昨日 {{ yesterdaySteps }} 步</text>
          <text class="steps-checkin">已打卡 {{ checkinDays }} 天</text>
        </view>
        <view class="checkin-btn" :class="{ 'checkin-done': checkedIn }" @click="handleCheckin">
          <text class="checkin-icon">{{ checkedIn ? '✓' : '✓' }}</text>
          <text class="checkin-text">{{ checkedIn ? '已打卡' : '打卡' }}</text>
        </view>
      </view>
      <view class="ac-stats">
        <view class="ac-stat">
          <text class="acs-val">{{ calories }}<text class="acs-unit">千卡</text></text>
        </view>
        <view class="ac-stat">
          <text class="acs-val">{{ distance }}<text class="acs-unit">公里</text></text>
        </view>
        <view class="ac-stat">
          <text class="acs-val">{{ duration }}<text class="acs-unit">分钟</text></text>
        </view>
      </view>
    </view>

    <!-- ===== 快捷操作栏 ===== -->
    <view class="action-bar">
      <view class="action-btn action-primary" @click="goRunning">
        <text class="action-icon">▶</text>
        <text class="action-text">开始运动</text>
      </view>
      <view class="action-btn action-secondary" @click="goPlan">
        <text class="action-text">修改计划</text>
      </view>
    </view>

    <!-- ===== 今日目标卡片 ===== -->
    <view class="plan-card" @click="goToPlanDetail">
      <!-- 有今日目标 -->
      <template v-if="todayPlan && todayPlan.hasPlan">
        <view class="plan-head">
          <text class="plan-title">今日目标</text>
          <text class="plan-action" @click.stop="goToPlanDetail">去执行 ›</text>
        </view>
        <!-- 标准化活动卡片列表 -->
        <view class="act-card-list" v-if="todayPlan.activities && todayPlan.activities.length > 0">
          <view
            class="act-card"
            v-for="(act, idx) in todayPlan.activities"
            :key="idx"
            :class="{ 'act-card-rest': act.type === 'rest' }"
          >
            <view class="act-card-row1">
              <text class="act-icon">{{ act.icon }}</text>
              <text class="act-name">{{ act.name }}</text>
              <text class="act-duration" v-if="act.duration > 0">{{ act.duration }}分钟</text>
            </view>
            <view class="act-card-row2" v-if="act.summary">
              <text class="act-summary">{{ act.summary }}</text>
            </view>
            <view class="act-card-row3" v-if="act.tags && act.tags.length > 0">
              <text class="act-tag" v-for="(tag, tIdx) in act.tags" :key="tIdx">{{ tag }}</text>
            </view>
          </view>
        </view>
        <!-- 训练描述（当没有活动列表时） -->
        <view class="plan-description" v-if="todayPlan.description && !todayPlan.activities?.length">
          <text class="plan-desc-text">{{ todayPlan.description }}</text>
        </view>
      </template>
      <!-- 无今日目标 -->
      <template v-else>
        <view class="plan-head">
          <text class="plan-title">今日目标</text>
        </view>
        <view class="plan-empty">
          <text class="plan-empty-text">暂无今日目标</text>
          <text class="plan-empty-hint">去「定制」页面创建训练计划吧</text>
          <view class="plan-empty-btn" @click.stop="goToCreatePlan">去定制</view>
        </view>
      </template>
    </view>

    <!-- ===== 今日训练卡片 ===== -->
    <view class="session-card">
      <view class="sc-head">
        <view class="sc-title-row">
          <view class="sc-dot"></view>
          <text class="sc-title">今日训练</text>
        </view>
        <text class="sc-type" @click="goToTodayTask">{{ todayStats.recordCount || 0 }} 次运动 ›</text>
      </view>

      <!-- 有今日运动记录：卡片列表 -->
      <view class="workout-cards" v-if="todayWorkouts.length > 0">
        <view
          class="workout-card"
          v-for="(item, idx) in todayWorkouts"
          :key="idx"
          @click="goToWorkoutDetail(item.id)"
        >
          <view class="wc-main">
            <view class="wc-icon" :style="{ background: getWorkoutTypeInfo(item.type).bg }">
              <view class="wc-dot" :style="{ background: getWorkoutTypeInfo(item.type).color }"></view>
            </view>
            <view class="wc-info">
              <text class="wc-name">{{ item.typeName || getWorkoutTypeInfo(item.type).name }}</text>
              <text class="wc-time">{{ formatWorkoutTime(item.startTime) }}</text>
            </view>
          </view>
          <view class="wc-tags">
            <view
              class="wc-tag"
              v-for="(tag, tIdx) in buildWorkoutTags(item)"
              :key="tIdx"
              :style="{ background: tag.bg, color: tag.color }"
            >
              {{ tag.text }}
            </view>
          </view>
        </view>
      </view>

      <!-- 无记录：统计概览 -->
      <view class="sc-body" v-else>
        <view class="sc-stats">
          <view class="sc-stat">
            <text class="scs-val">{{ todayDurationStr }}</text>
            <text class="scs-label">时长</text>
          </view>
          <view class="sc-stat">
            <text class="scs-val">{{ todayPaceStr }}</text>
            <text class="scs-label">配速 /km</text>
          </view>
          <view class="sc-stat">
            <text class="scs-val">{{ todayDistanceStr }}</text>
            <text class="scs-label">公里</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ===== AI 教练卡片 ===== -->
    <view class="ai-coach-card">
      <view class="ai-coach-header">
        <view class="ai-coach-title-row">
          <text class="ai-coach-icon">🤖</text>
          <text class="ai-coach-title">AI 教练</text>
        </view>
        <view class="ai-model-selector" @click="toggleModelPicker">
          <text class="ai-model-name">{{ currentModelName }}</text>
          <text class="ai-model-arrow" :class="{ 'ai-model-arrow-up': showModelPicker }">▼</text>
        </view>
      </view>

      <!-- 模型设置面板 -->
      <view class="ai-settings-panel" v-if="showModelPicker">
        <view class="ai-settings-section">
          <text class="ai-settings-label">选择模型</text>
          <view class="ai-provider-list">
            <view
              v-for="model in modelList"
              :key="model.id"
              class="ai-provider-card"
              :class="{ 'ai-provider-active': model.id === selectedModelId }"
              @click="selectModel(model)"
            >
              <view class="ai-provider-info">
                <text class="ai-provider-name">{{ model.name }}</text>
                <text class="ai-provider-id">{{ model.model }}</text>
              </view>
              <text v-if="model.id === selectedModelId" class="ai-provider-check">✓</text>
            </view>
          </view>
        </view>

        <view class="ai-settings-section">
          <text class="ai-settings-label">API Key</text>
          <input
            class="ai-settings-input"
            :value="customApiKey"
            @input="updateApiKey($event.detail.value)"
            placeholder="输入自定义 API Key（留空使用后端配置）"
            password
          />
        </view>

        <view class="ai-settings-section">
          <text class="ai-settings-label">连接测试</text>
          <view class="ai-test-row">
            <view
              class="ai-test-btn"
              :class="{ 'ai-test-running': testingConnection }"
              @click="testConnection"
            >
              <text>{{ testingConnection ? '测试中...' : '测试连接' }}</text>
            </view>
            <view class="ai-test-result" v-if="testResult">
              <text :class="testResult.ok ? 'ai-test-success' : 'ai-test-fail'">
                {{ testResult.message }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 消息区域 -->
      <scroll-view class="ai-messages" scroll-y :scroll-top="aiScrollTop" :scroll-with-animation="true">
        <view class="ai-message ai-message-system" v-if="aiMessages.length === 0">
          <view class="ai-avatar">🤖</view>
          <view class="ai-bubble">
            <text class="ai-text">你好！我是 AI 教练。我可以根据你的运动数据生成个性化的训练方案。请告诉我你的需求，比如：

• "生成下周训练计划"
• "帮我制定减脂方案"
• "根据数据给些建议"</text>
          </view>
        </view>

        <view v-for="msg in aiMessages" :key="msg.id" :class="['ai-message', msg.role === 'user' ? 'ai-message-user' : 'ai-message-assistant']">
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
      </scroll-view>

      <!-- AI 思考中状态条 -->
      <view class="ai-thinking-bar" v-if="aiLoading">
        <view class="ai-thinking-dots">
          <view class="ai-dot"></view>
          <view class="ai-dot"></view>
          <view class="ai-dot"></view>
        </view>
        <text class="ai-thinking-text">AI 思考中...</text>
      </view>

      <!-- 快捷标签 -->
      <view class="ai-quick-tags" v-if="aiMessages.length === 0">
        <text class="ai-tag" v-for="tag in aiQuickTags" :key="tag" @click="sendQuick(tag)">{{ tag }}</text>
      </view>

      <!-- 输入区域 -->
      <view class="ai-input-area">
        <input
          class="ai-input"
          v-model="aiInputText"
          placeholder="输入你的需求..."
          confirm-type="send"
          @confirm="sendAiMessage"
          :disabled="aiLoading"
        />
        <view class="ai-send-btn" :class="{ 'ai-send-disabled': !aiInputText.trim() || aiLoading }" @click="sendAiMessage">
          <text>发送</text>
        </view>
      </view>

      <!-- WebSocket 状态提示 -->
      <view class="ai-ws-status" v-if="!aiWsConnected && aiMessages.length > 0">
        <text class="ai-ws-text" v-if="!aiWsError">连接中...</text>
        <text class="ai-ws-error" v-else>{{ aiWsError }}</text>
        <text class="ai-ws-retry" v-if="aiWsError" @click="retryAiConnect">点击重试</text>
      </view>
    </view>

    <view class="bottom-safe"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNowWeather } from '@/api/weather'
import { fetchTodaySteps } from '@/utils/steps'
import { reportTodaySteps, getTodayStats, getDailyStats } from '@/api/stats'
import { getWorkoutList } from '@/api/workout'
import { createDefaultWeeklyPlan, normalizeDayPlan } from '@/api/plan'
import { formatDate } from '@/utils'
import { WS_BASE_URL } from '@/utils/request.js'
import { get, post } from '@/utils/request.js'

const statusBarHeight = ref(20)

// ---- 天气数据 ----
const weather = ref({
  temp: '--',
  feelsLike: '--',
  text: '加载中',
  city: '',
  aqi: null,
  aqiLevel: '',
  aqiColor: '',
  sportCategory: '',
  sportText: ''
})

// ---- 问候语 ----
const now = new Date()
const hour = now.getHours()

const greetingText = computed(() => {
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const greetingEmoji = computed(() => {
  if (hour < 12) return '☀️'
  if (hour < 18) return '🌤️'
  return '🌙'
})

const todayDate = computed(() => {
  const m = now.getMonth() + 1
  const d = now.getDate()
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${m}月${d}日 ${days[now.getDay()]}`
})

// ---- 步数数据（真实数据） ----
const stepsCount = ref(0)
const yesterdaySteps = ref(0)
const stepsProgress = computed(() => Math.min(100, Math.round((stepsCount.value / 8000) * 100)))
const circumference = 2 * Math.PI * 52 // ≈ 326.7

const calories = ref(0)
const distance = ref(0)
const duration = ref(0)

// ---- 打卡系统 ----
const checkedIn = ref(false)
const checkinDays = ref(0)

function loadCheckinData() {
  const records = uni.getStorageSync('checkin_records')
  const today = formatDate(new Date(), 'YYYY-MM-DD')
  if (records) {
    try {
      const data = JSON.parse(records)
      checkedIn.value = !!data[today]
      checkinDays.value = Object.keys(data).length
    } catch (e) {
      checkedIn.value = false
      checkinDays.value = 0
    }
  }
}

function handleCheckin() {
  if (checkedIn.value) {
    uni.showToast({ title: '今天已打卡', icon: 'none' })
    return
  }
  const today = formatDate(new Date(), 'YYYY-MM-DD')
  const raw = uni.getStorageSync('checkin_records')
  const records = raw ? JSON.parse(raw) : {}
  records[today] = true
  uni.setStorageSync('checkin_records', JSON.stringify(records))
  checkedIn.value = true
  checkinDays.value = Object.keys(records).length
  uni.showToast({ title: '打卡成功！继续加油', icon: 'none' })
}

// ---- 今日运动数据 ----
const todayStats = ref({})
const todayWorkouts = ref([])
const hasTodayWorkout = computed(() => todayStats.value.statDate && (todayStats.value.recordCount || 0) > 0)
const todayDurationStr = computed(() => {
  const d = todayStats.value.duration || 0
  if (!d) return '00:00'
  const m = Math.floor(d / 60)
  const s = d % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})
const todayPaceStr = computed(() => {
  const dist = (todayStats.value.distance || 0) / 1000
  const dur = (todayStats.value.duration || 0)
  if (dist <= 0 || dur <= 0) return "0'00\""
  const pace = Math.round(dur / dist)
  const min = Math.floor(pace / 60)
  const sec = pace % 60
  return `${min}'${String(sec).padStart(2, '0')}"`
})
const todayDistanceStr = computed(() => {
  const dist = (todayStats.value.distance || 0) / 1000
  return dist > 0 ? dist.toFixed(1) : '0.0'
})

const workoutTypeMap = {
  1: { name: '跑步', color: '#22c55e', bg: '#ecfdf5' },
  2: { name: '健走', color: '#3b82f6', bg: '#eff6ff' },
  3: { name: '骑行', color: '#f97316', bg: '#fff7ed' }
}

function getWorkoutTypeInfo(type) {
  return workoutTypeMap[type] || { name: '运动', color: '#64748b', bg: '#f8fafc' }
}

const activityColorMap = {
  run: { color: '#22c55e' },
  strength: { color: '#3b82f6' },
  yoga: { color: '#a855f7' },
  rest: { color: '#94a3b8' },
  custom: { color: '#f97316' }
}

function getActivityColor(act) {
  if (act.tags && act.tags.length > 0) {
    const firstTag = act.tags[0]
    if (firstTag.type === 'run') return activityColorMap.run
    if (firstTag.type === 'strength') return activityColorMap.strength
    if (firstTag.type === 'yoga') return activityColorMap.yoga
    if (firstTag.type === 'rest') return activityColorMap.rest
    if (firstTag.type === 'custom') return activityColorMap.custom
  }
  const name = (act.name || '').toLowerCase()
  if (name.includes('跑')) return activityColorMap.run
  if (name.includes('力量') || name.includes('训练')) return activityColorMap.strength
  if (name.includes('瑜伽')) return activityColorMap.yoga
  if (name.includes('休息')) return activityColorMap.rest
  return activityColorMap.custom
}

function formatWorkoutTime(time) {
  if (!time) return ''
  const str = typeof time === 'string' ? time : ''
  return str.substring(0, 16).replace('T', ' ')
}

function formatWorkoutDuration(seconds) {
  if (!seconds || seconds <= 0) return '0分钟'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  if (s === 0) return `${m}分钟`
  return `${m}分${s}秒`
}

function formatWorkoutDistance(meters) {
  if (!meters || meters <= 0) return ''
  if (meters < 1000) return `${Math.round(meters)}米`
  return `${(meters / 1000).toFixed(1)}公里`
}

function buildWorkoutTags(item) {
  const tags = []
  if (item.duration) tags.push({ text: formatWorkoutDuration(item.duration), color: '#3b82f6', bg: '#eff6ff' })
  if (item.distance) tags.push({ text: formatWorkoutDistance(item.distance), color: '#22c55e', bg: '#ecfdf5' })
  if (item.calories) tags.push({ text: `${item.calories}千卡`, color: '#f97316', bg: '#fff7ed' })
  if (tags.length === 0) tags.push({ text: '运动记录', color: '#64748b', bg: '#f8fafc' })
  return tags
}

// ---- 今日目标 ----
const todayPlan = ref(null)

const typeLabelMap = {
  1: { label: '有氧', color: '#22c55e', bg: '#ecfdf5' },
  2: { label: '力量', color: '#3b82f6', bg: '#eff6ff' },
  3: { label: '拉伸', color: '#a855f7', bg: '#fdf4ff' },
  4: { label: 'HIIT', color: '#f97316', bg: '#fff7ed' },
  5: { label: '综合', color: '#64748b', bg: '#f8fafc' },
  6: { label: '休息', color: '#94a3b8', bg: '#f1f5f9' }
}

function getDayOfWeek(date = new Date()) {
  const d = date.getDay()
  return d === 0 ? 7 : d
}

function getWeeksDiff(startMs, endMs) {
  const oneWeek = 7 * 24 * 60 * 60 * 1000
  const diff = endMs - startMs
  return Math.max(1, Math.floor(diff / oneWeek) + 1)
}

/** 从 activities 数组计算总时长 */
function calcTotalDuration(activities) {
  if (!activities || !activities.length) return 0
  return activities.reduce((sum, a) => sum + (a.duration || 0), 0)
}

function loadTodayPlan() {
  try {
    const now = new Date()
    const todayDay = getDayOfWeek(now)

    // 第1层：从 weeklyPlan_current 读取当天计划
    const weeklyRaw = uni.getStorageSync('weeklyPlan_current')
    if (weeklyRaw) {
      const weekly = JSON.parse(weeklyRaw)
      const dayPlan = (weekly.days || []).find(d => d.dayOfWeek === todayDay)
      if (dayPlan) {
        const normalized = normalizeDayPlan(dayPlan)
        const acts = normalized.activities || []
        const totalDur = calcTotalDuration(acts)
        const isRest = acts.some(a => a.type === 'rest')
        todayPlan.value = {
          name: isRest ? '休息自由活动' : (acts[0]?.name || '今日训练'),
          type: isRest ? '休息' : '运动',
          typeColor: isRest ? '#94a3b8' : '#22c55e',
          typeBg: isRest ? '#f1f5f9' : '#ecfdf5',
          duration: totalDur,
          distanceKm: 0,
          description: '',
          activities: acts,
          hasPlan: true,
          planType: isRest ? 'rest' : (dayPlan.type || 'custom')
        }
        return
      }
    }

    // 第2层：从 myTrainingPlans 查找课程
    const savedPlans = uni.getStorageSync('myTrainingPlans')
    let plans = []
    if (savedPlans) {
      try { plans = JSON.parse(savedPlans) } catch (e) { plans = [] }
    }
    if (plans.length > 0) {
      const plan = plans[0]
      let startMs = plan.startDate ? new Date(plan.startDate).getTime() : (plan.createdAt ? new Date(plan.createdAt).getTime() : Date.now())
      const currentWeek = Math.min(plan.totalWeeks || 1, getWeeksDiff(startMs, now.getTime()))
      // 找到当天所有课程（支持同一天多课程）
      const dayCourses = (plan.courses || []).filter(c => c.week === currentWeek && c.day === todayDay)
      if (dayCourses.length > 0) {
        const typeIconMap = { 1: '🏃', 2: '💪', 3: '🧘', 4: '⚡', 5: '⚡', 6: '😴' }
        const typeNameMap = { 1: '跑步', 2: '力量', 3: '瑜伽', 4: 'HIIT', 5: '综合', 6: '休息' }
        const rTypeMap = { 1: 'run', 2: 'strength', 3: 'yoga', 4: 'custom', 5: 'custom', 6: 'rest' }
        const activities = dayCourses.map(c => ({
          icon: typeIconMap[c.type] || '🏃',
          type: rTypeMap[c.type] || 'custom',
          name: c.name || '训练',
          duration: c.duration || 0,
          tags: [typeNameMap[c.type] || '运动'],
          summary: '',
          details: c
        }))
        const totalDur = calcTotalDuration(activities)
        const isRest = activities.some(a => a.type === 'rest')
        todayPlan.value = {
          name: isRest ? '休息自由活动' : (dayCourses[0].name || '今日训练'),
          type: isRest ? '休息' : '运动',
          typeColor: isRest ? '#94a3b8' : '#22c55e',
          typeBg: isRest ? '#f1f5f9' : '#ecfdf5',
          duration: totalDur,
          distanceKm: 0,
          description: '',
          activities,
          hasPlan: true,
          planType: isRest ? 'rest' : 'custom'
        }
        return
      }
    }

    // 第3层：创建默认计划
    const defaultPlan = createDefaultWeeklyPlan()
    const defaultDayPlan = (defaultPlan.days || []).find(d => d.dayOfWeek === todayDay)
    if (defaultDayPlan) {
      const normalized = normalizeDayPlan(defaultDayPlan)
      const acts = normalized.activities || []
      const totalDur = calcTotalDuration(acts)
      const isRest = acts.some(a => a.type === 'rest')
      todayPlan.value = {
        name: isRest ? '休息自由活动' : (acts[0]?.name || '今日训练'),
        type: isRest ? '休息' : '运动',
        typeColor: isRest ? '#94a3b8' : '#22c55e',
        typeBg: isRest ? '#f1f5f9' : '#ecfdf5',
        duration: totalDur,
        distanceKm: 0,
        description: '',
        activities: acts,
        hasPlan: true,
        planType: isRest ? 'rest' : (defaultDayPlan.type || 'custom')
      }
      return
    }

    todayPlan.value = null
  } catch (e) {
    console.error('[今日目标] 加载失败:', e)
    todayPlan.value = null
  }
}

// ---- 初始化 ----
onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
  loadTodayData()
  fetchModels()
  connectWebSocket()
})

onShow(() => {
  loadTodayData()
})

function getLocation() {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => resolve({ lat: res.latitude, lng: res.longitude }),
      fail: () => {
        // 降级：默认杭州坐标
        resolve({ lat: 30.2741, lng: 120.1551 })
      }
    })
  })
}

async function loadTodayData() {
  // 并行加载天气、步数、今日运动统计和今日目标
  await Promise.all([loadWeatherData(), loadStepsData(), loadTodayWorkout(), loadTodayWorkouts(), loadYesterdaySteps()])
  loadTodayPlan()
  loadCheckinData()
}

async function loadTodayWorkout() {
  try {
    const res = await getTodayStats()
    if (res.code === 200) {
      todayStats.value = res.data || {}
    }
  } catch (e) {
    console.error('[今日运动] 加载失败:', e)
  }
}

async function loadTodayWorkouts() {
  try {
    const now = new Date()
    const todayStr = formatDate(now, 'YYYY-MM-DD')
    const res = await getWorkoutList({ page: 1, size: 10, startDate: todayStr, endDate: todayStr })
    if (res.code === 200 && res.data) {
      todayWorkouts.value = res.data.list || []
    }
  } catch (e) {
    console.error('[今日运动记录] 加载失败:', e)
  }
}

async function loadWeatherData() {
  try {
    const { lat, lng } = await getLocation()
    console.log('[天气] 获取到定位:', lat, lng)
    const res = await getNowWeather(lat, lng)
    console.log('[天气] API返回:', JSON.stringify(res))
    if (res.code === 200 && res.data) {
      weather.value = res.data
      console.log('[天气] 页面数据已更新:', JSON.stringify(weather.value))
    } else {
      console.warn('[天气] 返回code异常:', res.code, res.message)
    }
  } catch (e) {
    console.error('[天气] 请求失败:', e.message || e)
  }
}

async function loadStepsData() {
  try {
    const result = await fetchTodaySteps()
    console.log('[步数] 获取结果:', result)

    // 直接使用返回的步数数据
    // （微信小程序端：内部已通过 tryWeRunWithAuth 完成解密；
    //  H5/App 端：来自后端 API 或原生 API）
    stepsCount.value = result.steps || 0
    if (result.calories !== undefined) calories.value = result.calories
    if (result.distance !== undefined) distance.value = result.distance
    if (result.duration !== undefined) duration.value = result.duration

    // 如果是原生读取的数据，同步到后端备份
    if (['health', 'pedometer'].includes(result.source) && result.steps > 0) {
      reportTodaySteps({
        steps: result.steps,
        calories: calories.value,
        distance: distance.value,
        duration: duration.value,
        source: result.source
      }).catch(() => {})
    }
  } catch (e) {
    console.error('[步数] 加载失败:', e.message || e)
    // 保持默认值 0，不展示错误提示
  }
}

async function loadYesterdaySteps() {
  try {
    const d = new Date()
    d.setDate(d.getDate() - 1)
    const yStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    const res = await getDailyStats(yStr)
    if (res.code === 200 && res.data) {
      yesterdaySteps.value = res.data.steps || res.data.stepCount || 0
    }
  } catch (e) {
    console.warn('[昨日步数] 加载失败:', e.message || e)
  }
}

function goRunning() {
  uni.switchTab({ url: '/pages/running/running' })
}

function goPlan() {
  uni.switchTab({ url: '/pages/goal/goal' })
}

function goToTodayTask() {
  uni.navigateTo({ url: '/pages/task/task-detail' })
}

function goToWorkoutDetail(id) {
  if (id) {
    uni.navigateTo({ url: `/pages/workout/detail?id=${id}` })
  }
}

function goToPlanDetail() {
  if (!todayPlan.value || !todayPlan.value.hasPlan) return

  const pType = todayPlan.value.planType
  if (pType === 'rest') {
    uni.showToast({ title: '今天休息，好好恢复 💤', icon: 'none' })
    return
  }

  if (pType === 'run') {
    uni.switchTab({ url: '/pages/running/running' })
    return
  }

  // 力量 / 瑜伽 / 自定义 → 去 workout
  uni.navigateTo({ url: '/pages/workout/workout' })
}

function goToCreatePlan() {
  uni.switchTab({ url: '/pages/goal/goal' })
}

// ---- AI 教练 ----
const modelList = ref([])
const selectedModelId = ref(uni.getStorageSync('ai_selected_model') || '')
const showModelPicker = ref(false)

const apiKeyMap = ref(JSON.parse(uni.getStorageSync('ai_api_key_map') || '{}'))
const customApiKey = computed(() => apiKeyMap.value[selectedModelId.value] || '')

const testingConnection = ref(false)
const testResult = ref(null)

const testConnection = async () => {
  if (testingConnection.value) return
  testingConnection.value = true
  testResult.value = null
  try {
    const res = await post('/ai/test-connection', {
      modelId: selectedModelId.value,
      apiKey: customApiKey.value || undefined
    })
    if (res && res.code === 200) {
      testResult.value = { ok: true, message: '连接成功！' }
    } else {
      testResult.value = { ok: false, message: res?.message || '连接失败' }
    }
  } catch (e) {
    testResult.value = { ok: false, message: '网络请求失败，请检查后端服务' }
  } finally {
    testingConnection.value = false
  }
}

const updateApiKey = (val) => {
  apiKeyMap.value[selectedModelId.value] = val
  uni.setStorageSync('ai_api_key_map', JSON.stringify(apiKeyMap.value))
}

const fetchModels = async () => {
  try {
    const res = await get('/ai/models')
    const list = res?.data || res
    if (Array.isArray(list)) {
      modelList.value = list
      if (!selectedModelId.value) {
        const active = list.find(m => m.active)
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

const currentModelName = computed(() => {
  const m = modelList.value.find(m => m.id === selectedModelId.value)
  return m ? m.name : 'AI 教练'
})

const toggleModelPicker = () => {
  showModelPicker.value = !showModelPicker.value
}

const aiInputText = ref('')
const aiMessages = ref([])
const aiLoading = ref(false)
const aiScrollTop = ref(0)
const aiWsConnected = ref(false)
const aiWsError = ref('')
const aiWsConnecting = ref(false)
const aiCurrentAssistantIndex = ref(-1)
let aiConnectTimer = null
let aiWsSessionId = 0
let aiInJsonBlock = false
let aiJsonBuffer = ''

const aiQuickTags = [
  '生成下周训练计划',
  '制定减脂方案',
  '增肌训练建议',
  '分析运动数据'
]

function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000
    return Date.now() >= exp
  } catch {
    return false
  }
}

const connectWebSocket = () => {
  if (aiWsConnecting.value || aiWsConnected.value) return

  const token = uni.getStorageSync('token')
  if (!token) {
    aiWsError.value = '请先登录后再使用 AI 功能'
    return
  }

  if (isTokenExpired(token)) {
    aiWsError.value = '登录已过期，请重新登录'
    return
  }

  aiWsConnecting.value = true
  const sessionId = ++aiWsSessionId

  const url = `${WS_BASE_URL}?token=${token}`
  console.log('正在连接 WebSocket:', url)

  uni.onSocketOpen(() => {
    if (sessionId !== aiWsSessionId) return
    clearTimeout(aiConnectTimer)
    aiConnectTimer = null
    aiWsConnecting.value = false
    aiWsConnected.value = true
    aiWsError.value = ''
    console.log('WebSocket 连接成功')
  })

  uni.onSocketMessage((res) => {
    if (sessionId !== aiWsSessionId) return
    try {
      const data = JSON.parse(res.data)
      handleWsMessage(data)
    } catch (e) {
      console.error('WebSocket 消息解析失败:', res.data)
    }
  })

  uni.onSocketError((err) => {
    if (sessionId !== aiWsSessionId) return
    clearTimeout(aiConnectTimer)
    aiConnectTimer = null
    aiWsConnecting.value = false
    aiWsConnected.value = false
    aiWsError.value = 'AI 连接失败，请检查网络或后端服务是否启动'
    console.error('WebSocket 错误:', err)
  })

  uni.onSocketClose(() => {
    if (sessionId !== aiWsSessionId) return
    clearTimeout(aiConnectTimer)
    aiConnectTimer = null
    aiWsConnecting.value = false
    aiWsConnected.value = false
    console.log('WebSocket 关闭')
  })

  uni.connectSocket({ url })

  aiConnectTimer = setTimeout(() => {
    if (sessionId !== aiWsSessionId) return
    if (!aiWsConnected.value) {
      aiWsSessionId++
      uni.closeSocket()
      aiWsConnecting.value = false
      aiWsConnected.value = false
      aiWsError.value = 'AI 连接超时，请检查网络或尝试重新登录'
    }
  }, 10000)
}

const disconnectWebSocket = () => {
  clearTimeout(aiConnectTimer)
  aiConnectTimer = null
  aiWsSessionId++
  aiInJsonBlock = false
  aiJsonBuffer = ''
  uni.closeSocket()
  aiWsConnecting.value = false
  aiWsConnected.value = false
  aiWsError.value = ''
}

const retryAiConnect = () => {
  disconnectWebSocket()
  connectWebSocket()
}

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

const stripPlanJson = (text) => {
  if (!text) return ''
  return text.replace(/<<<PLAN_JSON>>>[\s\S]*?<<<PLAN_JSON_END>>>/g, '').trim()
}

let aiScrollTimer = null
const throttledScroll = () => {
  if (aiScrollTimer) return
  aiScrollTimer = setTimeout(() => {
    scrollToBottom()
    aiScrollTimer = null
  }, 300)
}

const handleWsMessage = (data) => {
  switch (data.type) {
    case 'start':
      aiLoading.value = true
      aiCurrentAssistantIndex.value = aiMessages.value.length
      aiMessages.value.push({ id: Date.now() + '_' + Math.random().toString(36).slice(2, 7), role: 'assistant', content: '', displayContent: '', isPlan: false })
      scrollToBottom()
      break

    case 'delta':
      if (aiCurrentAssistantIndex.value >= 0) {
        const msg = aiMessages.value[aiCurrentAssistantIndex.value]
        const chunk = data.content

        if (!aiInJsonBlock) {
          msg.content += chunk
          if (msg.content.includes('<<<PLAN_JSON>>>')) {
            aiInJsonBlock = true
            const idx = msg.content.indexOf('<<<PLAN_JSON>>>')
            aiJsonBuffer = msg.content.substring(idx)
            msg.content = msg.content.substring(0, idx)
            msg.displayContent = cleanAiText(msg.content)
          } else {
            msg.displayContent = cleanAiText(msg.content)
          }
        } else {
          aiJsonBuffer += chunk
          if (aiJsonBuffer.includes('<<<PLAN_JSON_END>>>')) {
            aiInJsonBlock = false
            aiJsonBuffer = ''
          }
        }
        throttledScroll()
      }
      break

    case 'done':
      aiLoading.value = false
      if (aiCurrentAssistantIndex.value >= 0) {
        const msg = aiMessages.value[aiCurrentAssistantIndex.value]
        aiInJsonBlock = false
        const rawContent = msg.content + aiJsonBuffer
        aiJsonBuffer = ''
        const planData = parsePlanJson(rawContent)
        msg.displayContent = cleanAiText(msg.content)
        msg.isPlan = !!planData || msg.displayContent.includes('计划') || msg.displayContent.includes('方案') || msg.displayContent.includes('训练')
        msg.planData = planData
      }
      aiCurrentAssistantIndex.value = -1
      scrollToBottom()
      break

    case 'error':
      aiLoading.value = false
      aiCurrentAssistantIndex.value = -1
      let errMsg = data.message || '请求失败'
      if (errMsg.includes('401') || errMsg.includes('Unauthorized')) {
        errMsg = 'AI 认证失败，请检查后端配置的 API Key 是否有效或已过期。'
      }
      aiMessages.value.push({ id: Date.now() + '_' + Math.random().toString(36).slice(2, 7), role: 'assistant', content: `错误: ${errMsg}` })
      scrollToBottom()
      break
  }
}

const sendQuick = (text) => {
  aiInputText.value = text
  sendAiMessage()
}

const buildSystemPrompt = () => {
  const s = aiStatsData.value || {}
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

const sendAiMessage = () => {
  const text = aiInputText.value.trim()
  if (!text || aiLoading.value) return
  if (!aiWsConnected.value) {
    uni.showToast({ title: 'AI 连接中，请稍候', icon: 'none' })
    return
  }

  aiMessages.value.push({ id: Date.now() + '_' + Math.random().toString(36).slice(2, 7), role: 'user', content: text })
  aiInputText.value = ''
  scrollToBottom()

  const systemPrompt = buildSystemPrompt()
  const chatMessages = [
    { role: 'system', content: systemPrompt },
    ...aiMessages.value.map(m => ({ role: m.role, content: m.content }))
  ]

  const payload = JSON.stringify({ type: 'chat', messages: chatMessages, modelId: selectedModelId.value || undefined })
  uni.sendSocketMessage({ data: payload })
}

const scrollToBottom = () => {
  nextTick(() => {
    aiScrollTop.value = aiMessages.value.length * 9999
  })
}

const copyPlan = (content) => {
  uni.setClipboardData({
    data: content,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

const usePlan = (planData) => {
  uni.setStorageSync('ai_generated_plan', typeof planData === 'object' ? planData : { rawText: planData })
  uni.showToast({ title: '已应用方案', icon: 'success' })
  uni.navigateTo({ url: '/pages/goal/create' })
}

const aiStatsData = computed(() => {
  const list = todayWorkouts.value || []
  const totalWorkouts = list.length
  const totalDistance = list.reduce((sum, r) => sum + (r.distance || 0), 0)
  const totalDuration = list.reduce((sum, r) => sum + (r.duration || 0), 0)
  const totalCalories = list.reduce((sum, r) => sum + (r.calories || 0), 0)

  const typeCount = {}
  list.forEach(r => {
    typeCount[r.type] = (typeCount[r.type] || 0) + 1
  })

  return {
    summary: {
      totalWorkouts,
      totalDistanceStr: totalDistance >= 1000 ? (totalDistance / 1000).toFixed(2) + 'km' : totalDistance + 'm',
      totalCaloriesStr: totalCalories + '千卡',
      streakDays: 0,
      weeklyWorkouts: 0,
      monthlyWorkouts: 0
    },
    todayStats: todayStats.value || {},
    trendData: {
      dates: list.slice(0, 7).map(r => (r.startTime || '').substring(5, 10)),
      distances: list.slice(0, 7).map(r => r.distance || 0),
      durations: list.slice(0, 7).map(r => r.duration || 0),
      calories: list.slice(0, 7).map(r => r.calories || 0)
    },
    recordSummary: {
      totalWorkouts,
      totalDistance,
      totalDuration,
      totalCalories,
      typeCount
    }
  }
})

</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 0 28rpx;
  transition: background 0.3s;
}

.status-bar {
  width: 100%;
}

// ===== 问候语 =====
.greeting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 8rpx 24rpx;
}

.greeting-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.greeting-hi {
  font-size: 44rpx;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -1rpx;
}

.greeting-sub {
  font-size: 26rpx;
  color: var(--text-tertiary);
}

.greeting-emoji {
  font-size: 60rpx;
}

// ===== 天气卡片 =====
.weather-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

.weather-left {
  display: flex;
  align-items: baseline;
  gap: 14rpx;
}

.weather-temp {
  font-size: 56rpx;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.weather-desc {
  font-size: 24rpx;
  color: var(--text-tertiary);
}

.weather-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.weather-loc-aqi-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.weather-aqi {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #f0fdf4;
  border-radius: 20rpx;
  padding: 8rpx 18rpx;
}

.aqi-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #22c55e;
}

.aqi-text {
  font-size: 22rpx;
  color: #16a34a;
  font-weight: 600;
}

.aqi-num {
  font-size: 22rpx;
  color: #16a34a;
  font-weight: 700;
}

.weather-loc {
  font-size: 22rpx;
  color: var(--text-tertiary);
}

.weather-sport {
  max-width: 280rpx;
  margin-top: 4rpx;
}

.sport-index-text {
  font-size: 20rpx;
  color: var(--text-tertiary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

// ===== 今日活动卡片 =====
.activity-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

.ac-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.ac-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.ac-date {
  font-size: 24rpx;
  color: var(--text-tertiary);
}

.ac-main {
  display: flex;
  align-items: center;
  gap: 40rpx;
  padding: 10rpx 0 24rpx;
}

// 圆形进度环
.steps-ring-wrap {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
}

.steps-svg {
  width: 160rpx;
  height: 160rpx;
}

.steps-icon-box {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0fdf4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.steps-icon {
  font-size: 40rpx;
}

// 步数信息
.steps-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.steps-num {
  font-size: 64rpx;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -2rpx;
}

.steps-label {
  font-size: 24rpx;
  color: var(--text-tertiary);
  margin-top: 4rpx;
}

.steps-yesterday {
  font-size: 22rpx;
  color: var(--text-tertiary);
  margin-top: 6rpx;
}

.steps-checkin {
  font-size: 22rpx;
  color: #f59e0b;
  margin-top: 4rpx;
}

// 已打卡状态
.checkin-done {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  box-shadow: 0 4rpx 16rpx rgba(245, 158, 11, 0.3) !important;
}

.steps-pct {
  font-size: 28rpx;
  font-weight: 700;
  color: #22c55e;
  margin-top: 6rpx;
}

// 打卡按钮
.checkin-btn {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 16rpx rgba(34, 197, 94, 0.3);
  margin-left: auto;

  &:active {
    transform: scale(0.92);
    opacity: 0.85;
  }
}

.checkin-icon {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.checkin-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
  margin-top: 2rpx;
}

// 底部三列数据
.ac-stats {
  display: flex;
  border-top: 1rpx solid var(--border-color);
  padding-top: 20rpx;
}

.ac-stat {
  flex: 1;
  text-align: center;
  position: relative;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 10%;
    height: 80%;
    width: 1rpx;
    background: #f1f5f9;
  }
}

.acs-val {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-secondary);
}

.acs-unit {
  font-size: 22rpx;
  font-weight: 400;
  color: var(--text-tertiary);
  margin-left: 4rpx;
}

// ===== 运动分类 =====
// ===== 快捷操作栏 =====
.action-bar {
  display: flex;
  gap: 16rpx;
  padding: 0 16px;
  margin-bottom: 20rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  border-radius: 12px;
  gap: 8rpx;
}

.action-primary {
  flex: 7;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 4rpx 16rpx rgba(34, 197, 94, 0.25);
}

.action-icon {
  font-size: 24rpx;
  color: #fff;
}

.action-primary .action-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 700;
}

.action-secondary {
  flex: 3;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-color);
}

.action-secondary .action-text {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

// ===== 今日训练卡片 =====
.session-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

.sc-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.sc-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.sc-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #22c55e;
}

.sc-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.sc-type {
  font-size: 24rpx;
  color: #22c55e;
  background: #f0fdf4;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
}

.sc-body {
  display: flex;
  gap: 24rpx;
}

.sc-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.sc-stat {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.scs-val {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.scs-label {
  font-size: 22rpx;
  color: var(--text-tertiary);
}

// 今日训练记录卡片列表
.workout-cards {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.workout-card {
  background: var(--bg-secondary);
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;

  &:active {
    background: #f1f5f9;
  }
}

.wc-main {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.wc-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.wc-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}

.wc-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.wc-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-secondary);
}

.wc-time {
  font-size: 22rpx;
  color: var(--text-tertiary);
}

.wc-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding-left: 88rpx;
}

.wc-tag {
  font-size: 22rpx;
  font-weight: 600;
  padding: 6rpx 16rpx;
  border-radius: 10rpx;
  line-height: 1;
}

.sc-map {
  width: 240rpx;
  height: 140rpx;
  background: var(--bg-secondary);
  border-radius: 16rpx;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.route-svg {
  width: 100%;
  height: 100%;
}

// ===== 今日目标卡片 =====
.plan-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

.plan-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.plan-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.plan-action {
  font-size: 26rpx;
  color: #22c55e;
  font-weight: 600;
}

.plan-body {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 10rpx;
}

.plan-tag {
  font-size: 22rpx;
  font-weight: 600;
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.plan-name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-secondary);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-duration {
  font-size: 26rpx;
  color: var(--text-tertiary);
  font-weight: 500;
  flex-shrink: 0;
}

.plan-meta {
  font-size: 24rpx;
  color: var(--text-tertiary);
  padding-left: 2rpx;
}

// ===== 标准化活动卡片 =====
.act-card-list {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.act-card {
  background: var(--bg-secondary);
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-height: 120rpx;

  &.act-card-rest {
    background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--border-color) 100%);
    border: 1rpx dashed var(--text-tertiary);
  }
}

.act-card-row1 {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.act-icon {
  font-size: 36rpx;
  width: 48rpx;
  text-align: center;
  flex-shrink: 0;
}

.act-name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.act-duration {
  font-size: 26rpx;
  color: var(--accent-green);
  font-weight: 600;
  flex-shrink: 0;
}

.act-card-row2 {
  padding-left: 62rpx;
}

.act-summary {
  font-size: 24rpx;
  color: var(--text-tertiary);
  line-height: 1.4;
}

.act-card-row3 {
  padding-left: 62rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.act-tag {
  font-size: 20rpx;
  font-weight: 500;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  background: var(--bg-card);
  color: var(--text-tertiary);
  border: 1rpx solid var(--border-color);
}

.plan-description {
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  background: var(--bg-secondary);
  border-radius: 12rpx;
}

.plan-desc-text {
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.5;
}

// 今日目标空状态
.plan-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 20rpx 20rpx;
}

.plan-empty-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #64748b;
}

.plan-empty-hint {
  font-size: 24rpx;
  color: var(--text-tertiary);
  margin-top: 8rpx;
}

.plan-empty-btn {
  margin-top: 20rpx;
  padding: 14rpx 40rpx;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 12rpx;
}

// ===== 底部安全区 =====
// ===== AI 教练卡片 =====
.ai-coach-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
  display: flex;
  flex-direction: column;
  height: 720rpx;
  overflow: hidden;
  position: relative;
}

.ai-coach-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  flex-shrink: 0;
}

.ai-coach-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-coach-icon {
  font-size: 40rpx;
}

.ai-coach-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

// 模型选择器
.ai-model-selector {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: var(--bg-secondary);

  &:active {
    background: var(--bg-secondary);
  }
}

.ai-model-name {
  font-size: 24rpx;
  color: var(--accent-purple);
  font-weight: 500;
}

.ai-model-arrow {
  font-size: 18rpx;
  color: #999;
  transition: transform 0.2s;
}

.ai-model-arrow-up {
  transform: rotate(180deg);
}

// 模型设置面板
.ai-settings-panel {
  position: absolute;
  top: 90rpx;
  left: 20rpx;
  right: 20rpx;
  background: var(--bg-card);
  border-radius: 20rpx;
  box-shadow: 0 8rpx 32rpx var(--shadow-color);
  z-index: 20;
  overflow: hidden;
  max-height: 50vh;
  overflow-y: auto;
}

.ai-settings-section {
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.ai-settings-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.ai-provider-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ai-provider-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-radius: 16rpx;
  background: var(--bg-secondary);
  border: 2rpx solid transparent;

  &:active {
    background: var(--bg-secondary);
  }
}

.ai-provider-active {
  border-color: var(--accent-purple);
  background: var(--bg-secondary);
}

.ai-provider-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.ai-provider-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.ai-provider-id {
  font-size: 22rpx;
  color: #999;
}

.ai-provider-check {
  color: #667eea;
  font-size: 28rpx;
  font-weight: bold;
}

.ai-settings-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: var(--bg-secondary);
  border-radius: 12rpx;
  font-size: 26rpx;
  color: var(--text-primary);
  border: 2rpx solid var(--border-color);
  box-sizing: border-box;
}

.ai-test-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-wrap: wrap;
}

.ai-test-btn {
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 26rpx;
  color: #fff;

  &:active {
    opacity: 0.85;
  }
}

.ai-test-running {
  opacity: 0.6;
  pointer-events: none;
}

.ai-test-result {
  font-size: 26rpx;
}

.ai-test-success {
  color: #10b981;
}

.ai-test-fail {
  color: #e74c3c;
}

// 消息区域
.ai-messages {
  flex: 1;
  min-height: 0;
  height: 0;
  padding: 20rpx 0;
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
    background: var(--bg-secondary);
    border: 1rpx solid var(--border-color);
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
  background: var(--bg-secondary);
}

.ai-message-user .ai-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-text {
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-message-user .ai-text {
  color: #fff;
}

// AI 思考中状态条
.ai-thinking-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 16rpx 0;
  background: var(--bg-secondary);
  border-top: 1rpx solid var(--border-color);
  flex-shrink: 0;
}

.ai-thinking-dots {
  display: flex;
  gap: 8rpx;
}

.ai-thinking-text {
  font-size: 24rpx;
  color: #999;
}

.ai-dot {
  width: 12rpx;
  height: 12rpx;
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
  background: var(--bg-card);
  border: 1rpx solid var(--border-color);
  font-size: 24rpx;
  color: var(--text-secondary);

  &:active {
    background: var(--bg-secondary);
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
  padding: 0 0 16rpx;
  flex-shrink: 0;
}

.ai-tag {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: var(--bg-secondary);
  font-size: 26rpx;
  color: var(--accent-purple);
  border: 1rpx solid var(--border-color);

  &:active {
    background: var(--bg-secondary);
  }
}

// 输入区域
.ai-input-area {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 0 0;
  border-top: 1rpx solid var(--border-color);
  background: var(--bg-secondary);
  flex-shrink: 0;
}

.ai-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  background: var(--bg-card);
  border-radius: 36rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  border: 1rpx solid var(--border-color);
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
  color: var(--text-tertiary);
  background: var(--bg-secondary);
  border-top: 1rpx solid var(--border-color);
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

// ===== 底部安全区 =====
.bottom-safe {
  height: calc(100rpx + env(safe-area-inset-bottom));
}
</style>
