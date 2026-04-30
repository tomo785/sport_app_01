<template>
  <view class="task-card">
    <!-- 头部标题 -->
    <view class="card-header">
      <view class="header-left">
        <text class="card-title">训练计划</text>
        <text class="card-date">{{ currentDate }}</text>
      </view>
      <view class="header-more" @click="onMoreClick">
        <text class="more-icon">⋯</text>
      </view>
    </view>

    <!-- 上区块：计划展示区 -->
    <view class="plan-section">
      <!-- 第一行：3个卡片 -->
      <view class="cards-row-1">
        <!-- 今日运动计划 -->
        <view class="card today-plan-card" @click="onPlanClick">
          <view class="card-icon-wrapper">
            <text class="card-icon">💪</text>
            <text class="card-label">今日计划</text>
          </view>
          <view class="plan-content">
            <text class="plan-title">{{ todayTask.title || '今日训练' }}</text>
            <text class="plan-subtitle">{{ todayTask.subtitle || '上肢力量训练' }}</text>
            <view class="exercise-list">
              <view class="exercise-item" v-for="(item, index) in displayExercises" :key="index">
                <text class="exercise-dot">•</text>
                <text class="exercise-text">{{ item.exerciseName }} {{ item.setsPlanned }}组×{{ item.repsPlanned || item.durationPlanned + '秒' }}</text>
              </view>
            </view>
            <view class="rest-time" v-if="todayTask.restTime">
              <text class="rest-icon">⏱</text>
              <text class="rest-text">组间休息 {{ todayTask.restTime }}秒</text>
            </view>
          </view>
        </view>

        <!-- 训练进度条 -->
        <view class="card progress-card" @click="onProgressClick">
          <view class="progress-ring-wrapper">
            <view class="progress-ring">
              <view class="progress-circle" :style="progressCircleStyle">
                <text class="progress-percent">{{ todayTask.progress || 0 }}%</text>
              </view>
            </view>
          </view>
          <view class="progress-info">
            <text class="progress-tasks">今日任务：{{ todayTask.completedExercises || 0 }}/{{ todayTask.totalExercises || 0 }}项</text>
            <text class="progress-time">剩余时长：{{ todayTask.remainingTime || 0 }}分钟</text>
          </view>
        </view>

        <!-- 数据看板 -->
        <view class="card stats-card">
          <view class="stat-item">
            <view class="stat-icon heart">❤️</view>
            <view class="stat-info">
              <text class="stat-value">{{ dataBoard.heartRate || 0 }}</text>
              <text class="stat-label">心率/分</text>
            </view>
          </view>
          <view class="stat-item">
            <view class="stat-icon fire">🔥</view>
            <view class="stat-info">
              <text class="stat-value">{{ dataBoard.caloriesBurned || 0 }}</text>
              <text class="stat-label">大卡</text>
            </view>
          </view>
          <view class="stat-item">
            <view class="stat-icon chart">📊</view>
            <view class="stat-info">
              <text class="stat-value">{{ dataBoard.completionRate || 0 }}%</text>
              <text class="stat-label">训练率</text>
            </view>
          </view>
          <view class="stat-item">
            <view class="stat-icon target">🎯</view>
            <view class="stat-info">
              <text class="stat-value">{{ dataBoard.goalProgress || 0 }}%</text>
              <text class="stat-label">目标度</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 第二行：3个卡片 -->
      <view class="cards-row-2">
        <!-- 7天训练趋势 -->
        <view class="card trend-card">
          <text class="trend-title">本周趋势</text>
          <view class="trend-chart">
            <view class="trend-bar-wrapper" v-for="(item, index) in weeklyTrend" :key="index">
              <view class="trend-bar" :style="{ height: calculateBarHeight(item.duration) + '%' }"></view>
              <text class="trend-date">{{ item.dateLabel }}</text>
            </view>
          </view>
        </view>

        <!-- 智能穿戴对接 -->
        <view class="card device-card" @click="onDeviceClick">
          <view class="device-icon">⌚</view>
          <text class="device-status">{{ deviceStatus }}</text>
          <text class="device-name">{{ deviceName }}</text>
          <view class="sync-btn" @click.stop="onSyncClick">
            <text class="sync-text">{{ syncText }}</text>
          </view>
        </view>

        <!-- 训练提醒/倒计时 -->
        <view class="card reminder-card">
          <view class="reminder-header">
            <text class="reminder-icon">⏰</text>
            <text class="reminder-label">训练提醒</text>
          </view>
          <view class="reminder-content">
            <text class="next-exercise">下一组：{{ nextExerciseName }}</text>
            <text class="countdown">{{ formatCountdown }}</text>
            <text class="countdown-label">休息倒计时</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 开始训练按钮 -->
    <view class="action-section" v-if="todayTask.status === 0">
      <button class="start-btn" @click="onStartTask">开始训练</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getTaskDashboard, startTodayTask } from '../api/task'

