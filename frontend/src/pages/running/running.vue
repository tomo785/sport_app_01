<template>
  <view class="container">
    <!-- ==================== 准备阶段 ==================== -->
    <block v-if="phase === 'ready'">
      <!-- 活动类型选择条 -->
      <view class="activity-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="activity-bar-inner">
          <view class="activity-center-bg"></view>
          <scroll-view
            class="activity-scroll"
            scroll-x
            :show-scrollbar="false"
            :scroll-left="scrollLeft"
            scroll-with-animation
          >
            <view class="activity-spacer"></view>
            <view
              class="activity-item"
              v-for="(act, idx) in activityList"
              :key="act.key"
              :id="'act-' + idx"
              :class="{ active: selectedActivityIndex === idx }"
              @click="selectActivity(idx)"
            >
              <text class="act-name">{{ act.name }}</text>
            </view>
            <view class="activity-spacer"></view>
          </scroll-view>
        </view>
      </view>

      <!-- GPS 地图 / 非 GPS 活动说明 -->
      <view class="map-area" v-if="isGpsActivity">
        <map
          id="runMap"
          class="gps-map"
          :latitude="currentLat"
          :longitude="currentLng"
          :scale="16"
          :show-location="true"
          :enable-3D="false"
          :show-compass="true"
          :enable-zoom="false"
          :enable-scroll="false"
          :enable-rotate="false"
        />
        <!-- GPS 精度浮层 -->
        <view class="gps-float">
          <text class="gps-dot" :class="gpsSignalClass"></text>
          <text class="gps-float-text" v-if="gpsErrorMsg">{{ gpsErrorMsg }}</text>
          <text class="gps-float-text" v-else>{{ gpsReady ? '精度 ±' + Math.round(gpsAccuracy) + 'm' : '定位中...' }}</text>
        </view>
        <view class="gps-retry" v-if="!gpsReady && gpsErrorMsg" @click="retryGps">
          <text>点击重试</text>
        </view>
      </view>
      <view class="activity-info-card" v-else>
        <text class="activity-info-name">{{ selectedActivity.name }}</text>
        <text class="activity-info-desc">专注训练，享受运动的每一刻</text>
      </view>

      <!-- 底部操作卡片 -->
      <view class="bottom-card" :class="{ compact: !isGpsActivity }">
        <!-- GPS 相关选项（非 GPS 活动自动折叠） -->
        <view class="gps-options-wrap" :class="{ collapsed: !isGpsActivity }">
          <!-- 模式切换 -->
          <view class="mode-tabs">
          <view
            class="mode-tab"
            :class="{ active: goalType === 'none' }"
            @click="goalType = 'none'"
          >
            <text class="mode-tab-text">自由跑</text>
            <text class="mode-tab-desc">无目标限制</text>
          </view>
          <view
            class="mode-tab"
            :class="{ active: goalType !== 'none' }"
            @click="goalType = 'distance'"
          >
            <text class="mode-tab-text">目标跑</text>
            <text class="mode-tab-desc">设定距离或时长</text>
          </view>
        </view>

        <!-- 目标跑 - 子选项 -->
        <view class="sub-options" v-if="goalType !== 'none'">
          <view class="sub-tabs">
            <view
              class="sub-tab"
              :class="{ active: goalType === 'distance' }"
              @click="goalType = 'distance'"
              v-if="isGpsActivity"
            >距离</view>
            <view
              class="sub-tab"
              :class="{ active: goalType === 'duration' }"
              @click="goalType = 'duration'"
            >时长</view>
            <view
              class="sub-tab"
              :class="{ active: goalType === 'custom' }"
              @click="goalType = 'custom'"
            >定制</view>
          </view>

          <scroll-view class="picker-scroll" scroll-x :show-scrollbar="false" v-if="goalType === 'distance'">
            <view class="picker-row">
              <view
                class="picker-chip"
                v-for="d in distanceOptions"
                :key="d"
                :class="{ selected: goalValue === d }"
                @click="goalValue = d"
              >
                <text class="chip-num">{{ d }}</text>
                <text class="chip-unit">KM</text>
              </view>
            </view>
          </scroll-view>

          <scroll-view class="picker-scroll" scroll-x :show-scrollbar="false" v-if="goalType === 'duration'">
            <view class="picker-row">
              <view
                class="picker-chip"
                v-for="t in durationOptions"
                :key="t"
                :class="{ selected: goalValue === t }"
                @click="goalValue = t"
              >
                <text class="chip-num">{{ t }}</text>
                <text class="chip-unit">MIN</text>
              </view>
            </view>
          </scroll-view>

          <!-- 定制跑 - 已创建的定制活动列表 -->
          <scroll-view class="custom-scroll" scroll-y v-if="goalType === 'custom'">
            <view
              class="custom-act-item"
              v-for="act in runningActivities"
              :key="act.id"
              :class="{ selected: selectedActId === act.id }"
              @click="selectCustomActivity(act)"
            >
              <view class="custom-act-left">
                <text class="custom-act-icon">定</text>
              </view>
              <view class="custom-act-info">
                <text class="custom-act-title">{{ act.title }}</text>
                <text class="custom-act-meta">{{ act.warmupCount }}步热身 · {{ act.mainCount }}项主训练 · {{ act.repeatCount }}组循环</text>
              </view>
              <view class="custom-act-check" v-if="selectedActId === act.id">✓</view>
            </view>
            <view class="custom-act-empty" v-if="runningActivities.length === 0">
              <text class="empty-icon">无</text>
              <text class="empty-text">暂无定制活动</text>
              <text class="empty-hint">去「定制」页面创建一个吧</text>
            </view>
          </scroll-view>
        </view>

        </view>
        <!-- 开始按钮 -->
        <view class="start-run-btn" @click="startRunning">
          <text class="start-run-text">{{ startBtnText }}</text>
        </view>
      </view>
    </block>

    <!-- ==================== 运动中 ==================== -->
    <block v-if="phase === 'active'">
      <!-- 全屏地图（仅 GPS 活动） -->
      <view class="map-area full-map" v-if="isGpsActivity">
        <map
          id="runMapActive"
          class="gps-map"
          :latitude="currentLat"
          :longitude="currentLng"
          :scale="17"
          :show-location="true"
          :enable-3D="false"
          :enable-zoom="true"
          :enable-scroll="true"
          :enable-rotate="false"
          :polyline="polylineData"
        />
      </view>

      <!-- GPS 活动数据浮层 -->
      <cover-view class="data-overlay" v-if="isGpsActivity">
        <cover-view class="data-top">
          <cover-view class="goal-tag" v-if="hasGoal">
            <cover-view class="goal-tag-text">{{ goalLabel }}</cover-view>
          </cover-view>
          <cover-view class="gps-indicator" :class="gpsSignalClass">
            <cover-view class="gps-ind-dot"></cover-view>
            <cover-view class="gps-ind-text">{{ gpsAccuracy > 0 ? '±' + Math.round(gpsAccuracy) : '定位中' }}</cover-view>
          </cover-view>
        </cover-view>

        <cover-view class="data-main">
          <cover-view class="data-distance-row">
            <cover-view class="data-distance">{{ currentDistance }}</cover-view>
            <cover-view class="data-dist-unit">km</cover-view>
          </cover-view>
          <cover-view class="data-pace">{{ formatPace(avgPace) }}</cover-view>
          <cover-view class="data-pace-label">平均配速 /km</cover-view>
        </cover-view>

        <cover-view class="data-sub">
          <cover-view class="data-sub-item">
            <cover-view class="data-sub-val">{{ formatDuration(elapsedSeconds) }}</cover-view>
            <cover-view class="data-sub-lbl">时长</cover-view>
          </cover-view>
          <cover-view class="data-sub-item">
            <cover-view class="data-sub-val">{{ currentCalories }}</cover-view>
            <cover-view class="data-sub-lbl">千卡</cover-view>
          </cover-view>
        </cover-view>

        <cover-view class="progress-wrap" v-if="hasGoal">
          <cover-view class="progress-track">
            <cover-view class="progress-fill" :style="{ width: progressPercent + '%' }"></cover-view>
          </cover-view>
        </cover-view>
      </cover-view>

      <!-- 非 GPS 活动数据浮层 -->
      <cover-view class="data-overlay indoor-overlay" v-else>
        <cover-view class="indoor-top">
          <cover-view class="goal-tag" v-if="hasGoal">
            <cover-view class="goal-tag-text">{{ goalLabel }}</cover-view>
          </cover-view>
          <cover-view class="indoor-label">{{ selectedActivity.name }}</cover-view>
        </cover-view>

        <cover-view class="indoor-main">
          <cover-view class="indoor-timer">{{ formatDuration(elapsedSeconds) }}</cover-view>
          <cover-view class="indoor-timer-label">已进行</cover-view>
        </cover-view>

        <cover-view class="data-sub">
          <cover-view class="data-sub-item">
            <cover-view class="data-sub-val">{{ currentCalories }}</cover-view>
            <cover-view class="data-sub-lbl">千卡</cover-view>
          </cover-view>
        </cover-view>

        <cover-view class="progress-wrap" v-if="hasGoal && goalType === 'duration'">
          <cover-view class="progress-track">
            <cover-view class="progress-fill" :style="{ width: progressPercent + '%' }"></cover-view>
          </cover-view>
        </cover-view>
      </cover-view>

      <!-- 底部控制 -->
      <cover-view class="active-ctrl-bar">
        <cover-view class="ctrl-btn" @click="discardRunning">
          <cover-view class="ctrl-icon-wrap lock">
            <cover-view class="ctrl-icon-txt">✕</cover-view>
          </cover-view>
          <cover-view class="ctrl-label">结束</cover-view>
        </cover-view>
        <cover-view class="ctrl-btn" @click="togglePause">
          <cover-view class="ctrl-icon-wrap main">
            <cover-view class="ctrl-icon-txt">{{ isPaused ? '▶' : '||' }}</cover-view>
          </cover-view>
          <cover-view class="ctrl-label">{{ isPaused ? '继续' : '暂停' }}</cover-view>
        </cover-view>
        <cover-view class="ctrl-btn" @click="finishRunning">
          <cover-view class="ctrl-icon-wrap">
            <cover-view class="ctrl-icon-txt">完</cover-view>
          </cover-view>
          <cover-view class="ctrl-label">完成</cover-view>
        </cover-view>
      </cover-view>
    </block>

    <!-- ==================== 总结阶段 ==================== -->
    <block v-if="phase === 'summary'">
      <scroll-view class="summary-scroll" scroll-y>
        <!-- 路线地图（仅 GPS 活动） -->
        <view class="summary-map-area" v-if="isGpsActivity">
          <map
            id="summaryMap"
            class="gps-map"
            :latitude="currentLat"
            :longitude="currentLng"
            :scale="15"
            :show-location="true"
            :enable-3D="false"
            :enable-zoom="false"
            :enable-scroll="false"
            :enable-rotate="false"
            :polyline="polylineData"
          />
        </view>

        <!-- 核心数据 -->
        <view class="summary-card">
          <view class="summary-hero-row">
            <view class="summary-hero-distance" v-if="isGpsActivity">
              <text class="hero-dist-num">{{ summaryData.distance }}</text>
              <text class="hero-dist-unit">km</text>
            </view>
            <view class="summary-hero-timer" v-else>
              <text class="hero-timer-num">{{ formatDuration(summaryData.duration) }}</text>
            </view>
            <text class="summary-hero-date">{{ todayDate }}</text>
          </view>

          <view class="summary-stat-row">
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ formatDuration(summaryData.duration) }}</text>
              <text class="stat-item-label">时长</text>
            </view>
            <view class="summary-stat-item" v-if="isGpsActivity">
              <text class="stat-item-num">{{ formatPace(summaryData.avgPace) }}</text>
              <text class="stat-item-label">配速</text>
            </view>
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ summaryData.calories }}</text>
              <text class="stat-item-label">千卡</text>
            </view>
            <view class="summary-stat-item" v-if="isGpsActivity">
              <text class="stat-item-num">{{ summaryData.avgSpeed }}</text>
              <text class="stat-item-label">km/h</text>
            </view>
          </view>

          <!-- 目标达成 -->
          <view class="summary-goal-banner" v-if="hasGoal">
            <text>{{ goalAchieved ? '目标达成！太棒了！' : '继续加油，下次一定行！' }}</text>
          </view>
        </view>

        <!-- 操作 -->
        <view class="summary-actions">
          <view class="summary-btn primary" @click="shareResult">分享记录</view>
          <view class="summary-btn secondary" @click="goToRecordDetail" v-if="summaryData.recordId">查看记录</view>
          <view class="summary-btn secondary" @click="resetToReady">再来一次</view>
        </view>

        <view class="summary-spacer"></view>
      </scroll-view>
    </block>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { formatDate, formatDuration, formatPace } from '@/utils'
