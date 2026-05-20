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
          <text class="steps-target">目标 {{ targetSteps }} 步</text>
          <text class="steps-pct">{{ stepsProgress }}%</text>
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

    <!-- ===== 今日计划卡片 ===== -->
    <view class="plan-card" @click="goToPlanDetail">
      <!-- 有今日计划 -->
      <template v-if="todayPlan && todayPlan.hasPlan">
        <view class="plan-head">
          <text class="plan-title">今日计划</text>
          <text class="plan-action">去执行 ›</text>
        </view>
        <view class="plan-body">
          <view class="plan-tag" :style="{ background: todayPlan.typeBg, color: todayPlan.typeColor }">
            {{ todayPlan.type }}
          </view>
          <text class="plan-name">{{ todayPlan.name }}</text>
          <text class="plan-duration" v-if="todayPlan.duration > 0">{{ todayPlan.duration }}分钟</text>
        </view>
        <view class="plan-meta" v-if="todayPlan.distanceKm > 0">
          <text>{{ todayPlan.type }} · {{ todayPlan.distanceKm }}公里</text>
        </view>
      </template>
      <!-- 无今日计划 -->
      <template v-else>
        <view class="plan-head">
          <text class="plan-title">今日计划</text>
        </view>
        <view class="plan-empty">
          <text class="plan-empty-text">暂无今日计划</text>
          <text class="plan-empty-hint">去「定制」页面创建训练计划吧</text>
          <view class="plan-empty-btn" @click.stop="goToCreatePlan">去定制</view>
        </view>
      </template>
    </view>

    <!-- ===== 今日训练卡片 ===== -->
    <view class="session-card" @click="goToTodayTask">
      <view class="sc-head">
        <view class="sc-title-row">
          <view class="sc-dot"></view>
          <text class="sc-title">今日训练</text>
        </view>
        <text class="sc-type">{{ todayStats.recordCount || 0 }} 次运动 ›</text>
      </view>
      <view class="sc-body">
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

    <view class="bottom-safe"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNowWeather } from '@/api/weather'
import { fetchTodaySteps } from '@/utils/steps'
import { reportTodaySteps, reportWeRunData, getTodayStats } from '@/api/stats'

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
const targetSteps = ref(8000)
const stepsProgress = computed(() => Math.min(100, Math.round((stepsCount.value / targetSteps.value) * 100)))
const circumference = 2 * Math.PI * 52 // ≈ 326.7

const calories = ref(0)
const distance = ref(0)
const duration = ref(0)

// ---- 今日运动数据 ----
const todayStats = ref({})
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

// ---- 今日计划 ----
const todayPlan = ref(null)

const typeLabelMap = {
  1: { label: '有氧', color: '#22c55e', bg: '#ecfdf5' },
  2: { label: '力量', color: '#3b82f6', bg: '#eff6ff' },
  3: { label: '拉伸', color: '#a855f7', bg: '#fdf4ff' },
  4: { label: 'HIIT', color: '#f97316', bg: '#fff7ed' },
  5: { label: '综合', color: '#64748b', bg: '#f8fafc' }
}