const props = defineProps({
  // 可以传入外部数据
})

const emit = defineEmits(['more', 'plan-click', 'progress-click', 'device-click', 'start'])

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
})

// 任务数据
const todayTask = ref({
  title: '上肢力量训练',
  subtitle: '强化胸肌与三头肌',
  totalExercises: 3,
  completedExercises: 0,
  progress: 0,
  remainingTime: 45,
  restTime: 60,
  exercises: []
})

// 数据看板
const dataBoard = ref({
  heartRate: 135,
  caloriesBurned: 428,
  completionRate: 65,
  goalProgress: 72
})

// 7天趋势数据
const weeklyTrend = ref([])

// 设备状态
const deviceConnected = ref(true)
const deviceName = ref('Apple Watch')
const syncStatus = ref('idle') // idle, syncing, synced

const deviceStatus = computed(() => deviceConnected.value ? '● 已连接' : '○ 未连接')
const syncText = computed(() => {
  switch (syncStatus.value) {
    case 'syncing': return '同步中...'
    case 'synced': return '已同步'
    default: return '同步数据'
  }
})

// 显示的示例动作
const displayExercises = computed(() => {
  if (todayTask.value.exercises && todayTask.value.exercises.length > 0) {
    return todayTask.value.exercises.slice(0, 3)
  }
  // 默认示例数据
  return [
    { exerciseName: '俯卧撑', setsPlanned: 3, repsPlanned: 15 },
    { exerciseName: '哑铃卧推', setsPlanned: 4, repsPlanned: 12 },
    { exerciseName: '平板支撑', setsPlanned: 3, durationPlanned: 60 }
  ]
})

// 下一组动作
const nextExerciseName = computed(() => {
  if (todayTask.value.exercises && todayTask.value.exercises.length > 0) {
    const next = todayTask.value.exercises.find(e => e.status === 0)
    return next ? next.exerciseName : '训练完成'
  }
  return '哑铃卧推'
})

// 倒计时
const countdown = ref(60)
let countdownTimer = null

const formatCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 进度环样式
const progressCircleStyle = computed(() => {
  const progress = todayTask.value.progress || 0
  const angle = (progress / 100) * 360
  return {
    background: `conic-gradient(#3b82f6 0deg, #8b5cf6 ${angle}deg, #e5e7eb ${angle}deg, #e5e7eb 360deg)`
  }
})

// 计算柱状图高度
function calculateBarHeight(duration) {
  const maxDuration = Math.max(...weeklyTrend.value.map(item => item.duration), 60)
  return maxDuration > 0 ? (duration / maxDuration) * 100 : 0
}

// 加载数据
async function loadDashboard() {
  try {
    const res = await getTaskDashboard()
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.todayTask) {
        todayTask.value = data.todayTask
      }
      if (data.dataBoard) {
        dataBoard.value = data.dataBoard
      }
      if (data.weeklyTrend) {
        weeklyTrend.value = data.weeklyTrend
      }
    }
  } catch (e) {
    console.error('加载任务看板失败:', e)
    // 使用默认数据
    initDefaultTrend()
  }
}

// 初始化默认趋势数据
function initDefaultTrend() {
  const dates = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const durations = [30, 45, 35, 60, 50, 70, 45]
  weeklyTrend.value = dates.map((date, index) => ({
    dateLabel: date,
    duration: durations[index]
  }))
}

// 事件处理
function onMoreClick() {
  emit('more')
}

function onPlanClick() {
  emit('plan-click')
}

function onProgressClick() {
  emit('progress-click')
}

function onDeviceClick() {
  emit('device-click')
}

async function onSyncClick() {
  if (syncStatus.value === 'syncing') return
  
  syncStatus.value = 'syncing'
  setTimeout(() => {
    syncStatus.value = 'synced'
    setTimeout(() => {
      syncStatus.value = 'idle'
    }, 2000)
  }, 1500)
}

async function onStartTask() {
  try {
    await startTodayTask()
    todayTask.value.status = 1
    emit('start')
    uni.showToast({ title: '开始训练', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '开始失败', icon: 'none' })
  }
}

// 倒计时
function startCountdown() {
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      countdown.value = todayTask.value.restTime || 60
    }
  }, 1000)
}