import { startWorkout, finishWorkout, uploadTrajectory } from '@/api/workout'
import { getStatsSummary } from '@/api/stats'

// ===================== 状态 =====================
const statusBarHeight = ref(0)
const screenWidth = ref(750)
const scrollLeft = ref(0)
const phase = ref('ready')
const isPaused = ref(false)
const goalType = ref('distance')
const goalValue = ref(5)
const gpsReady = ref(false)
const gpsAccuracy = ref(0)
const gpsErrorMsg = ref('')
const currentLat = ref(39.9042)
const currentLng = ref(116.4074)

// 运动数据
const elapsedSeconds = ref(0)
const totalDistanceMeters = ref(0)
const currentCalories = ref(0)
const currentSteps = ref(0)
const recordId = ref(null)
const startTimestamp = ref(0)
const pausedDuration = ref({ accumulated: 0, start: 0 })

// GPS 轨迹
const lastLocation = ref(null)
const trajectory = ref([])
const timerInterval = ref(null)

// 统计/总结
const stats = ref({ totalRuns: 0, totalDistance: '0', totalDuration: '0' })
const summaryData = ref({
  distance: '0', duration: 0, avgPace: 0,
  maxPace: 0, maxPaceStr: "0'00\"", calories: 0, steps: 0, avgSpeed: '0'
})