const weeklyTypeMap = {
  run: { label: '跑步', color: '#22c55e', bg: '#ecfdf5' },
  strength: { label: '力量', color: '#3b82f6', bg: '#eff6ff' },
  yoga: { label: '瑜伽', color: '#a855f7', bg: '#fdf4ff' },
  rest: { label: '休息', color: '#94a3b8', bg: '#f1f5f9' },
  custom: { label: '自定义', color: '#f97316', bg: '#fff7ed' }
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

function loadTodayPlan() {
  try {
    // 1. 优先读取 myTrainingPlans（多周计划）
    const savedPlans = uni.getStorageSync('myTrainingPlans')
    let plans = []
    if (savedPlans) {
      try { plans = JSON.parse(savedPlans) } catch (e) { plans = [] }
    }

    const now = new Date()
    const todayDay = getDayOfWeek(now)

    if (plans.length > 0) {
      const plan = plans[0]
      let startMs
      if (plan.startDate) {
        startMs = new Date(plan.startDate).getTime()
      } else if (plan.createdAt) {
        startMs = new Date(plan.createdAt).getTime()
      } else if (typeof plan.id === 'number' && plan.id > 1000000000000) {
        startMs = plan.id
      } else {
        startMs = Date.now()
      }
      const currentWeek = Math.min(plan.totalWeeks || 1, getWeeksDiff(startMs, now.getTime()))
      const course = (plan.courses || []).find(c => c.week === currentWeek && c.day === todayDay)

      if (course) {
        const typeInfo = typeLabelMap[course.type] || typeLabelMap[5]
        const totalDistance = (course.exercises || []).reduce((sum, ex) => sum + (ex.distance || 0), 0)
        todayPlan.value = {
          name: course.name,
          type: typeInfo.label,
          typeColor: typeInfo.color,
          typeBg: typeInfo.bg,
          duration: course.duration || 0,
          distanceKm: totalDistance > 0 ? (totalDistance / 1000).toFixed(1) : 0,
          hasPlan: true
        }
        return
      }
    }

    // 2. Fallback 到 weeklyPlan_current
    const weeklyRaw = uni.getStorageSync('weeklyPlan_current')
    if (weeklyRaw) {
      const weekly = JSON.parse(weeklyRaw)
      const dayPlan = (weekly.days || []).find(d => d.dayOfWeek === todayDay)
      if (dayPlan && dayPlan.type !== 'rest') {
        const typeInfo = weeklyTypeMap[dayPlan.type] || weeklyTypeMap.custom
        const duration = dayPlan.details?.duration || 0
        const distance = dayPlan.details?.runParams?.distance || 0
        todayPlan.value = {
          name: dayPlan.title || '今日训练',
          type: typeInfo.label,
          typeColor: typeInfo.color,
          typeBg: typeInfo.bg,
          duration,
          distanceKm: distance > 0 ? Number(distance).toFixed(1) : 0,
          hasPlan: true
        }
        return
      }
    }

    todayPlan.value = null
  } catch (e) {
    console.error('[今日计划] 加载失败:', e)
    todayPlan.value = null
  }
}

// ---- 初始化 ----
onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
  loadTodayData()
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
  // 并行加载天气、步数、今日运动统计和今日计划
  await Promise.all([loadWeatherData(), loadStepsData(), loadTodayWorkout()])
  loadTodayPlan()
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

    // 处理微信运动加密数据（需要后端解密）
    if (result.source === 'werun_encrypted' && result.needDecrypt) {
      try {
        const decryptRes = await reportWeRunData({
          encryptedData: result.encryptedData,
          iv: result.iv
        })
        if (decryptRes.code === 200 && decryptRes.data) {
          stepsCount.value = decryptRes.data.steps || 0
          calories.value = decryptRes.data.calories || 0
          distance.value = decryptRes.data.distance || 0
          duration.value = decryptRes.data.duration || 0
        }
      } catch (err) {
        console.warn('[步数] 微信运动解密失败:', err)
      }
      return
    }

    // 普通数据直接展示
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

function goRunning() {
  uni.switchTab({ url: '/pages/running/running' })
}

function goPlan() {
  uni.navigateTo({ url: '/pages/goal/goal' })
}

function goToTodayTask() {
  uni.navigateTo({ url: '/pages/task/task-detail' })
}

function goToPlanDetail() {
  uni.navigateTo({ url: '/pages/task/task-detail' })
}

function goToCreatePlan() {
  uni.navigateTo({ url: '/pages/goal/goal' })
}
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 0 28rpx;
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
  color: #1c1c1e;
  letter-spacing: -1rpx;
}

.greeting-sub {
  font-size: 26rpx;
  color: #94a3b8;
}

.greeting-emoji {
  font-size: 60rpx;
}

// ===== 天气卡片 =====
.weather-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.weather-left {
  display: flex;
  align-items: baseline;
  gap: 14rpx;
}

.weather-temp {
  font-size: 56rpx;
  font-weight: 700;
  color: #1c1c1e;
  line-height: 1;
}

.weather-desc {
  font-size: 24rpx;
  color: #94a3b8;
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
  color: #94a3b8;
}

.weather-sport {
  max-width: 280rpx;
  margin-top: 4rpx;
}

.sport-index-text {
  font-size: 20rpx;
  color: #94a3b8;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

// ===== 今日活动卡片 =====
.activity-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
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
  color: #1c1c1e;
}

.ac-date {
  font-size: 24rpx;
  color: #94a3b8;
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
  color: #1c1c1e;
  line-height: 1;
  letter-spacing: -2rpx;
}

.steps-label {
  font-size: 24rpx;
  color: #94a3b8;
  margin-top: 4rpx;
}

.steps-target {
  font-size: 24rpx;
  color: #94a3b8;
  margin-top: 8rpx;
}

.steps-pct {
  font-size: 28rpx;
  font-weight: 700;
  color: #22c55e;
  margin-top: 6rpx;
}

// 底部三列数据
.ac-stats {
  display: flex;
  border-top: 1rpx solid #f1f5f9;
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
  color: #334155;
}

.acs-unit {
  font-size: 22rpx;
  font-weight: 400;
  color: #94a3b8;
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
  background: #f5f5f7;
  border: 1rpx solid #e5e5e7;
}

.action-secondary .action-text {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

// ===== 今日训练卡片 =====
.session-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
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
  color: #1c1c1e;
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
  color: #1c1c1e;
}

.scs-label {
  font-size: 22rpx;
  color: #94a3b8;
}

.sc-map {
  width: 240rpx;
  height: 140rpx;
  background: #f8fafc;
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

// ===== 今日计划卡片 =====
.plan-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
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
  color: #1c1c1e;
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
  color: #334155;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-duration {
  font-size: 26rpx;
  color: #94a3b8;
  font-weight: 500;
  flex-shrink: 0;
}

.plan-meta {
  font-size: 24rpx;
  color: #94a3b8;
  padding-left: 2rpx;
}

// 今日计划空状态
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
  color: #94a3b8;
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
.bottom-safe {
  height: calc(100rpx + env(safe-area-inset-bottom));
}
</style>
