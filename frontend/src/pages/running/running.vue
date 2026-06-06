<template>
  <view class="container">
    <!-- ==================== 准备阶段 ==================== -->
    <block v-if="phase === 'ready'">
      <view class="activity-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="activity-bar-inner">
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
              <view class="act-icon-shell">
                <AppIcon :name="act.icon" size="30" />
              </view>
              <view class="act-copy">
                <text class="act-name">{{ act.name }}</text>
                <text class="act-meta">{{ act.meta }}</text>
              </view>
            </view>
            <view class="activity-spacer"></view>
          </scroll-view>
        </view>
      </view>
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
        <text class="activity-info-desc">{{ indoorHeroDesc }}</text>
      </view>
      <view class="bottom-card">
        <scroll-view class="gps-options-wrap" scroll-y :show-scrollbar="false">
          <view class="mode-tabs">
            <view
              class="mode-tab"
              :class="{ active: goalType === 'none' }"
              @click="switchGoalType('none')"
            >
              <text class="mode-tab-text">{{ freeModeTitle }}</text>
              <text class="mode-tab-desc">{{ freeModeDesc }}</text>
            </view>
            <view
              class="mode-tab"
              :class="{ active: goalType !== 'none' }"
              @click="enableTargetMode"
            >
              <text class="mode-tab-text">{{ targetModeTitle }}</text>
              <text class="mode-tab-desc">{{ targetModeDesc }}</text>
            </view>
          </view>
          <view class="goal-summary" v-if="goalType !== 'none'">
            <view class="goal-summary-left">
              <text class="goal-summary-kicker">{{ goalType === 'distance' ? '目标距离' : goalType === 'duration' ? '目标时长' : '定制训练' }}</text>
              <view class="goal-summary-main" @click="openGoalInput">
                <text class="goal-summary-value">{{ goalSummaryValue }}</text>
                <text class="goal-summary-unit">{{ goalSummaryUnit }}</text>
              </view>
              <text class="goal-summary-desc">{{ goalSummaryDesc }}</text>
            </view>
            <view class="goal-summary-icon">
              <AppIcon :name="goalType === 'duration' ? 'time' : goalType === 'custom' ? 'plan' : 'target'" size="42" />
            </view>
          </view>
          <view class="sub-options" v-if="goalType !== 'none'">
            <view class="sub-tabs">
              <view
                class="sub-tab"
                :class="{ active: goalType === 'distance' }"
                @click="switchGoalType('distance')"
                v-if="isGpsActivity"
              >距离</view>
              <view
                class="sub-tab"
                :class="{ active: goalType === 'duration' }"
                @click="switchGoalType('duration')"
              >计时</view>
              <view
                class="sub-tab"
                :class="{ active: goalType === 'custom' }"
                @click="switchGoalType('custom')"
              >计划</view>
            </view>
            <view class="goal-wheel-card" v-if="goalType === 'distance'">
              <view class="goal-wheel-edit" @click="openGoalInput">
                <text class="goal-wheel-edit-value">{{ goalValue }}</text>
                <text class="goal-wheel-edit-unit">km</text>
              </view>
              <picker-view
                class="goal-picker"
                :value="distancePickerValue"
                indicator-class="goal-picker-indicator"
                @change="onDistancePickerChange"
              >
                <picker-view-column>
                  <view class="picker-option" v-for="n in distanceIntegerOptions" :key="'km-' + n">
                    <text>{{ n }}</text>
                  </view>
                </picker-view-column>
                <picker-view-column>
                  <view class="picker-option" v-for="n in distanceDecimalOptions" :key="'decimal-' + n">
                    <text>{{ n }}</text>
                  </view>
                </picker-view-column>
              </picker-view>
              <text class="goal-picker-dot">.</text>
              <text class="goal-wheel-unit">公里</text>
            </view>
            <view class="goal-wheel-card" v-if="goalType === 'duration'">
              <view class="goal-wheel-edit" @click="openGoalInput">
                <text class="goal-wheel-edit-value">{{ goalValue }}</text>
                <text class="goal-wheel-edit-unit">min</text>
              </view>
              <picker-view
                class="goal-picker"
                :value="durationPickerValue"
                indicator-class="goal-picker-indicator"
                @change="onDurationPickerChange"
              >
                <picker-view-column>
                  <view class="picker-option" v-for="n in durationHourOptions" :key="'hour-' + n">
                    <text>{{ n }}</text>
                  </view>
                </picker-view-column>
                <picker-view-column>
                  <view class="picker-option" v-for="n in durationMinuteOptions" :key="'minute-' + n">
                    <text>{{ String(n).padStart(2, '0') }}</text>
                  </view>
                </picker-view-column>
              </picker-view>
              <text class="goal-wheel-unit">小时 / 分钟</text>
            </view>
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
                <AppIcon class="custom-act-check" v-if="selectedActId === act.id" name="check" size="24" />
              </view>
              <view class="custom-act-empty" v-if="runningActivities.length === 0">
                <text class="empty-icon">无</text>
                <text class="empty-text">暂无定制活动</text>
                <text class="empty-hint">去「定制」页面创建一个吧</text>
              </view>
            </scroll-view>
          </view>
        </scroll-view>
        <view class="start-run-btn" @click="startRunning">
          <text class="start-run-text">{{ startBtnText }}</text>
        </view>
      </view>
      <view class="goal-input-mask" v-if="showGoalInput" @click="closeGoalInput">
        <view class="goal-input-card" @click.stop>
          <text class="goal-input-title">{{ goalType === 'duration' ? '输入计时时长' : '输入目标公里数' }}</text>
          <text class="goal-input-sub">{{ goalType === 'duration' ? '单位：分钟' : '单位：公里，支持一位小数' }}</text>
          <input
            class="goal-input"
            type="digit"
            v-model="goalInputValue"
            :placeholder="goalType === 'duration' ? '例如 45' : '例如 5.5'"
            focus
          />
          <view class="goal-input-actions">
            <view class="goal-input-btn ghost" @click="closeGoalInput">取消</view>
            <view class="goal-input-btn primary" @click="confirmGoalInput">确定</view>
          </view>
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
      <!-- 非 GPS 活动背景 -->
      <view class="indoor-bg" v-else>
        <view class="indoor-bg-circle c1"></view>
        <view class="indoor-bg-circle c2"></view>
        <view class="indoor-bg-circle c3"></view>
      </view>
      <!-- 锁定遮罩 -->
      <view class="lock-overlay" v-if="isLocked" @click="unlockScreen">
        <view class="lock-hint">
          <AppIcon class="lock-hint-icon" name="lock" size="30" />
          <text class="lock-hint-text">屏幕已锁定</text>
          <text class="lock-hint-sub">点击解锁</text>
        </view>
      </view>
      <!-- GPS 活动数据浮层 -->
      <cover-view class="data-overlay" :class="{ 'overlay-paused': isPaused }" v-if="isGpsActivity && !isLocked">
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
          <cover-view class="data-pace-row">
            <cover-view class="data-pace">{{ formatPace(currentPace) }}</cover-view>
            <cover-view class="data-pace-label">当前配速</cover-view>
          </cover-view>
        </cover-view>
      </cover-view>
      <!-- 底部悬浮面板（GPS活动） -->
      <cover-view class="bottom-float-panel" v-if="isGpsActivity && !isLocked">
        <cover-view class="panel-progress" v-if="hasGoal">
          <cover-view class="panel-progress-track">
            <cover-view class="panel-progress-fill" :style="{ width: progressPercent + '%' }"></cover-view>
          </cover-view>
          <cover-view class="panel-progress-text" v-if="goalType === 'distance'">
            <cover-view>剩余 {{ remainingDistance }} km</cover-view>
          </cover-view>
          <cover-view class="panel-progress-text" v-else-if="goalType === 'duration'">
            <cover-view>剩余 {{ formatDuration(remainingDuration) }}</cover-view>
          </cover-view>
        </cover-view>
        <cover-view class="panel-data-grid">
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ formatDuration(elapsedSeconds) }}</cover-view>
            <cover-view class="panel-data-lbl">时长</cover-view>
          </cover-view>
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ formatPace(avgPace) }}</cover-view>
            <cover-view class="panel-data-lbl">平均配速</cover-view>
          </cover-view>
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ currentCalories }}</cover-view>
            <cover-view class="panel-data-lbl">千卡</cover-view>
          </cover-view>
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ currentCadence }}</cover-view>
            <cover-view class="panel-data-lbl">步频</cover-view>
          </cover-view>
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ currentSteps }}</cover-view>
            <cover-view class="panel-data-lbl">步数</cover-view>
          </cover-view>
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ Math.round(elevationGain) }}</cover-view>
            <cover-view class="panel-data-lbl">爬升(m)</cover-view>
          </cover-view>
        </cover-view>
        <cover-view class="panel-btn-row">
          <cover-view class="panel-btn" @click="discardRunning">
            <cover-view class="panel-btn-circle end">
              <cover-view class="panel-btn-icon">关闭</cover-view>
            </cover-view>
            <cover-view class="panel-btn-label">结束</cover-view>
          </cover-view>
          <cover-view class="panel-btn" @click="goNextItem">
            <cover-view class="panel-btn-circle next">
              <cover-view class="panel-btn-icon">下项</cover-view>
            </cover-view>
            <cover-view class="panel-btn-label">下一项</cover-view>
          </cover-view>
          <cover-view class="panel-btn main" @click="togglePause">
            <cover-view class="panel-btn-circle main">
              <cover-view class="panel-btn-icon">{{ isPaused ? '继续' : '暂停' }}</cover-view>
            </cover-view>
            <cover-view class="panel-btn-label">{{ isPaused ? '继续' : '暂停' }}</cover-view>
          </cover-view>
        </cover-view>
      </cover-view>
      <!-- 非 GPS 活动数据浮层 -->
      <cover-view class="data-overlay indoor-overlay" :class="{ 'overlay-paused': isPaused }" v-if="!isGpsActivity && !isLocked">
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
      </cover-view>
      <!-- 底部悬浮面板（非GPS活动） -->
      <cover-view class="bottom-float-panel indoor-panel" v-if="!isGpsActivity && !isLocked">
        <cover-view class="panel-progress" v-if="hasGoal && goalType === 'duration'">
          <cover-view class="panel-progress-track">
            <cover-view class="panel-progress-fill" :style="{ width: progressPercent + '%' }"></cover-view>
          </cover-view>
          <cover-view class="panel-progress-text">
            <cover-view>剩余 {{ formatDuration(remainingDuration) }}</cover-view>
          </cover-view>
        </cover-view>
        <cover-view class="panel-data-grid indoor-data-grid">
          <cover-view class="panel-data-item">
            <cover-view class="panel-data-val">{{ currentCalories }}</cover-view>
            <cover-view class="panel-data-lbl">千卡</cover-view>
          </cover-view>
          <cover-view class="panel-data-item" v-if="goalType === 'duration'">
            <cover-view class="panel-data-val">{{ formatDuration(remainingDuration) }}</cover-view>
            <cover-view class="panel-data-lbl">剩余</cover-view>
          </cover-view>
          <cover-view class="panel-data-item" v-else>
            <cover-view class="panel-data-val">{{ formatDuration(elapsedSeconds) }}</cover-view>
            <cover-view class="panel-data-lbl">已进行</cover-view>
          </cover-view>
        </cover-view>
        <cover-view class="panel-btn-row">
          <cover-view class="panel-btn" @click="discardRunning">
            <cover-view class="panel-btn-circle end">
              <cover-view class="panel-btn-icon">关闭</cover-view>
            </cover-view>
            <cover-view class="panel-btn-label">结束</cover-view>
          </cover-view>
          <cover-view class="panel-btn" @click="goNextItem">
            <cover-view class="panel-btn-circle next">
              <cover-view class="panel-btn-icon">下项</cover-view>
            </cover-view>
            <cover-view class="panel-btn-label">下一项</cover-view>
          </cover-view>
          <cover-view class="panel-btn main" @click="togglePause">
            <cover-view class="panel-btn-circle main">
              <cover-view class="panel-btn-icon">{{ isPaused ? '继续' : '暂停' }}</cover-view>
            </cover-view>
            <cover-view class="panel-btn-label">{{ isPaused ? '继续' : '暂停' }}</cover-view>
          </cover-view>
        </cover-view>
      </cover-view>
      <!-- 暂停浮层 -->
      <view class="pause-overlay" v-if="isPaused && !isLocked">
        <view class="pause-hint">
          <text class="pause-hint-text">运动已暂停</text>
          <text class="pause-hint-duration">{{ formatDuration(pausedElapsedSeconds) }}</text>
        </view>
      </view>
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
          <view class="summary-map-badge">
            <text class="map-badge-text">{{ selectedActivity.name }}</text>
          </view>
        </view>
        <!-- 非 GPS 活动顶部 -->
        <view class="summary-indoor-header" v-else>
          <text class="summary-indoor-name">{{ selectedActivity.name }}</text>
          <text class="summary-indoor-date">{{ todayDate }}</text>
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
            <text class="summary-hero-date" v-if="isGpsActivity">{{ todayDate }}</text>
          </view>
          <view class="summary-stat-row">
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ formatDuration(summaryData.duration) }}</text>
              <text class="stat-item-label">时长</text>
            </view>
            <view class="summary-stat-item" v-if="isGpsActivity">
              <text class="stat-item-num">{{ formatPace(summaryData.avgPace) }}</text>
              <text class="stat-item-label">平均配速</text>
            </view>
            <view class="summary-stat-item" v-if="isGpsActivity">
              <text class="stat-item-num">{{ formatPace(summaryData.bestPace) }}</text>
              <text class="stat-item-label">最佳配速</text>
            </view>
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ summaryData.calories }}</text>
              <text class="stat-item-label">千卡</text>
            </view>
          </view>
          <view class="summary-stat-row second-row" v-if="isGpsActivity">
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ summaryData.avgSpeed }}</text>
              <text class="stat-item-label">km/h</text>
            </view>
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ summaryData.steps }}</text>
              <text class="stat-item-label">步数</text>
            </view>
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ summaryData.avgCadence }}</text>
              <text class="stat-item-label">平均步频</text>
            </view>
            <view class="summary-stat-item">
              <text class="stat-item-num">{{ summaryData.elevationGain }}</text>
              <text class="stat-item-label">累计爬升(m)</text>
            </view>
          </view>
          <view class="summary-goal-banner" v-if="hasGoal">
            <text>{{ goalAchieved ? '目标达成！太棒了！' : '继续加油，下次一定行！' }}</text>
          </view>
        </view>
        <!-- 分段配速 -->
        <view class="splits-card" v-if="isGpsActivity && splitPaces.length > 0">
          <text class="splits-title">分段配速</text>
          <view class="splits-list">
            <view
              class="split-item"
              v-for="(sp, idx) in splitPaces"
              :key="idx"
            >
              <text class="split-num">{{ idx + 1 }}</text>
              <view class="split-bar-wrap">
                <view
                  class="split-bar"
                  :style="{ width: sp.barWidth + '%' }"
                  :class="{ best: sp.isBest, worst: sp.isWorst }"
                ></view>
              </view>
              <text class="split-pace">{{ formatPace(sp.pace) }}</text>
              <text class="split-dist">1km</text>
            </view>
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
import { startWorkout, pauseWorkout, resumeWorkout, finishWorkout, uploadTrajectory } from '@/api/workout'
import { getStatsSummary } from '@/api/stats'
const statusBarHeight = ref(0)
const screenWidth = ref(750)
const scrollLeft = ref(0)
const phase = ref('ready')
const isPaused = ref(false)
const isLocked = ref(false)
const goalType = ref('distance')
const goalValue = ref(5)
const gpsReady = ref(false)
const gpsAccuracy = ref(0)
const gpsErrorMsg = ref('')
const currentLat = ref(39.9042)
const currentLng = ref(116.4074)
const elapsedSeconds = ref(0)
const totalDistanceMeters = ref(0)
const currentCalories = ref(0)
const currentSteps = ref(0)
const recordId = ref(null)
const startTimestamp = ref(0)
const pausedDuration = ref({ accumulated: 0, start: 0 })
const pausedElapsedSeconds = ref(0)
const pausedTimerInterval = ref(null)
const lastLocation = ref(null)
const trajectory = ref([])
const timerInterval = ref(null)
const locationPollingInterval = ref(null)
const locationChangeReceived = ref(false)
const currentPace = ref(0)
const paceBuffer = ref([])
const currentCadence = ref(0)
const cadenceBuffer = ref([])
const lastStepTimestamp = ref(0)
const splitPaces = ref([])
const lastSplitKm = ref(0)
const lastSplitTimestamp = ref(0)
const elevationGain = ref(0)
const lastAltitude = ref(null)
const bestPace = ref(0)
const stats = ref({ totalRuns: 0, totalDistance: '0', totalDuration: '0' })
const summaryData = ref({
  distance: '0', duration: 0, avgPace: 0,
  bestPace: 0, maxPace: 0, maxPaceStr: "0'00\"", calories: 0, steps: 0, avgSpeed: '0',
  avgCadence: 0, elevationGain: 0, recordId: null
})
const LOCAL_RECORDS_KEY = 'local_workout_records'
const distanceOptions = [1, 2, 3, 5, 8, 10, 15, 21.1]
const durationOptions = [10, 15, 20, 30, 45, 60, 90, 120]
const distanceIntegerOptions = Array.from({ length: 100 }, (_, i) => i)
const distanceDecimalOptions = Array.from({ length: 10 }, (_, i) => i)
const durationHourOptions = Array.from({ length: 13 }, (_, i) => i)
const durationMinuteOptions = Array.from({ length: 12 }, (_, i) => i * 5)
const distancePickerValue = ref([5, 0])
const durationPickerValue = ref([0, 6])
const showGoalInput = ref(false)
const goalInputValue = ref('')
const selectedActId = ref(null)
const selectedCustomActivity = ref(null)
const runningActivities = ref([])
const activityList = [
  { key: 'run-outdoor', name: '跑步', meta: '配速训练', icon: 'run', type: 1, needsGps: true },
  { key: 'bike-outdoor', name: '骑行', meta: '路线记录', icon: 'cycling', type: 3, needsGps: true },
  { key: 'hike', name: '徒步', meta: '耐力出行', icon: 'walk', type: 2, needsGps: true },
  { key: 'walk', name: '健步走', meta: '轻负荷', icon: 'steps', type: 2, needsGps: true },
  { key: 'climb', name: '登山', meta: '爬升挑战', icon: 'level', type: 2, needsGps: true },
  { key: 'bike-indoor', name: '骑行台', meta: '室内有氧', icon: 'cycling', type: 3, needsGps: false },
  { key: 'swim', name: '游泳', meta: '全身耐力', icon: 'hydration', type: 1, needsGps: false },
  { key: 'yoga', name: '瑜伽', meta: '柔韧恢复', icon: 'stretch', type: 1, needsGps: false },
  { key: 'strength', name: '力量', meta: '肌群训练', icon: 'strength', type: 1, needsGps: false },
  { key: 'aerobic', name: '有氧操', meta: '燃脂节奏', icon: 'cardio', type: 1, needsGps: false },
  { key: 'stretch', name: '拉伸', meta: '舒展放松', icon: 'heart', type: 1, needsGps: false }
]
const selectedActivityIndex = ref(0)
const selectedActivity = computed(() => activityList[selectedActivityIndex.value])
const startBtnText = computed(() => '开始' + selectedActivity.value.name)
const isGpsActivity = computed(() => selectedActivity.value.needsGps)
const activityCopy = computed(() => {
  const key = selectedActivity.value.key
  const map = {
    'run-outdoor': {
      freeTitle: '自由跑步',
      freeDesc: '随时开始记录',
      targetTitle: '目标训练',
      targetDesc: '按距离或计时完成',
      distanceDesc: '适合节奏跑、轻松跑和固定里程训练',
      durationDesc: '适合热身、恢复跑和时间管理训练'
    },
    'bike-outdoor': {
      freeTitle: '自由骑行',
      freeDesc: '记录路线与速度',
      targetTitle: '骑行目标',
      targetDesc: '按距离或计时完成',
      distanceDesc: '适合通勤骑、长距离骑行和路线挑战',
      durationDesc: '适合室外有氧和固定时间骑行'
    },
    hike: {
      freeTitle: '自由徒步',
      freeDesc: '记录路线与爬升',
      targetTitle: '徒步目标',
      targetDesc: '按距离或计时完成',
      distanceDesc: '适合路线徒步、城市远足和耐力训练',
      durationDesc: '适合固定时间的轻负荷耐力训练'
    },
    walk: {
      freeTitle: '自由健走',
      freeDesc: '轻松记录步行',
      targetTitle: '健走目标',
      targetDesc: '按距离或计时完成',
      distanceDesc: '适合每日步行、饭后健走和低强度有氧',
      durationDesc: '适合固定时间的轻松活动'
    },
    climb: {
      freeTitle: '自由登山',
      freeDesc: '记录轨迹与爬升',
      targetTitle: '登山目标',
      targetDesc: '按距离或计时完成',
      distanceDesc: '适合登山路线、爬升挑战和户外训练',
      durationDesc: '适合按时间控制强度的登山活动'
    },
    'bike-indoor': {
      freeTitle: '自由骑行',
      freeDesc: '室内轻松开始',
      targetTitle: '计时骑行',
      targetDesc: '按时长完成训练',
      durationDesc: '适合骑行台、有氧区间和固定时长课程'
    },
    swim: {
      freeTitle: '自由游泳',
      freeDesc: '记录泳池训练',
      targetTitle: '计时游泳',
      targetDesc: '按时长完成训练',
      durationDesc: '适合泳池练习、技术训练和恢复游'
    },
    yoga: {
      freeTitle: '自由瑜伽',
      freeDesc: '安静开始练习',
      targetTitle: '计时练习',
      targetDesc: '按时长完成课程',
      durationDesc: '适合瑜伽、呼吸练习和柔韧恢复'
    },
    strength: {
      freeTitle: '自由力量',
      freeDesc: '记录训练时间',
      targetTitle: '计时训练',
      targetDesc: '按时长完成训练',
      durationDesc: '适合力量训练、循环组和器械训练'
    },
    aerobic: {
      freeTitle: '自由有氧',
      freeDesc: '跟着节奏动起来',
      targetTitle: '计时有氧',
      targetDesc: '按时长完成训练',
      durationDesc: '适合有氧操、燃脂课程和心肺训练'
    },
    stretch: {
      freeTitle: '自由拉伸',
      freeDesc: '舒展恢复',
      targetTitle: '计时拉伸',
      targetDesc: '按时长完成恢复',
      durationDesc: '适合训练后放松、晨间舒展和恢复练习'
    }
  }
  return map[key] || {
    freeTitle: '自由模式',
    freeDesc: '无目标限制',
    targetTitle: '目标模式',
    targetDesc: '按时长完成训练',
    distanceDesc: '适合按距离记录的运动',
    durationDesc: '适合按时间记录的训练'
  }
})
const freeModeTitle = computed(() => activityCopy.value.freeTitle)
const freeModeDesc = computed(() => activityCopy.value.freeDesc)
const targetModeTitle = computed(() => activityCopy.value.targetTitle)
const targetModeDesc = computed(() => activityCopy.value.targetDesc)
const indoorHeroDesc = computed(() => activityCopy.value.durationDesc || '按自己的节奏完成训练')
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
const goalSummaryValue = computed(() => {
  if (goalType.value === 'distance') return goalValue.value
  if (goalType.value === 'duration') return goalValue.value
  if (goalType.value === 'custom' && selectedCustomActivity.value) return selectedCustomActivity.value.title
  return '待选择'
})
const goalSummaryUnit = computed(() => {
  if (goalType.value === 'distance') return 'km'
  if (goalType.value === 'duration') return 'min'
  return ''
})
const goalSummaryDesc = computed(() => {
  if (goalType.value === 'distance') return activityCopy.value.distanceDesc || '适合按距离记录的户外运动'
  if (goalType.value === 'duration') return activityCopy.value.durationDesc || '适合按时间记录的训练'
  if (selectedCustomActivity.value) return '使用你的定制训练结构'
  return '选择一个定制活动后开始'
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
const remainingDistance = computed(() => {
  if (goalType.value !== 'distance') return '0'
  const rem = goalValue.value - totalDistanceMeters.value / 1000
  return rem > 0 ? rem.toFixed(2) : '0.00'
})
const remainingDuration = computed(() => {
  if (goalType.value !== 'duration') return 0
  const rem = goalValue.value * 60 - elapsedSeconds.value
  return rem > 0 ? rem : 0
})
const polylineData = computed(() => {
  if (trajectory.value.length < 2) return []
  return [{
    points: trajectory.value.map(p => ({ latitude: p.latitude, longitude: p.longitude })),
    color: '#22D3EE',
    width: 4,
    arrowLine: false
  }]
})
const EARTH_RADIUS = 6371000
function toRad(d) { return (d * Math.PI) / 180 }
function haversineDistance(lat1, lon1, lat2, lon2) {
  const dLat = toRad(lat2 - lat1), dLon = toRad(lon2 - lon1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2
  return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}
const metMap = {
  'run-outdoor': 9.8, 'bike-outdoor': 7.5, 'hike': 6.0,
  'walk': 3.5, 'climb': 8.0, 'bike-indoor': 7.0,
  'swim': 8.0, 'yoga': 2.5, 'strength': 5.0,
  'aerobic': 6.5, 'stretch': 2.0
}
const userWeight = ref(70)
function calcCaloriesByMet(seconds) {
  const met = metMap[selectedActivity.value.key] || 5
  return Math.round(met * userWeight.value * (seconds / 3600))
}
function updateCurrentPace(speed) {
  if (speed > 0.5) {
    const paceSec = Math.round(1000 / speed)
    paceBuffer.value.push(paceSec)
    if (paceBuffer.value.length > 10) paceBuffer.value.shift()
    const sum = paceBuffer.value.reduce((a, b) => a + b, 0)
    currentPace.value = Math.round(sum / paceBuffer.value.length)
    if (bestPace.value === 0 || currentPace.value < bestPace.value) {
      bestPace.value = currentPace.value
    }
  } else {
    // 速度过低时配速归零，避免显示旧值
    currentPace.value = 0
  }
}
function updateCadence() {
  const now = Date.now()
  if (lastStepTimestamp.value > 0) {
    const stepInterval = now - lastStepTimestamp.value
    if (stepInterval > 200 && stepInterval < 2000) {
      const instantCadence = Math.round(60000 / stepInterval)
      cadenceBuffer.value.push(instantCadence)
      if (cadenceBuffer.value.length > 20) cadenceBuffer.value.shift()
      const sum = cadenceBuffer.value.reduce((a, b) => a + b, 0)
      currentCadence.value = Math.round(sum / cadenceBuffer.value.length)
    }
  }
  lastStepTimestamp.value = now
}
function checkSplitPace() {
  const currentKm = Math.floor(totalDistanceMeters.value / 1000)
  if (currentKm > lastSplitKm.value && currentKm > 0) {
    const splitTime = elapsedSeconds.value - (lastSplitTimestamp.value || 0)
    const splitPace = splitTime
    splitPaces.value.push({
      pace: splitPace,
      barWidth: 0,
      isBest: false,
      isWorst: false
    })
    updateSplitBarWidths()
    lastSplitKm.value = currentKm
    lastSplitTimestamp.value = elapsedSeconds.value
    voiceAnnounce(currentKm, splitPace)
  }
}
function updateSplitBarWidths() {
  if (splitPaces.value.length === 0) return
  const paces = splitPaces.value.map(s => s.pace).filter(p => p > 0)
  if (paces.length === 0) return
  const minPace = Math.min(...paces)
  const maxPace = Math.max(...paces)
  const bestIdx = splitPaces.value.findIndex(s => s.pace === minPace)
  const worstIdx = splitPaces.value.findIndex(s => s.pace === maxPace)
  splitPaces.value.forEach((sp, idx) => {
    sp.isBest = idx === bestIdx
    sp.isWorst = idx === worstIdx && paces.length > 1
    if (maxPace === minPace) {
      sp.barWidth = 80
    } else {
      sp.barWidth = Math.max(20, Math.round(((maxPace - sp.pace) / (maxPace - minPace)) * 60 + 20))
    }
  })
}
function voiceAnnounce(km, splitPace) {
  try {
    const innerAudioContext = uni.createInnerAudioContext()
    const paceMin = Math.floor(splitPace / 60)
    const paceSec = splitPace % 60
    const text = `已跑${km}公里，本公里配速${paceMin}分${paceSec}秒`
    // #ifdef APP-PLUS
    plus.speech.startSpeak(text)
    // #endif
    // #ifndef APP-PLUS
    uni.showToast({ title: text, icon: 'none', duration: 3000 })
    // #endif
  } catch (e) {}
}
function toTrajectoryPayload(p) {
  return {
    latitude: p.latitude,
    longitude: p.longitude,
    accuracy: p.accuracy || 0,
    speed: p.speed || 0,
    altitude: p.altitude || 0,
    timestamp: p.timestamp
  }
}
function buildWorkoutFinishPayload() {
  const duration = elapsedSeconds.value
  const distance = Math.round(totalDistanceMeters.value)
  const avgSpeedMps = duration > 0 ? Number((distance / duration).toFixed(2)) : 0
  const maxSpeedMps = bestPace.value > 0 ? Number((1000 / bestPace.value).toFixed(2)) : avgSpeedMps
  return {
    duration,
    distance,
    calories: currentCalories.value,
    steps: currentSteps.value,
    avgSpeed: avgSpeedMps,
    maxSpeed: maxSpeedMps,
    totalAscent: Math.round(elevationGain.value),
    trajectoryCount: trajectory.value.length
  }
}
function saveLocalWorkoutRecord(summary) {
  const now = new Date()
  const endTime = now.toISOString()
  const startTime = new Date(startTimestamp.value || Date.now()).toISOString()
  const metrics = buildWorkoutFinishPayload()
  const record = {
    id: `local-${Date.now()}`,
    localOnly: true,
    type: selectedActivity.value.type,
    typeName: selectedActivity.value.name,
    status: 1,
    statusDesc: '已完成',
    startTime,
    endTime,
    startTimeStr: startTime.replace('T', ' ').substring(0, 19),
    endTimeStr: endTime.replace('T', ' ').substring(0, 19),
    duration: summary.duration || elapsedSeconds.value,
    distance: Math.round(Number(summary.distance || 0) * 1000),
    calories: summary.calories || currentCalories.value,
    steps: summary.steps || currentSteps.value,
    avgPace: summary.avgPace || avgPace.value,
    bestPace: summary.bestPace || bestPace.value,
    avgSpeed: metrics.avgSpeed,
    maxSpeed: metrics.maxSpeed,
    totalAscent: metrics.totalAscent,
    goalType: goalType.value,
    goalValue: goalValue.value,
    goalAchieved: goalAchieved.value,
    trajectory: trajectory.value.map(toTrajectoryPayload)
  }
  try {
    const raw = uni.getStorageSync(LOCAL_RECORDS_KEY)
    const list = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
    const nextList = [record, ...(Array.isArray(list) ? list : [])].slice(0, 100)
    uni.setStorageSync(LOCAL_RECORDS_KEY, JSON.stringify(nextList))
  } catch (e) {}
  return record
}
function onLocationChange(res) {
  if (isPaused.value || !res || !res.latitude || !res.longitude) return
  locationChangeReceived.value = true
  const { latitude, longitude, accuracy, speed, altitude } = res
  gpsAccuracy.value = accuracy || 0
  currentLat.value = latitude
  currentLng.value = longitude
  if (speed && speed > 0) {
    updateCurrentPace(speed)
  } else {
    currentPace.value = 0
  }
  const isFirstLocation = !lastLocation.value
  let moved = false
  let shouldUpdateLastLoc = true
  if (lastLocation.value) {
    const dist = haversineDistance(
      lastLocation.value.latitude, lastLocation.value.longitude, latitude, longitude)
    const dt = (Date.now() - lastLocation.value.timestamp) / 1000
    const calculatedSpeed = dt > 0 ? dist / dt : 0
    if (dt > 0 && calculatedSpeed < 50) {
      if (accuracy && accuracy > 100 && dist < 3) {
        shouldUpdateLastLoc = false
      } else if (dist > 1) {
        totalDistanceMeters.value += dist
        currentSteps.value = Math.round(totalDistanceMeters.value / 0.8)
        updateCadence()
        checkSplitPace()
        moved = true
      } else if (dt <= 30) {
        shouldUpdateLastLoc = false
      }
    }
  }
  if (altitude && altitude !== 0) {
    if (lastAltitude.value !== null && altitude > lastAltitude.value) {
      elevationGain.value += (altitude - lastAltitude.value)
    }
    lastAltitude.value = altitude
  }
  if (shouldUpdateLastLoc) {
    lastLocation.value = { latitude, longitude, timestamp: Date.now() }
  }
  if (moved || isFirstLocation) {
    trajectory.value.push({ latitude, longitude, accuracy, speed, altitude, timestamp: Date.now() })
  }
  currentCalories.value = Math.round(70 * (totalDistanceMeters.value / 1000) * 1.036)
  if (hasGoal.value && progressPercent.value >= 100 && elapsedSeconds.value > 5) autoFinish()
}
async function startRunning() {
  if (goalType.value === 'custom' && !selectedCustomActivity.value) {
    uni.showToast({ title: '请先选择一个定制活动', icon: 'none' })
    return
  }
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
  isLocked.value = false
  elapsedSeconds.value = 0
  totalDistanceMeters.value = 0
  currentCalories.value = 0
  currentSteps.value = 0
  currentPace.value = 0
  currentCadence.value = 0
  bestPace.value = 0
  lastLocation.value = null
  trajectory.value = []
  pausedDuration.value = { accumulated: 0, start: 0 }
  pausedElapsedSeconds.value = 0
  paceBuffer.value = []
  cadenceBuffer.value = []
  lastStepTimestamp.value = 0
  splitPaces.value = []
  lastSplitKm.value = 0
  lastSplitTimestamp.value = 0
  elevationGain.value = 0
  lastAltitude.value = null
  if (isGpsActivity.value) {
    locationChangeReceived.value = false
    // #ifdef MP-WEIXIN
    wx.startLocationUpdate({
      success: () => {
        gpsReady.value = true
        wx.onLocationChange(onLocationChange)
        setTimeout(() => {
          if (!locationChangeReceived.value) {
            startLocationPolling()
          }
        }, 5000)
      },
      fail: (err) => {
        console.error('wx.startLocationUpdate fail:', err)
        showGpsPermissionGuide()
        startLocationPolling()
      }
    })
    // #endif
    // #ifndef MP-WEIXIN
    uni.startLocationUpdate({
      success: () => {
        gpsReady.value = true
        uni.onLocationChange(onLocationChange)
        setTimeout(() => {
          if (!locationChangeReceived.value) {
            startLocationPolling()
          }
        }, 5000)
      },
      fail: (err) => {
        console.error('startLocationUpdate fail:', err)
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
        startLocationPolling()
      }
    })
    // #endif
  }
  try {
    const res = await startWorkout({ type: selectedActivity.value.type, activityName: selectedActivity.value.name })
    if (res.code === 200 && res.data) recordId.value = res.data.recordId
  } catch (e) {}
  phase.value = 'active'
  startTimestamp.value = Date.now()
  timerInterval.value = setInterval(() => {
    if (isPaused.value) return
    const now = Date.now()
    const acc = typeof pausedDuration.value.accumulated === 'number' ? pausedDuration.value.accumulated : 0
    elapsedSeconds.value = Math.floor((now - startTimestamp.value - acc) / 1000)
    if (!isGpsActivity.value) {
      currentCalories.value = calcCaloriesByMet(elapsedSeconds.value)
    }
    if (trajectory.value.length >= 10 && recordId.value) uploadTrajectoryToServer()
  }, 1000)
}
function togglePause() {
  isPaused.value = !isPaused.value
  if (isPaused.value) {
    if (recordId.value) pauseWorkout(recordId.value).catch(() => {})
    pausedDuration.value = { accumulated: pausedDuration.value.accumulated || 0, start: Date.now() }
    pausedElapsedSeconds.value = 0
    pausedTimerInterval.value = setInterval(() => {
      pausedElapsedSeconds.value++
    }, 1000)
  } else {
    if (recordId.value) resumeWorkout(recordId.value).catch(() => {})
    if (pausedDuration.value.start) {
      pausedDuration.value.accumulated = (pausedDuration.value.accumulated || 0) + (Date.now() - pausedDuration.value.start)
    }
    if (pausedTimerInterval.value) {
      clearInterval(pausedTimerInterval.value)
      pausedTimerInterval.value = null
    }
    pausedElapsedSeconds.value = 0
    // 恢复运动时重置 lastLocation，防止位置跳跃导致距离暴增
    lastLocation.value = null
    lastAltitude.value = null
    // 清空配速/步频缓冲区，避免恢复后数据异常
    paceBuffer.value = []
    cadenceBuffer.value = []
  }
}
function toggleLock() {
  isLocked.value = !isLocked.value
}
function unlockScreen() {
  isLocked.value = false
}
function startLocationPolling() {
  if (locationPollingInterval.value) return
  locationPollingInterval.value = setInterval(() => {
    if (isPaused.value || phase.value !== 'active') return
    // #ifdef MP-WEIXIN
    wx.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      highAccuracyExpireTime: 3000,
      success: (res) => {
        onLocationChange(res)
      },
      fail: () => {}
    })
    // #endif
    // #ifndef MP-WEIXIN
    uni.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      highAccuracyExpireTime: 3000,
      success: (res) => {
        onLocationChange(res)
      },
      fail: () => {}
    })
    // #endif
  }, 3000)
}
function stopLocationPolling() {
  if (locationPollingInterval.value) {
    clearInterval(locationPollingInterval.value)
    locationPollingInterval.value = null
  }
}
let lastTrajUpload = 0
function uploadTrajectoryToServer() {
  const now = Date.now()
  if (now - lastTrajUpload < 10000 || !recordId.value) return
  lastTrajUpload = now
  const pts = trajectory.value.slice(-15)
  if (!pts.length) return
  uploadTrajectory({ recordId: recordId.value, trajectory: pts.map(toTrajectoryPayload) }).catch(() => {})
}
function autoFinish() {
  uni.showModal({
    title: '目标达成',
    content: '恭喜！您已完成运动目标，是否结束运动？',
    confirmText: '结束运动',
    cancelText: '继续运动',
    success: (r) => {
      if (r.confirm) finishRunning()
    }
  })
}
async function finishRunning() {
  if (isGpsActivity.value) {
    // #ifdef MP-WEIXIN
    wx.offLocationChange(onLocationChange)
    wx.stopLocationUpdate()
    // #endif
    // #ifndef MP-WEIXIN
    uni.offLocationChange(onLocationChange)
    uni.stopLocationUpdate()
    // #endif
    stopLocationPolling()
  }
  if (timerInterval.value) { clearInterval(timerInterval.value); timerInterval.value = null }
  if (pausedTimerInterval.value) { clearInterval(pausedTimerInterval.value); pausedTimerInterval.value = null }
  if (recordId.value && trajectory.value.length > 0) {
    try { await uploadTrajectory({ recordId: recordId.value, trajectory: trajectory.value.map(toTrajectoryPayload) }) } catch (e) {}
  }
  const d = elapsedSeconds.value
  const dist = (totalDistanceMeters.value / 1000).toFixed(2)
  const p = avgPace.value
  const spd = totalDistanceMeters.value > 0 ? ((totalDistanceMeters.value / 1000) / (d / 3600)).toFixed(1) : '0'
  const avgCad = d > 0 ? Math.round(currentSteps.value / (d / 60)) : 0
  if (isGpsActivity.value) {
    summaryData.value = {
      distance: dist, duration: d, avgPace: p, bestPace: bestPace.value,
      maxPace: p, maxPaceStr: formatPace(p), calories: currentCalories.value,
      steps: currentSteps.value, avgSpeed: spd, avgCadence: avgCad,
      elevationGain: Math.round(elevationGain.value), recordId: recordId.value
    }
  } else {
    summaryData.value = {
      distance: '0.00', duration: d, avgPace: 0, bestPace: 0,
      maxPace: 0, maxPaceStr: '--', calories: currentCalories.value,
      steps: 0, avgSpeed: '0', avgCadence: 0, elevationGain: 0,
      recordId: recordId.value
    }
  }
  let serverSaved = false
  if (recordId.value) {
    try {
      const res = await finishWorkout(recordId.value, buildWorkoutFinishPayload())
      serverSaved = res && res.code === 200
    } catch (e) {
      serverSaved = false
    }
  }
  if (!serverSaved) {
    const localRecord = saveLocalWorkoutRecord(summaryData.value)
    summaryData.value.recordId = localRecord.id
    uni.showToast({ title: '已保存到本地记录', icon: 'none' })
  }
  phase.value = 'summary'
}
function discardRunning() {
  uni.showModal({
    title: '确认放弃', content: '放弃后本次数据不会保存', confirmText: '确定放弃', cancelText: '继续', confirmColor: '#ef4444',
    success: r => {
      if (!r.confirm) return
      // #ifdef MP-WEIXIN
      wx.offLocationChange(onLocationChange)
      wx.stopLocationUpdate()
      // #endif
      // #ifndef MP-WEIXIN
      uni.offLocationChange(onLocationChange)
      uni.stopLocationUpdate()
      // #endif
      stopLocationPolling()
      if (timerInterval.value) { clearInterval(timerInterval.value); timerInterval.value = null }
      if (pausedTimerInterval.value) { clearInterval(pausedTimerInterval.value); pausedTimerInterval.value = null }
      recordId.value = null; trajectory.value = []
      isLocked.value = false
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
  currentPace.value = 0; currentCadence.value = 0
  bestPace.value = 0; splitPaces.value = []
  elevationGain.value = 0; lastAltitude.value = null
  trajectory.value = []; lastLocation.value = null
  isLocked.value = false
  phase.value = 'ready'
}
function goNextItem() {
  uni.showToast({ title: '暂无下一项', icon: 'none' })
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
function selectGoalValue(value) {
  goalValue.value = value
  syncPickerFromGoal()
}
function enableTargetMode() {
  switchGoalType(isGpsActivity.value ? 'distance' : 'duration')
}
function switchGoalType(type) {
  goalType.value = type
  if (type === 'distance' && !distanceOptions.includes(goalValue.value)) goalValue.value = 5
  if (type === 'duration' && !durationOptions.includes(goalValue.value)) goalValue.value = 30
  if (type === 'custom' && selectedCustomActivity.value) goalValue.value = selectedCustomActivity.value.id
  syncPickerFromGoal()
}
function syncPickerFromGoal() {
  if (goalType.value === 'distance') {
    const value = Math.max(0, Number(goalValue.value) || 0)
    const integer = Math.min(99, Math.floor(value))
    const decimal = Math.min(9, Math.round((value - integer) * 10))
    distancePickerValue.value = [integer, decimal]
  }
  if (goalType.value === 'duration') {
    const minutes = Math.max(5, Math.round(Number(goalValue.value) || 30))
    const hour = Math.min(12, Math.floor(minutes / 60))
    const minute = Math.min(55, minutes % 60)
    const minuteIndex = durationMinuteOptions.findIndex(n => n >= minute)
    durationPickerValue.value = [hour, minuteIndex >= 0 ? minuteIndex : 0]
  }
}
function onDistancePickerChange(e) {
  const [integerIndex, decimalIndex] = e.detail.value
  const integer = distanceIntegerOptions[integerIndex] || 0
  const decimal = distanceDecimalOptions[decimalIndex] || 0
  const value = Number((integer + decimal / 10).toFixed(1))
  goalValue.value = value > 0 ? value : 0.1
  syncPickerFromGoal()
}
function onDurationPickerChange(e) {
  const [hourIndex, minuteIndex] = e.detail.value
  const hours = durationHourOptions[hourIndex] || 0
  const minutes = durationMinuteOptions[minuteIndex] || 0
  const value = hours * 60 + minutes
  goalValue.value = value > 0 ? value : 5
  syncPickerFromGoal()
}
function openGoalInput() {
  if (goalType.value === 'custom') return
  goalInputValue.value = goalType.value === 'duration'
    ? String(goalValue.value || 30)
    : String(goalValue.value || 5)
  showGoalInput.value = true
}
function closeGoalInput() {
  showGoalInput.value = false
}
function confirmGoalInput() {
  const value = Number(goalInputValue.value)
  if (!Number.isFinite(value) || value <= 0) {
    uni.showToast({ title: '请输入有效数字', icon: 'none' })
    return
  }
  goalValue.value = goalType.value === 'duration'
    ? Math.min(720, Math.round(value))
    : Math.min(99.9, Number(value.toFixed(1)))
  syncPickerFromGoal()
  closeGoalInput()
}
function goalOptionLabel(value) {
  if (value <= 2) return '轻松'
  if (value <= 5) return '常用'
  if (value <= 10) return '进阶'
  return '挑战'
}
function durationOptionLabel(value) {
  if (value <= 15) return '唤醒'
  if (value <= 30) return '日常'
  if (value <= 60) return '强化'
  return '长课'
}
function selectActivity(index) {
  selectedActivityIndex.value = index
  scrollLeft.value = Math.max(0, Math.round(index * 232 * screenWidth.value / 750))
  if (!selectedActivity.value.needsGps && goalType.value === 'distance') {
    goalType.value = 'duration'
    goalValue.value = 30
  }
}
function initGpsLocation() {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    wx.getSetting({
      success: (settingRes) => {
        const auth = settingRes.authSetting
        if (auth['scope.userLocation'] === false) {
          gpsErrorMsg.value = '定位权限已拒绝，请在设置中开启'
          resolve(false)
        } else if (auth['scope.userLocation'] === true || auth['scope.userLocation'] === undefined) {
          wx.getLocation({
            type: 'gcj02',
            isHighAccuracy: true,
            highAccuracyExpireTime: 5000,
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
async function ensureLocationPermission() {
  // #ifdef MP-WEIXIN
  return new Promise((resolve) => {
    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userLocation'] === false) {
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
          wx.getLocation({
            type: 'gcj02',
            isHighAccuracy: true,
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
    content: '请在右上角设置中开启"位置信息"，用于记录运动轨迹。\n操作：右上角 ···，设置，位置信息，开启',
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
  // #ifdef MP-WEIXIN
  wx.offLocationChange(onLocationChange)
  wx.stopLocationUpdate()
  // #endif
  // #ifndef MP-WEIXIN
  uni.offLocationChange(onLocationChange)
  uni.stopLocationUpdate()
  // #endif
  stopLocationPolling()
  if (timerInterval.value) { clearInterval(timerInterval.value); timerInterval.value = null }
  if (pausedTimerInterval.value) { clearInterval(pausedTimerInterval.value); pausedTimerInterval.value = null }
})
</script>
<style lang="scss" scoped>
.container { height: 100vh; overflow: hidden; background: var(--bg-primary); display: flex; flex-direction: column; }
/* ==================== 活动选择条 ==================== */
.activity-bar {
  position: relative;
  background:
    linear-gradient(180deg, var(--bg-glass-strong) 0%, var(--bg-glass) 72%, transparent 100%);
  flex-shrink: 0;
}
.activity-bar-inner {
  position: relative;
  height: 124rpx;
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
  padding: 10rpx 0;
}
.activity-spacer {
  display: inline-block;
  width: 28rpx;
  height: 1rpx;
}
.activity-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 14rpx;
  width: 206rpx;
  height: 92rpx;
  margin-right: 16rpx;
  padding: 14rpx;
  border-radius: 24rpx;
  background: var(--bg-card);
  border: 1rpx solid var(--card-border);
  box-shadow: var(--card-shadow-soft);
  vertical-align: middle;
  opacity: 0.68;
  transition: all 0.25s ease;
  &.active {
    opacity: 1;
    transform: translateY(-4rpx);
    background: linear-gradient(135deg, var(--accent-green) 0%, var(--accent-blue) 100%);
    box-shadow: 0 16rpx 34rpx var(--accent-glow);
    border-color: transparent;
    .act-icon-shell {
      color: var(--accent-green-dark);
      background: rgba(255, 255, 255, 0.9);
    }
    .act-name {
      color: #fff;
      font-weight: 700;
    }
    .act-meta {
      color: rgba(255, 255, 255, 0.78);
    }
  }
}
.act-icon-shell {
  width: 52rpx;
  height: 52rpx;
  border-radius: 18rpx;
  color: var(--accent-green);
  background: var(--soft-green);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.act-copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.act-name {
  font-size: 26rpx;
  color: var(--text-primary);
  transition: all 0.25s ease;
  font-weight: 700;
  white-space: nowrap;
}
.act-meta {
  font-size: 20rpx;
  color: var(--text-tertiary);
  white-space: nowrap;
}
.act-type {
  display: none;
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
  &.ok { background: var(--accent-green); }
  &.fair { background: #fbbf24; }
  &.weak { background: #ef4444; }
}
.gps-float-text { font-size: 22rpx; color: #555; }
.gps-retry {
  position: absolute;
  top: 110rpx;
  left: 30rpx;
  background: var(--bg-card);
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  color: var(--accent-green);
  font-weight: 600;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.08);
  z-index: 10;
}
.activity-info-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--soft-green) 0%, var(--soft-blue) 100%);
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
  height: auto;
  min-height: 0;
  max-height: calc(100vh - 176rpx);
  background: var(--bg-card);
  border-radius: 32rpx 32rpx 0 0;
  margin-top: -24rpx;
  z-index: 20;
  position: relative;
  padding: 22rpx 28rpx calc(108rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
  transition: height 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.bottom-card.compact {
  height: auto;
  min-height: 178rpx;
}
.gps-options-wrap {
  flex: 1;
  min-height: 0;
  max-height: calc(100vh - 320rpx);
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
.mode-tabs {
  display: flex;
  gap: 12rpx;
}
.mode-tab {
  flex: 1;
  padding: 14rpx 16rpx;
  border-radius: 20rpx;
  background: var(--bg-secondary);
  text-align: left;
  transition: all 0.2s;
  border: 1rpx solid var(--metric-border);
  &.active {
    background: linear-gradient(135deg, var(--selected-soft), var(--bg-elevated));
    border: 2rpx solid var(--accent-green);
  }
}
.mode-tab-text { font-size: 26rpx; font-weight: 600; color: var(--text-primary); display: block; }
.mode-tab-desc { font-size: 20rpx; color: var(--text-tertiary); margin-top: 2rpx; display: block; }
.mode-tab.active .mode-tab-text { color: var(--accent-green-dark); }
.mode-tab.active .mode-tab-desc { color: var(--accent-green); }
.goal-summary {
  margin-top: 14rpx;
  padding: 16rpx 18rpx;
  border-radius: 22rpx;
  background:
    linear-gradient(135deg, rgba(34, 197, 94, 0.14), rgba(59, 130, 246, 0.12)),
    var(--bg-elevated);
  border: 1rpx solid var(--metric-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}
.goal-summary-left {
  flex: 1;
  min-width: 0;
}
.goal-summary-kicker {
  font-size: 20rpx;
  color: var(--text-tertiary);
  display: block;
}
.goal-summary-main {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 2rpx;
}
.goal-summary-value {
  max-width: 440rpx;
  font-size: 42rpx;
  line-height: 1;
  font-weight: 800;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.goal-summary-unit {
  font-size: 22rpx;
  font-weight: 700;
  color: var(--accent-green);
}
.goal-summary-desc {
  display: block;
  margin-top: 4rpx;
  font-size: 20rpx;
  color: var(--text-secondary);
  opacity: 0.72;
  line-height: 1.35;
}
.goal-summary-icon {
  width: 66rpx;
  height: 66rpx;
  border-radius: 20rpx;
  background: var(--bg-card);
  color: var(--accent-green);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--card-shadow-soft);
}
.sub-options { margin-top: 12rpx; }
.sub-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 10rpx;
}
.sub-tab {
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  &.active { background: linear-gradient(135deg, var(--accent-green), var(--accent-green-dark)); color: #fff; font-weight: 600; }
}
.goal-wheel-card {
  position: relative;
  border-radius: 22rpx;
  background: var(--surface-row-gradient);
  border: 1rpx solid var(--metric-border);
  overflow: hidden;
  box-shadow: var(--card-shadow-soft);
}
.goal-wheel-edit {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 18rpx 0;
}
.goal-wheel-edit-value {
  font-size: 40rpx;
  line-height: 1;
  font-weight: 800;
  color: var(--text-primary);
}
.goal-wheel-edit-unit {
  font-size: 20rpx;
  color: var(--accent-green);
  font-weight: 700;
}
.goal-picker {
  height: 164rpx;
  width: 100%;
  padding: 0 76rpx;
  box-sizing: border-box;
}
.goal-picker-indicator {
  height: 54rpx;
  border-radius: 18rpx;
  background: rgba(34, 197, 94, 0.1);
}
.picker-option {
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 800;
  color: var(--text-primary);
}
.goal-picker-dot {
  position: absolute;
  left: 50%;
  top: 152rpx;
  transform: translate(-50%, -50%);
  font-size: 38rpx;
  line-height: 1;
  font-weight: 900;
  color: var(--accent-green);
  pointer-events: none;
  z-index: 2;
}
.goal-wheel-unit {
  position: absolute;
  right: 18rpx;
  bottom: 18rpx;
  font-size: 20rpx;
  color: var(--text-tertiary);
  font-weight: 700;
}
.picker-scroll { width: 100%; }
.picker-row {
  display: flex;
  gap: 16rpx;
  padding: 4rpx 2rpx 12rpx;
  white-space: nowrap;
}
.picker-chip {
  flex-shrink: 0;
  width: 136rpx;
  min-height: 132rpx;
  padding: 16rpx 18rpx;
  border-radius: 24rpx;
  background: var(--surface-row-gradient);
  text-align: left;
  border: 1rpx solid var(--metric-border);
  box-shadow: var(--card-shadow-soft);
  &.selected {
    background: linear-gradient(135deg, var(--accent-green), var(--accent-blue));
    border-color: var(--accent-green);
    .chip-label,
    .chip-num,
    .chip-unit {
      color: #fff;
    }
  }
}
.chip-label {
  font-size: 20rpx;
  color: var(--text-tertiary);
  display: block;
  margin-bottom: 12rpx;
}
.chip-num { font-size: 38rpx; font-weight: 800; color: var(--text-primary); display: block; line-height: 1; }
.chip-unit { font-size: 20rpx; color: var(--accent-green); margin-top: 8rpx; display: block; font-weight: 700; }
.custom-scroll {
  max-height: 176rpx;
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
    background: var(--selected-soft);
    border-color: var(--accent-green);
  }
  &:active { background: var(--selected-soft); }
}
.custom-act-left {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: var(--soft-green);
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
  color: var(--text-primary);
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.custom-act-meta {
  font-size: 20rpx;
  color: var(--text-tertiary);
  margin-top: 4rpx;
  display: block;
}
.custom-act-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: var(--accent-green);
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
  color: var(--text-tertiary);
  margin-top: 12rpx;
  display: block;
}
.empty-hint {
  font-size: 20rpx;
  color: #bbb;
  margin-top: 6rpx;
  display: block;
}
.start-run-btn {
  flex-shrink: 0;
  margin-top: 14rpx;
  height: 82rpx;
  line-height: 82rpx;
  text-align: center;
  background: linear-gradient(135deg, var(--accent-green), var(--accent-green-dark));
  border-radius: 48rpx;
  box-shadow: 0 6rpx 20rpx var(--accent-glow);
}
.start-run-text { font-size: 30rpx; font-weight: 700; color: #fff; letter-spacing: 3rpx; }
.start-run-btn:active { opacity: 0.85; transform: scale(0.98); }
.goal-input-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 80;
  background: rgba(0, 0, 0, 0.38);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}
.goal-input-card {
  width: 100%;
  max-width: 620rpx;
  border-radius: 30rpx;
  padding: 34rpx;
  background: var(--bg-card);
  border: 1rpx solid var(--card-border);
  box-shadow: var(--card-shadow);
}
.goal-input-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: var(--text-primary);
}
.goal-input-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--text-tertiary);
}
.goal-input {
  margin-top: 28rpx;
  height: 96rpx;
  padding: 0 24rpx;
  border-radius: 22rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--metric-border);
  font-size: 36rpx;
  font-weight: 800;
  color: var(--text-primary);
}
.goal-input-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 28rpx;
}
.goal-input-btn {
  flex: 1;
  height: 78rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}
.goal-input-btn.ghost {
  background: var(--bg-secondary);
  color: var(--text-secondary);
}
.goal-input-btn.primary {
  background: linear-gradient(135deg, var(--accent-green), var(--accent-blue));
  color: #fff;
}
/* ==================== 室内运动背景 ==================== */
.indoor-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  z-index: 0;
  overflow: hidden;
}
.indoor-bg-circle {
  position: absolute;
  border-radius: 50%;
  border: 2rpx solid var(--metric-border);
  &.c1 {
    width: 600rpx;
    height: 600rpx;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -60%);
    animation: pulse-ring 4s ease-in-out infinite;
  }
  &.c2 {
    width: 900rpx;
    height: 900rpx;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -60%);
    animation: pulse-ring 4s ease-in-out infinite 1s;
  }
  &.c3 {
    width: 1200rpx;
    height: 1200rpx;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -60%);
    animation: pulse-ring 4s ease-in-out infinite 2s;
  }
}
@keyframes pulse-ring {
  0%, 100% { opacity: 0.3; transform: translate(-50%, -60%) scale(1); }
  50% { opacity: 0.8; transform: translate(-50%, -60%) scale(1.05); }
}
/* ==================== 锁定遮罩 ==================== */
.lock-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.lock-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}
.lock-hint-icon {
  font-size: 80rpx;
}
.lock-hint-text {
  font-size: 36rpx;
  color: #fff;
  font-weight: 600;
}
.lock-hint-sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.6);
}
/* ==================== 运动中浮层 ==================== */
.data-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 60rpx 40rpx 0;
  pointer-events: none;
  transition: opacity 0.3s;
}
.data-overlay.overlay-paused {
  opacity: 0.4;
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
.gps-indicator.ok .gps-ind-dot { background: var(--accent-green); }
.gps-indicator.fair .gps-ind-dot { background: #fbbf24; }
.gps-indicator.weak .gps-ind-dot { background: #ef4444; }
.gps-ind-text { font-size: 20rpx; color: rgba(255, 255, 255, 0.8); }
.data-main { text-align: center; margin-bottom: 40rpx; }
.data-distance-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10rpx;
}
.data-distance { font-size: 140rpx; font-weight: 300; color: #fff; letter-spacing: -4rpx; line-height: 1; }
.data-dist-unit { font-size: 36rpx; color: rgba(255, 255, 255, 0.6); }
.data-pace-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8rpx;
  margin-top: 16rpx;
}
.data-pace { font-size: 44rpx; font-weight: 500; color: var(--accent-green); }
.data-pace-label { font-size: 24rpx; color: rgba(74, 222, 128, 0.7); }
.data-avg-pace-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8rpx;
  margin-top: 8rpx;
}
.data-avg-pace { font-size: 32rpx; font-weight: 400; color: rgba(255, 255, 255, 0.8); }
.data-avg-pace-label { font-size: 22rpx; color: rgba(255, 255, 255, 0.45); }
.data-grid {
  display: flex;
  justify-content: space-around;
  text-align: center;
  margin-bottom: 20rpx;
}
.data-grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120rpx;
}
.data-grid-val { font-size: 28rpx; font-weight: 600; color: #fff; }
.data-grid-lbl { font-size: 20rpx; color: rgba(255, 255, 255, 0.4); margin-top: 6rpx; }
.indoor-grid {
  justify-content: center;
  gap: 80rpx;
}
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
  background: var(--accent-green);
  border-radius: 999rpx;
  transition: width 0.5s;
}
.progress-text {
  margin-top: 8rpx;
  display: flex;
  justify-content: center;
}
.progress-text-inner {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.5);
}
/* ==================== 暂停浮层 ==================== */
.pause-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.pause-hint {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 24rpx;
  padding: 32rpx 56rpx;
  text-align: center;
}
.pause-hint-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  display: block;
}
.pause-hint-duration {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8rpx;
  display: block;
}
/* ==================== 底部悬浮面板 ==================== */
.bottom-float-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  background: rgba(15, 23, 42, 0.88);
  backdrop-filter: blur(16px);
  border-radius: 32rpx 32rpx 0 0;
  padding: 28rpx 32rpx calc(28rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.2);
}
.panel-progress {
  margin-bottom: 16rpx;
}
.panel-progress-track {
  width: 100%;
  height: 6rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 999rpx;
  overflow: hidden;
}
.panel-progress-fill {
  height: 100%;
  background: var(--accent-green);
  border-radius: 999rpx;
  transition: width 0.5s;
}
.panel-progress-text {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.45);
  text-align: center;
  margin-top: 8rpx;
}
.panel-data-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  margin-bottom: 24rpx;
}
.panel-data-item {
  width: 33.33%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12rpx 0;
}
.panel-data-val {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}
.panel-data-lbl {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 6rpx;
}
.panel-btn-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-top: 8rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.08);
}
.panel-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}
.panel-btn.main {
  transform: scale(1.1);
}
.panel-btn-circle {
  width: 82rpx;
  height: 82rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.18);
}
.panel-btn-circle.end {
  background: rgba(239, 68, 68, 0.18);
  border-color: rgba(239, 68, 68, 0.25);
}
.panel-btn-circle.next {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-color: rgba(255, 255, 255, 0.22);
}
.panel-btn-circle.main {
  width: 96rpx;
  height: 96rpx;
  background: var(--bg-card);
  border: none;
  box-shadow: 0 4rpx 20rpx rgba(255, 255, 255, 0.12);
}
.panel-btn-icon {
  font-size: 34rpx;
  color: #fff;
  font-weight: 800;
}
.panel-btn-circle.end .panel-btn-icon {
  color: #ef4444;
  font-size: 26rpx;
}
.panel-btn-circle.next .panel-btn-icon {
  color: #fff;
  font-size: 28rpx;
  line-height: 1;
}
.panel-btn-circle.main .panel-btn-icon {
  color: var(--accent-green);
  font-size: 30rpx;
  font-weight: 800;
}
.panel-btn-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.85);
}
/* 非GPS活动的浅色面板 */
.indoor-panel {
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.1);
}
.indoor-panel .panel-data-val {
  color: #1c1c1e;
}
.indoor-panel .panel-data-lbl {
  color: #94a3b8;
}
.indoor-panel .panel-btn-circle {
  background: var(--bg-secondary);
  border-color: #e5e5e5;
}
.indoor-panel .panel-btn-circle.end {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.2);
}
.indoor-panel .panel-btn-circle.next {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-color: rgba(37, 99, 235, 0.18);
}
.indoor-panel .panel-btn-circle.main {
  background: linear-gradient(135deg, var(--accent-green), var(--accent-green-dark));
  box-shadow: 0 4rpx 20rpx var(--accent-glow);
}
.indoor-panel .panel-btn-circle.main .panel-btn-icon {
  color: #fff;
}
.indoor-panel .panel-btn-label {
  color: var(--text-primary);
}
.indoor-panel .panel-btn-row {
  border-top-color: #f0f0f0;
}
.indoor-panel .panel-progress-track {
  background: rgba(0, 0, 0, 0.08);
}
.indoor-panel .panel-progress-text {
  color: #94a3b8;
}
/* ==================== 总结阶段 ==================== */
.summary-scroll { height: 100vh; background: var(--bg-primary); }
.summary-map-area {
  width: 100%;
  height: 40vh;
  position: relative;
}
.summary-map-badge {
  position: absolute;
  top: 24rpx;
  left: 24rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  padding: 8rpx 20rpx;
  border-radius: 16rpx;
}
.map-badge-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 500;
}
.summary-indoor-header {
  background: linear-gradient(135deg, var(--accent-green) 0%, var(--accent-green-dark) 100%);
  padding: 80rpx 40rpx 60rpx;
  text-align: center;
}
.summary-indoor-name {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  display: block;
}
.summary-indoor-date {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 12rpx;
  display: block;
}
.summary-card {
  background: var(--bg-card);
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
.hero-dist-unit { font-size: 28rpx; color: var(--text-tertiary); }
.summary-hero-date { font-size: 24rpx; color: var(--text-tertiary); }
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
}
.summary-stat-row.second-row {
  border-top: none;
  border-bottom: 1rpx solid #f0f0f0;
  padding-top: 16rpx;
}
.summary-stat-item { text-align: center; flex: 1; }
.stat-item-num { font-size: 36rpx; font-weight: 600; color: var(--text-primary); display: block; }
.stat-item-label { font-size: 22rpx; color: var(--text-tertiary); margin-top: 4rpx; display: block; }
.summary-goal-banner {
  margin-top: 24rpx;
  padding: 20rpx;
  background: var(--soft-green);
  border-radius: 12rpx;
  text-align: center;
  font-size: 26rpx;
  color: var(--accent-green-dark);
  font-weight: 500;
}
/* ==================== 分段配速 ==================== */
.splits-card {
  margin: 20rpx;
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.splits-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24rpx;
  display: block;
}
.splits-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.split-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.split-num {
  width: 48rpx;
  font-size: 24rpx;
  color: var(--text-tertiary);
  text-align: center;
  flex-shrink: 0;
}
.split-bar-wrap {
  flex: 1;
  height: 24rpx;
  background: var(--bg-secondary);
  border-radius: 12rpx;
  overflow: hidden;
}
.split-bar {
  height: 100%;
  background: var(--accent-green);
  border-radius: 12rpx;
  transition: width 0.5s ease;
  min-width: 20%;
}
.split-bar.best {
  background: var(--accent-green);
}
.split-bar.worst {
  background: #fbbf24;
}
.split-pace {
  width: 80rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-primary);
  text-align: right;
  flex-shrink: 0;
}
.split-dist {
  width: 60rpx;
  font-size: 20rpx;
  color: var(--text-tertiary);
  text-align: right;
  flex-shrink: 0;
}
/* ==================== 操作 ==================== */
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
  &.primary { background: linear-gradient(135deg, var(--accent-green), var(--accent-green-dark)); color: #fff; box-shadow: 0 4rpx 16rpx var(--accent-glow); }
  &.secondary { background: var(--bg-secondary); color: var(--text-secondary); }
  &:active { opacity: 0.85; }
}
.summary-spacer { height: 40rpx; }
</style>