const distanceOptions = [1, 2, 3, 5, 8, 10, 15, 21.1]
const durationOptions = [10, 15, 20, 30, 45, 60, 90, 120]

// 定制跑 - 已创建的跑步活动
const selectedActId = ref(null)
const selectedCustomActivity = ref(null)
const runningActivities = ref([])

// 活动类型选择条
const activityList = [
  { key: 'run-outdoor', name: '跑步', type: 1, needsGps: true },
  { key: 'bike-outdoor', name: '骑行', type: 3, needsGps: true },
  { key: 'hike', name: '徒步', type: 2, needsGps: true },
  { key: 'walk', name: '健步走', type: 2, needsGps: true },
  { key: 'climb', name: '登山', type: 2, needsGps: true },
  { key: 'bike-indoor', name: '骑行台', type: 3, needsGps: false },
  { key: 'swim', name: '游泳', type: 1, needsGps: false },
  { key: 'yoga', name: '瑜伽', type: 1, needsGps: false },
  { key: 'strength', name: '力量', type: 1, needsGps: false },
  { key: 'aerobic', name: '有氧操', type: 1, needsGps: false },
  { key: 'stretch', name: '拉伸', type: 1, needsGps: false }
]
const selectedActivityIndex = ref(0)

const selectedActivity = computed(() => activityList[selectedActivityIndex.value])
const startBtnText = computed(() => '开始' + selectedActivity.value.name)

// ===================== 计算属性 =====================
const isGpsActivity = computed(() => selectedActivity.value.needsGps)
const currentDistance = computed(() => (totalDistanceMeters.value / 1000).toFixed(2))
const avgPace = computed(() => {
  if (totalDistanceMeters.value < 10) return 0
  return Math.round(elapsedSeconds.value / (totalDistanceMeters.value / 1000))
})
const hasGoal = computed(() => goalType.value !== 'none')
const goalLabel = computed(() => {
  if (goalType.value === 'distance') return '目标 ' + goalValue.value + 'km'
  if (goalType.value === 'duration') return '目标 ' + goalValue.value + 'min'
  if (goalType.value === 'custom' && selectedCustomActivity.value) return selectedCustomActivity.value.title
  if (goalType.value === 'custom') return '定制'
  return '自由' + selectedActivity.value.name
})
const progressPercent = computed(() => {
  if (!hasGoal.value || goalType.value === 'custom') return 0
  if (goalType.value === 'distance')
    return Math.min(100, Math.round((totalDistanceMeters.value / (goalValue.value * 1000)) * 100))
  if (goalType.value === 'duration')
    return Math.min(100, Math.round((elapsedSeconds.value / (goalValue.value * 60)) * 100))
  return 0
})
const goalAchieved = computed(() => progressPercent.value >= 100)
const todayDate = computed(() => formatDate(new Date(), 'YYYY年MM月DD日'))
const gpsSignalClass = computed(() => {
  if (gpsAccuracy.value <= 0) return ''
  if (gpsAccuracy.value <= 10) return 'ok'
  if (gpsAccuracy.value <= 30) return 'fair'
  return 'weak'
})