// 生命周期
onMounted(() => {
  loadDashboard()
  startCountdown()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

// 暴露刷新方法
defineExpose({
  refresh: loadDashboard
})
</script>

<style lang="scss" scoped>
.task-card {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 24px;
  padding: 16px;
  margin: 10px;
  height: 50vh;
  max-height: 50vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

// 计划展示区 - 可滚动
.plan-section {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

// 头部
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;

  .header-left {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .card-title {
    font-size: 20px;
    font-weight: 700;
    color: #1c1c1e;
  }

  .card-date {
    font-size: 12px;
    color: #64748b;
  }

  .header-more {
    width: 32px;
    height: 32px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .more-icon {
      font-size: 18px;
      color: #1c1c1e;
      font-weight: bold;
    }
  }
}

// 第一行卡片
.cards-row-1 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  margin-bottom: 8px;
}

// 卡片基础样式
.card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
}

// 今日计划卡片
.today-plan-card {
  .card-icon-wrapper {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 6px;
  }

  .card-icon {
    font-size: 16px;
  }

  .card-label {
    font-size: 11px;
    font-weight: 600;
    color: #1c1c1e;
  }

  .plan-content {
    .plan-title {
      font-size: 12px;
      font-weight: 600;
      color: #3b82f6;
      margin-bottom: 2px;
    }

    .plan-subtitle {
      font-size: 10px;
      color: #64748b;
      margin-bottom: 4px;
    }
  }

  .exercise-list {
    .exercise-item {
      display: flex;
      align-items: flex-start;
      gap: 2px;
      margin-bottom: 1px;

      .exercise-dot {
        color: #3b82f6;
        font-size: 9px;
      }

      .exercise-text {
        font-size: 9px;
        color: #475569;
        line-height: 1.3;
      }
    }
  }

  .rest-time {
    display: flex;
    align-items: center;
    gap: 2px;
    margin-top: 4px;

    .rest-icon {
      font-size: 9px;
    }

    .rest-text {
      font-size: 8px;
      color: #f97316;
    }
  }
}

// 进度卡片
.progress-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .progress-ring-wrapper {
    margin-bottom: 6px;
  }

  .progress-ring {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    padding: 5px;
    background: white;

    .progress-circle {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        width: 85%;
        height: 85%;
        background: white;
        border-radius: 50%;
      }

      .progress-percent {
        position: relative;
        font-size: 14px;
        font-weight: 700;
        color: #1c1c1e;
      }
    }
  }

  .progress-info {
    text-align: center;

    .progress-tasks {
      font-size: 10px;
      font-weight: 600;
      color: #1c1c1e;
      margin-bottom: 2px;
    }

    .progress-time {
      font-size: 8px;
      color: #64748b;
    }
  }
}

// 数据看板卡片
.stats-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .stat-icon {
      width: 22px;
      height: 22px;
      border-radius: 5px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;

      &.heart { background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); }
      &.fire { background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%); }
      &.chart { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
      &.target { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
    }

    .stat-info {
      .stat-value {
        font-size: 12px;
        font-weight: 700;
        color: #1c1c1e;
      }

      .stat-label {
        font-size: 7px;
        color: #94a3b8;
      }
    }
  }
}

// 第二行卡片
.cards-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}

// 趋势卡片
.trend-card {
  .trend-title {
    font-size: 11px;
    font-weight: 600;
    color: #1c1c1e;
    margin-bottom: 6px;
  }

  .trend-chart {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    height: 50px;
    padding: 0 2px;

    .trend-bar-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 3px;

      .trend-bar {
        width: 6px;
        background: linear-gradient(180deg, #3b82f6 0%, #60a5fa 100%);
        border-radius: 3px;
        min-height: 3px;
        transition: height 0.3s ease;
      }

      .trend-date {
        font-size: 7px;
        color: #94a3b8;
      }
    }
  }
}

// 设备卡片
.device-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  .device-icon {
    font-size: 24px;
    margin-bottom: 4px;
  }

  .device-status {
    font-size: 10px;
    font-weight: 600;
    color: #10b981;
    margin-bottom: 1px;
  }

  .device-name {
    font-size: 8px;
    color: #64748b;
    margin-bottom: 6px;
  }

  .sync-btn {
    background: rgba(59, 130, 246, 0.1);
    padding: 3px 8px;
    border-radius: 8px;

    .sync-text {
      font-size: 8px;
      color: #3b82f6;
      font-weight: 500;
    }
  }
}

// 提醒卡片
.reminder-card {
  .reminder-header {
    display: flex;
    align-items: center;
    gap: 3px;
    margin-bottom: 6px;

    .reminder-icon {
      font-size: 12px;
    }

    .reminder-label {
      font-size: 10px;
      font-weight: 600;
      color: #1c1c1e;
    }
  }

  .reminder-content {
    text-align: center;

    .next-exercise {
      font-size: 10px;
      font-weight: 600;
      color: #1c1c1e;
      margin-bottom: 4px;
    }

    .countdown {
      font-size: 18px;
      font-weight: 700;
      color: #f97316;
      font-variant-numeric: tabular-nums;
      letter-spacing: -1px;
    }

    .countdown-label {
      font-size: 8px;
      color: #94a3b8;
      margin-top: 2px;
    }
  }
}

// 操作区域
.action-section {
  margin-top: 10px;
  flex-shrink: 0;

  .start-btn {
    width: 100%;
    height: 40px;
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: white;
    font-size: 14px;
    font-weight: 600;
    border-radius: 12px;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }
  }
}
</style>