// 地图路线
const polylineData = computed(() => {
  if (trajectory.value.length < 2) return []
  return [{
    points: trajectory.value.map(p => ({ latitude: p.latitude, longitude: p.longitude })),
    color: '#4ADE80',
    width: 4,
    arrowLine: false
  }]
})

// ===================== GPS & 距离 =====================
const EARTH_RADIUS = 6371000
function toRad(d) { return (d * Math.PI) / 180 }
function haversineDistance(lat1, lon1, lat2, lon2) {
  const dLat = toRad(lat2 - lat1), dLon = toRad(lon2 - lon1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2
  return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

// MET 值表（基于活动类型估算卡路里）
const metMap = {
  'run-outdoor': 9.8, 'bike-outdoor': 7.5, 'hike': 6.0,
  'walk': 3.5, 'climb': 8.0, 'bike-indoor': 7.0,
  'swim': 8.0, 'yoga': 2.5, 'strength': 5.0,
  'aerobic': 6.5, 'stretch': 2.0
}
const userWeight = ref(70) // kg，可从本地存储读取

function calcCaloriesByMet(seconds) {
  const met = metMap[selectedActivity.value.key] || 5
  return Math.round(met * userWeight.value * (seconds / 3600))
}

function onLocationChange(res) {
  if (isPaused.value || !res || !res.latitude || !res.longitude) return
  const { latitude, longitude, accuracy, speed } = res
  gpsAccuracy.value = accuracy || 0
  currentLat.value = latitude
  currentLng.value = longitude

  if (lastLocation.value) {
    const dist = haversineDistance(
      lastLocation.value.latitude, lastLocation.value.longitude, latitude, longitude)
    const dt = (Date.now() - lastLocation.value.timestamp) / 1000
    if (dt > 0 && dist / dt < 50) totalDistanceMeters.value += dist
  }
  lastLocation.value = { latitude, longitude, timestamp: Date.now() }
  trajectory.value.push({ latitude, longitude, accuracy, speed, timestamp: Date.now() })
  currentCalories.value = Math.round(70 * (totalDistanceMeters.value / 1000) * 1.036)
  currentSteps.value = Math.round(totalDistanceMeters.value / 0.8)

  if (hasGoal.value && progressPercent.value >= 100 && elapsedSeconds.value > 5) autoFinish()
}

// ===================== 运动控制 =====================
async function startRunning() {
  // 0. 定制跑模式下必须选中活动
  if (goalType.value === 'custom' && !selectedCustomActivity.value) {
    uni.showToast({ title: '请先选择一个定制活动', icon: 'none' })
    return
  }

  // 1. GPS 活动先确保定位已就绪
  if (isGpsActivity.value && !gpsReady.value) {
    uni.showToast({ title: 'GPS未就绪，请稍候', icon: 'none' })
    const granted = await ensureLocationPermission()
    if (!granted) {
      showGpsPermissionGuide()
      return
    }
    gpsReady.value = false
    await initGpsLocation()
    if (!gpsReady.value) {
      uni.showToast({ title: '定位失败，请重试', icon: 'none' })
      return
    }
  }

  isPaused.value = false
  elapsedSeconds.value = 0
  totalDistanceMeters.value = 0
  currentCalories.value = 0
  currentSteps.value = 0
  lastLocation.value = null
  trajectory.value = []
  pausedDuration.value = { accumulated: 0, start: 0 }

  // 2. GPS 活动启动持续定位
  if (isGpsActivity.value) {
    uni.startLocationUpdate({
      type: 'gcj02',
      success: () => { gpsReady.value = true },
      fail: (err) => {
        console.error('startLocationUpdate fail:', err)
        // #ifdef MP-WEIXIN
        showGpsPermissionGuide()
        // #endif
        // #ifndef MP-WEIXIN
        uni.showModal({
          title: 'GPS 不可用',
          content: '请在系统设置中开启定位权限',
          confirmText: '去设置',
          cancelText: '取消',
          success: r => {
            if (r.confirm) {
              // #ifdef APP-PLUS
              plus.runtime.openURL('app-settings:')
              // #endif
            }
          }
        })
        // #endif
      }
    })
    uni.onLocationChange(onLocationChange)
  }

  try {
    const res = await startWorkout({ type: selectedActivity.value.type, activityName: selectedActivity.value.name })
    if (res.code === 200 && res.data) recordId.value = res.data.recordId
  } catch (e) { /* offline */ }

  phase.value = 'active'
  startTimestamp.value = Date.now()

  timerInterval.value = setInterval(() => {
    if (isPaused.value) return
    const now = Date.now()
    const acc = typeof pausedDuration.value.accumulated === 'number' ? pausedDuration.value.accumulated : 0
    elapsedSeconds.value = Math.floor((now - startTimestamp.value - acc) / 1000)
    // 非 GPS 活动：用 MET 值实时估算卡路里
    if (!isGpsActivity.value) {
      currentCalories.value = calcCaloriesByMet(elapsedSeconds.value)
    }
    if (trajectory.value.length >= 10 && recordId.value) uploadTrajectoryToServer()
  }, 1000)
}

function togglePause() {
  isPaused.value = !isPaused.value
  if (isPaused.value) {
    pausedDuration.value = { accumulated: pausedDuration.value.accumulated || 0, start: Date.now() }
  } else {
    if (pausedDuration.value.start) {
      pausedDuration.value.accumulated = (pausedDuration.value.accumulated || 0) + (Date.now() - pausedDuration.value.start)
    }
  }
}

let lastTrajUpload = 0
function uploadTrajectoryToServer() {
  const now = Date.now()
  if (now - lastTrajUpload < 10000 || !recordId.value) return
  lastTrajUpload = now
  const pts = trajectory.value.slice(-15)
  if (!pts.length) return
  uploadTrajectory({ recordId: recordId.value, trajectory: pts.map(p => ({ latitude: p.latitude, longitude: p.longitude, accuracy: p.accuracy || 0, speed: p.speed || 0, timestamp: p.timestamp })) }).catch(() => {})
}

function autoFinish() { setTimeout(() => finishRunning(), 1500) }

async function finishRunning() {
  if (isGpsActivity.value) {
    uni.offLocationChange(onLocationChange)
    uni.stopLocationUpdate()
  }
  if (timerInterval.value) { clearInterval(timerInterval.value); timerInterval.value = null }

  if (recordId.value && trajectory.value.length > 0) {
    try { await uploadTrajectory({ recordId: recordId.value, trajectory: trajectory.value.map(p => ({ latitude: p.latitude, longitude: p.longitude, accuracy: p.accuracy || 0, speed: p.speed || 0, timestamp: p.timestamp })) }) } catch (e) {}
  }

  const d = elapsedSeconds.value
  const dist = (totalDistanceMeters.value / 1000).toFixed(2)
  const p = avgPace.value
  const spd = totalDistanceMeters.value > 0 ? ((totalDistanceMeters.value / 1000) / (d / 3600)).toFixed(1) : '0'

  if (isGpsActivity.value) {
    summaryData.value = { distance: dist, duration: d, avgPace: p, maxPace: p, maxPaceStr: formatPace(p), calories: currentCalories.value, steps: currentSteps.value, avgSpeed: spd, recordId: recordId.value }
  } else {
    summaryData.value = { distance: '0.00', duration: d, avgPace: 0, maxPace: 0, maxPaceStr: '--', calories: currentCalories.value, steps: 0, avgSpeed: '0', recordId: recordId.value }
  }

  if (recordId.value) { try { await finishWorkout(recordId.value) } catch (e) {} }
  phase.value = 'summary'
}

function discardRunning() {
  uni.showModal({
    title: '确认放弃', content: '放弃后本次数据不会保存', confirmText: '确定放弃', cancelText: '继续', confirmColor: '#ef4444',
    success: r => {
      if (!r.confirm) return
      uni.offLocationChange(onLocationChange); uni.stopLocationUpdate()
      if (timerInterval.value) { clearInterval(timerInterval.value); timerInterval.value = null }
      recordId.value = null; trajectory.value = []
      phase.value = 'ready'
    }
  })
}

function goToRecordDetail() {
  const id = summaryData.value.recordId
  if (id) {
    uni.navigateTo({ url: `/pages/workout/detail?id=${id}` })
  }
}

function resetToReady() {
  totalDistanceMeters.value = 0; elapsedSeconds.value = 0
  currentCalories.value = 0; currentSteps.value = 0
  trajectory.value = []; lastLocation.value = null
  phase.value = 'ready'
}

function shareResult() {
  uni.share({
    provider: 'weixin',
    title: `${selectedActivity.value.name}完成！${summaryData.value.distance}km`,
    content: `配速 ${formatPace(summaryData.value.avgPace)}，消耗 ${summaryData.value.calories} 千卡`,
    success: () => uni.showToast({ title: '分享成功', icon: 'success' }),
    fail: () => {
      uni.setClipboardData({ data: `${selectedActivity.value.name}完成！${summaryData.value.distance}km，配速 ${formatPace(summaryData.value.avgPace)}` })
      uni.showToast({ title: '已复制', icon: 'none' })
    }
  })
}

async function loadStats() {
  try {
    const res = await getStatsSummary()
    if (res.code === 200 && res.data) {
      stats.value = { totalRuns: res.data.monthlyWorkouts || 0, totalDistance: res.data.totalDistanceStr || '0', totalDuration: Math.round((res.data.totalDuration || 0) / 60).toString() }
    }
  } catch (e) {}
}

function loadCustomActivities() {
  try {
    const raw = uni.getStorageSync('userActivities')
    const list = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
    runningActivities.value = (list || [])
      .filter(a => a.type === 'run' || a.type === '跑步')
      .map(a => ({
        ...a,
        warmupCount: (a.warmup || []).length,
        mainCount: (a.main || []).length,
        repeatCount: (a.repeats || a.repeatGroups || []).length
      }))
  } catch (e) {
    runningActivities.value = []
  }
}

function selectCustomActivity(act) {
  selectedActId.value = act.id
  selectedCustomActivity.value = act
  goalValue.value = act.id
}

function selectActivity(index) {
  selectedActivityIndex.value = index
  // 160rpx 是每项宽度，将 rpx 转换为 px
  scrollLeft.value = Math.round(index * 160 * screenWidth.value / 750)
}

function initGpsLocation() {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    // 微信小程序：先检查授权状态
    wx.getSetting({
      success: (settingRes) => {
        const auth = settingRes.authSetting
        if (auth['scope.userLocation'] === false) {
          // 用户之前拒绝过定位权限
          gpsErrorMsg.value = '定位权限已拒绝，请在设置中开启'
          resolve(false)
        } else if (auth['scope.userLocation'] === true || auth['scope.userLocation'] === undefined) {
          // 已授权或未询问过
          wx.getLocation({
            type: 'gcj02',
            success: (res) => {
              gpsReady.value = true
              gpsAccuracy.value = res.accuracy || 0
              currentLat.value = res.latitude
              currentLng.value = res.longitude
              gpsErrorMsg.value = ''
              resolve(true)
            },
            fail: (err) => {
              console.error('wx.getLocation fail:', err)
              const errMsg = err.errMsg || ''
              if (errMsg.includes('auth deny') || errMsg.includes('authorize')) {
                gpsErrorMsg.value = '需要定位权限才能记录运动轨迹'
              } else {
                gpsErrorMsg.value = '获取位置失败: ' + (errMsg || '未知')
              }
              resolve(false)
            }
          })
        }
      },
      fail: () => resolve(false)
    })
    // #endif

    // #ifndef MP-WEIXIN
    // App/H5：先尝试gcj02坐标系统
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        gpsReady.value = true
        gpsAccuracy.value = res.accuracy || 0
        currentLat.value = res.latitude
        currentLng.value = res.longitude
        gpsErrorMsg.value = ''
        resolve(true)
      },
      fail: (err) => {
        console.error('getLocation(gcj02) fail:', err)
        const errMsg = typeof err === 'string' ? err : (err.errMsg || '')
        // gcj02失败（常见于H5未配置地图服务商），回退到wgs84
        if (errMsg.includes('translate coordinate') || errMsg.includes('map provider')) {
          console.warn('gcj02 not available, falling back to wgs84')
          uni.getLocation({
            type: 'wgs84',
            success: (res) => {
              gpsReady.value = true
              gpsAccuracy.value = res.accuracy || 0
              currentLat.value = res.latitude
              currentLng.value = res.longitude
              gpsErrorMsg.value = ''
              resolve(true)
            },
            fail: (retryErr) => {
              console.error('getLocation(wgs84) fail:', retryErr)
              gpsErrorMsg.value = '定位失败，请检查GPS开关和权限'
              setTimeout(() => {
                uni.getLocation({
                  type: 'wgs84',
                  success: (res) => {
                    gpsReady.value = true
                    gpsAccuracy.value = res.accuracy || 0
                    currentLat.value = res.latitude
                    currentLng.value = res.longitude
                    gpsErrorMsg.value = ''
                    resolve(true)
                  },
                  fail: () => resolve(false)
                })
              }, 1500)
            }
          })
        } else {
          gpsErrorMsg.value = '定位失败，请检查GPS开关和权限'
          setTimeout(() => {
            uni.getLocation({
              type: 'gcj02',
              success: (res) => {
                gpsReady.value = true
                gpsAccuracy.value = res.accuracy || 0
                currentLat.value = res.latitude
                currentLng.value = res.longitude
                gpsErrorMsg.value = ''
                resolve(true)
              },
              fail: () => resolve(false)
            })
          }, 1500)
        }
      }
    })
    // #endif
  })
}

// 确保位置权限已授权
async function ensureLocationPermission() {
  // #ifdef MP-WEIXIN
  return new Promise((resolve) => {
    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userLocation'] === false) {
          // 用户拒绝过，调用 wx.openSetting 引导开启
          wx.showModal({
            title: '需要定位权限',
            content: '请在设置页面中开启"位置信息"权限，用于记录运动轨迹',
            confirmText: '去设置',
            success: (r) => {
              if (r.confirm) {
                wx.openSetting({
                  success: (settingRes) => {
                    resolve(settingRes.authSetting['scope.userLocation'] === true)
                  },
                  fail: () => resolve(false)
                })
              } else {
                resolve(false)
              }
            }
          })
        } else {
          // 未询问或已授权，尝试获取一次位置触发授权弹窗
          wx.getLocation({
            type: 'gcj02',
            success: () => resolve(true),
            fail: () => resolve(false)
          })
        }
      },
      fail: () => resolve(false)
    })
  })
  // #endif

  // #ifndef MP-WEIXIN
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: () => resolve(true),
      fail: (err) => {
        const errMsg = typeof err === 'string' ? err : (err.errMsg || '')
        // gcj02失败（H5未配置地图服务商），回退到wgs84
        if (errMsg.includes('translate coordinate') || errMsg.includes('map provider')) {
          uni.getLocation({
            type: 'wgs84',
            success: () => resolve(true),
            fail: () => resolve(false)
          })
        } else {
          resolve(false)
        }
      }
    })
  })
  // #endif
}

function showGpsPermissionGuide() {
  // #ifdef MP-WEIXIN
  wx.showModal({
    title: '定位未授权',
    content: '请在右上角设置中开启"位置信息"，用于记录运动轨迹。\n操作：右上角 ··· → 设置 → 位置信息 → 开启',
    showCancel: false,
    confirmText: '知道了'
  })
  // #endif
  // #ifndef MP-WEIXIN
  uni.showModal({
    title: 'GPS未就绪',
    content: '请在系统设置中为应用开启定位权限，然后重试',
    confirmText: '去设置',
    cancelText: '取消',
    success: (r) => {
      if (r.confirm) {
        // #ifdef APP-PLUS
        plus.runtime.openURL('app-settings:')
        // #endif
      }
    }
  })
  // #endif
}

async function retryGps() {
  gpsErrorMsg.value = ''
  gpsReady.value = false
  const granted = await ensureLocationPermission()
  if (granted) {
    await initGpsLocation()
  }
}

onMounted(async () => {
  const sys = uni.getSystemInfoSync()
  statusBarHeight.value = sys.statusBarHeight || 0
  screenWidth.value = sys.screenWidth || 750
  loadStats()
  loadCustomActivities()
  await initGpsLocation()
})
onShow(() => { loadCustomActivities() })
onUnmounted(() => {
  uni.offLocationChange(onLocationChange); uni.stopLocationUpdate()
  if (timerInterval.value) { clearInterval(timerInterval.value); timerInterval.value = null }
})
</script>

<style lang="scss" scoped>
.container { height: 100vh; overflow: hidden; background: #f5f5f5; display: flex; flex-direction: column; }

/* ==================== 活动选择条 ==================== */
.activity-bar {
  position: relative;
  background: linear-gradient(to bottom, rgba(255,255,255,0.85) 0%, rgba(255,255,255,0.4) 70%, transparent 100%);
  flex-shrink: 0;
}

.activity-bar-inner {
  position: relative;
  height: 88rpx;
  display: flex;
  align-items: center;
}

.activity-center-bg {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 150rpx;
  height: 56rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 28rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.06);
  pointer-events: none;
}

.activity-scroll {
  white-space: nowrap;
  width: 100%;
}

.activity-spacer {
  display: inline-block;
  width: 295rpx;
  height: 1rpx;
}

.activity-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 160rpx;
  height: 72rpx;
  vertical-align: middle;
  opacity: 0.25;
  transition: all 0.25s ease;

  &.active {
    opacity: 1;
    transform: scale(1.1);

    .act-name {
      color: #16a34a;
      font-weight: 700;
      font-size: 30rpx;
    }
  }
}

.act-name {
  font-size: 26rpx;
  color: #333;
  transition: all 0.25s ease;
}

/* ==================== 地图区域 ==================== */
.map-area {
  width: 100%;
  flex: 1;
  position: relative;
  overflow: hidden;
}

.map-area.full-map {
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 0;
}

.gps-map {
  width: 100%;
  height: 100%;
}

/* GPS 精度浮层 */
.gps-float {
  position: absolute;
  top: 60rpx;
  left: 30rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  padding: 10rpx 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.gps-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #fbbf24;
  &.ok { background: #22c55e; }
  &.fair { background: #fbbf24; }
  &.weak { background: #ef4444; }
}

.gps-float-text { font-size: 22rpx; color: #555; }

.gps-retry {
  position: absolute;
  top: 110rpx;
  left: 30rpx;
  background: #fff;
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #22c55e;
  font-weight: 600;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.08);
  z-index: 10;
}

/* 非 GPS 活动说明卡片 */
.activity-info-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  margin: 0 28rpx 20rpx;
  border-radius: 24rpx;
  gap: 12rpx;
}

.activity-info-name {
  font-size: 48rpx;
  font-weight: 700;
  color: #1c1c1e;
}

.activity-info-desc {
  font-size: 26rpx;
  color: #94a3b8;
}

/* ==================== 底部操作卡片 ==================== */
.bottom-card {
  height: 42vh;
  min-height: 280rpx;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  margin-top: -24rpx;
  z-index: 20;
  position: relative;
  padding: 32rpx 36rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
  transition: height 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.bottom-card.compact {
  height: 220rpx;
}

/* GPS 选项折叠动画 */
.gps-options-wrap {
  max-height: 600rpx;
  opacity: 1;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.gps-options-wrap.collapsed {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
  margin-bottom: 0;
}

/* 模式切换 */
.mode-tabs {
  display: flex;
  gap: 20rpx;
}

.mode-tab {
  flex: 1;
  padding: 20rpx 0;
  border-radius: 16rpx;
  background: #f5f5f5;
  text-align: center;
  transition: all 0.2s;

  &.active {
    background: #f0fdf4;
    border: 2rpx solid #22c55e;
  }
}

.mode-tab-text { font-size: 28rpx; font-weight: 600; color: #333; display: block; }
.mode-tab-desc { font-size: 22rpx; color: #999; margin-top: 4rpx; display: block; }

.mode-tab.active .mode-tab-text { color: #16a34a; }
.mode-tab.active .mode-tab-desc { color: #22c55e; }

/* 子选项 */
.sub-options { margin-top: 24rpx; }

.sub-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.sub-tab {
  padding: 10rpx 28rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
  background: #f5f5f5;

  &.active { background: #22c55e; color: #fff; font-weight: 600; }
}

.picker-scroll { width: 100%; }

.picker-row {
  display: flex;
  gap: 16rpx;
  padding: 4rpx 0;
  white-space: nowrap;
}

.picker-chip {
  flex-shrink: 0;
  padding: 20rpx 32rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  text-align: center;
  min-width: 100rpx;
  border: 2rpx solid transparent;

  &.selected {
    background: #f0fdf4;
    border-color: #22c55e;
  }
}

.chip-num { font-size: 32rpx; font-weight: 700; color: #333; display: block; }
.chip-unit { font-size: 20rpx; color: #999; margin-top: 4rpx; display: block; }

.picker-chip.selected .chip-num { color: #16a34a; }
.picker-chip.selected .chip-unit { color: #22c55e; }

/* 定制跑活动列表 */
.custom-scroll {
  max-height: 220rpx;
}

.custom-act-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 10rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  &.selected {
    background: #f0fdf4;
    border-color: #22c55e;
  }

  &:active { background: #f0fdf4; }
}

.custom-act-left {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: #f0fdf4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-act-icon { font-size: 28rpx; }

.custom-act-info {
  flex: 1;
  overflow: hidden;
}

.custom-act-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.custom-act-meta {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.custom-act-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #22c55e;
  color: #fff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.custom-act-empty {
  text-align: center;
  padding: 30rpx 0;
}

.empty-icon { font-size: 40rpx; display: block; }

.empty-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
  display: block;
}

.empty-hint {
  font-size: 20rpx;
  color: #bbb;
  margin-top: 6rpx;
  display: block;
}

/* 开始按钮 - Keep 风格绿色 */
.start-run-btn {
  margin-top: auto;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  background: #22c55e;
  border-radius: 48rpx;
  box-shadow: 0 6rpx 20rpx rgba(34, 197, 94, 0.35);
}

.start-run-text { font-size: 32rpx; font-weight: 700; color: #fff; letter-spacing: 4rpx; }

.start-run-btn:active { opacity: 0.85; transform: scale(0.98); }

/* ==================== 运动中浮层 ==================== */
.data-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 60rpx 40rpx 0;
  pointer-events: none;
}

.data-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.goal-tag {
  background: rgba(0, 0, 0, 0.35);
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.goal-tag-text { font-size: 22rpx; color: #fff; }

.gps-indicator {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: rgba(0, 0, 0, 0.35);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.gps-ind-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #fbbf24;
}

.gps-indicator.ok .gps-ind-dot { background: #22c55e; }
.gps-indicator.fair .gps-ind-dot { background: #fbbf24; }
.gps-indicator.weak .gps-ind-dot { background: #ef4444; }

.gps-ind-text { font-size: 20rpx; color: rgba(255, 255, 255, 0.8); }

.data-main { text-align: center; margin-bottom: 50rpx; }

.data-distance-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10rpx;
}

.data-distance { font-size: 140rpx; font-weight: 300; color: #fff; letter-spacing: -4rpx; line-height: 1; }
.data-dist-unit { font-size: 36rpx; color: rgba(255, 255, 255, 0.6); }
.data-pace { font-size: 44rpx; font-weight: 500; color: #fff; }
.data-pace-label { font-size: 24rpx; color: rgba(255, 255, 255, 0.5); margin-top: 6rpx; }

.data-sub {
  display: flex;
  justify-content: center;
  gap: 80rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.data-sub-val { font-size: 28rpx; font-weight: 600; color: #fff; }
.data-sub-lbl { font-size: 22rpx; color: rgba(255, 255, 255, 0.45); margin-top: 4rpx; }

/* 非 GPS 活动运动中浮层 */
.indoor-overlay {
  background: linear-gradient(180deg, rgba(0,0,0,0.5) 0%, rgba(0,0,0,0) 40%);
}

.indoor-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.indoor-label {
  background: rgba(0, 0, 0, 0.35);
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #fff;
}

.indoor-main {
  text-align: center;
  margin-bottom: 50rpx;
}

.indoor-timer {
  font-size: 120rpx;
  font-weight: 300;
  color: #fff;
  letter-spacing: -2rpx;
  line-height: 1;
}

.indoor-timer-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 10rpx;
}

.progress-wrap {
  width: 100%;
  padding: 0 20rpx;
  margin-top: 10rpx;
}

.progress-track {
  width: 100%;
  height: 6rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #22c55e;
  border-radius: 999rpx;
  transition: width 0.5s;
}

/* 底部控制栏 */
.active-ctrl-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 30rpx 40rpx calc(40rpx + env(safe-area-inset-bottom));
  z-index: 10;
}

.ctrl-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.ctrl-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;

  &.main {
    width: 100rpx;
    height: 100rpx;
    background: #fff;
  }

  &.lock { border: 4rpx solid rgba(255, 255, 255, 0.3); }
}

.ctrl-icon-txt { font-size: 30rpx; }

.ctrl-icon-wrap.main .ctrl-icon-txt { color: #22c55e; font-size: 32rpx; }

.ctrl-label { font-size: 20rpx; color: rgba(255, 255, 255, 0.7); }

.ctrl-icon-wrap.lock .ctrl-icon-txt { color: rgba(255, 255, 255, 0.8); font-size: 26rpx; }

/* ==================== 总结阶段 ==================== */
.summary-scroll { height: 100vh; background: #f5f5f5; }

.summary-map-area {
  width: 100%;
  height: 40vh;
}

.summary-card {
  background: #fff;
  margin: -24rpx 20rpx 0;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;
  position: relative;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.summary-hero-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32rpx;
}

.summary-hero-distance {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.hero-dist-num { font-size: 56rpx; font-weight: 300; color: #111; letter-spacing: -2rpx; }
.hero-dist-unit { font-size: 28rpx; color: #999; }

.summary-hero-date { font-size: 24rpx; color: #999; }

.summary-hero-timer {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.hero-timer-num {
  font-size: 56rpx;
  font-weight: 300;
  color: #111;
  letter-spacing: -2rpx;
}

.summary-stat-row {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  border-top: 1rpx solid #f0f0f0;
  border-bottom: 1rpx solid #f0f0f0;
}

.summary-stat-item { text-align: center; flex: 1; }
.stat-item-num { font-size: 36rpx; font-weight: 600; color: #333; display: block; }
.stat-item-label { font-size: 22rpx; color: #999; margin-top: 4rpx; display: block; }

.summary-goal-banner {
  margin-top: 24rpx;
  padding: 20rpx;
  background: #f0fdf4;
  border-radius: 12rpx;
  text-align: center;
  font-size: 26rpx;
  color: #16a34a;
  font-weight: 500;
}

.summary-actions {
  padding: 32rpx 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.summary-btn {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 44rpx;
  font-size: 28rpx; font-weight: 600;

  &.primary { background: #22c55e; color: #fff; box-shadow: 0 4rpx 16rpx rgba(34, 197, 94, 0.3); }
  &.secondary { background: #f5f5f5; color: #666; }

  &:active { opacity: 0.85; }
}

.summary-spacer { height: 40rpx; }
</style>
