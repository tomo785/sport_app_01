<template>
  <view class="container">
    <view class="status-bar-placeholder" :style="{ height: statusBarHeight + 'px' }"></view>
    <!-- 训练状态卡片 -->
    <TrainingStatusCard />

    <!-- 打卡日历 -->
    <view class="calendar-card">
      <view class="calendar-header">
        <view class="month-nav" @click="changeView(-1)">
          <text class="nav-arrow">‹</text>
        </view>
        <text class="month-title">{{ currentYear }}年{{ currentMonth }}月</text>
        <view class="month-nav" @click="changeView(1)">
          <text class="nav-arrow">›</text>
        </view>
      </view>
      <view class="week-row">
        <text class="week-cell" v-for="w in weekDays" :key="w">{{ w }}</text>
      </view>
      <view class="day-grid">
        <view
          class="day-cell"
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="{
            'other-month': !day.isCurrentMonth,
            'today': day.isToday,
            'selected': isSelectedDay(day),
            'has-record': day.hasRecord
          }"
          @click="selectDay(day)"
        >
          <text class="day-num">{{ day.day }}</text>
          <text class="day-steps" v-if="day.steps > 0">{{ day.steps > 999 ? (day.steps/1000).toFixed(1)+'k' : day.steps }}</text>
          <view class="day-dot" v-if="day.hasRecord"></view>
        </view>
      </view>
      <view class="calendar-footer">
        <text class="back-today" v-if="!isCurrentMonthView" @click="backToToday">回到今天</text>
      </view>
    </view>

    <!-- 近期运动记录 -->
    <view class="record-section">
      <view class="section-header">
        <text class="section-title">近7天运动</text>
        <text class="more-link" @click="goToWorkoutList">查看全部 ›</text>
      </view>

      <view class="record-list" v-if="filteredRecords.length > 0">
        <view
          class="record-item"
          v-for="item in filteredRecords"
          :key="item.id"
          @click="goToWorkoutDetail(item.id)"
        >
          <view class="record-icon" :class="'type-' + item.type">
            <text>{{ getTypeIcon(item.type) }}</text>
          </view>
          <view class="record-info">
            <text class="record-name">{{ item.typeName || getTypeName(item.type) }}</text>
            <text class="record-time">{{ formatTime(item.startTime) }}</text>
          </view>
          <view class="record-stats">
            <text class="record-duration">{{ formatDuration(item.duration) }}</text>
            <text class="record-distance" v-if="item.distance">{{ formatDistance(item.distance) }}</text>
            <text class="record-calories" v-if="item.calories">{{ item.calories }}千卡</text>
          </view>
        </view>
      </view>

      <view class="empty" v-else>
        <text>近7天暂无运动记录</text>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTodayStats } from '@/api/stats'
import { getDailyStatsList } from '@/api/stats'
import { getWorkoutList } from '@/api/workout'
import { formatDate, formatDuration, formatDistance } from '@/utils'
import TrainingStatusCard from '@/components/TrainingStatusCard.vue'

// ===================== State =====================
const statusBarHeight = ref(20)
const todayStats = ref({})
const workoutRecords = ref([])
const recordDates = ref(new Set())

// 日历状态
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const selectedDate = ref('') // 格式 YYYY-MM-DD
const stepsMap = ref({}) // { 'YYYY-MM-DD': steps }

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// ===================== 计算属性 =====================
const isCurrentMonthView = computed(() => {
  const now = new Date()
  return currentYear.value === now.getFullYear() && currentMonth.value === now.getMonth() + 1
})

const selectedDateStr = computed(() => {
  if (!selectedDate.value) return ''
  const d = new Date(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const filteredRecords = computed(() => {
  const now = new Date()
  const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  return workoutRecords.value.filter(item => {
    const timeStr = item.startTimeStr || item.startTime || ''
    if (!timeStr) return false
    const itemDate = new Date(timeStr)
    return itemDate >= sevenDaysAgo && itemDate <= now
  }).slice(0, 7)
})

const calendarDays = computed(() => {
  const todayStr = formatDate(new Date(), 'YYYY-MM-DD')
  const year = currentYear.value
  const month = currentMonth.value
  const firstDayOfMonth = new Date(year, month - 1, 1)
  const lastDayOfMonth = new Date(year, month, 0)
  const daysInMonth = lastDayOfMonth.getDate()
  const startWeekDay = firstDayOfMonth.getDay()

  const buildDay = (y, m, d, isCurrent) => {
    const dateStr = formatDate(new Date(y, m - 1, d), 'YYYY-MM-DD')
    return {
      day: d, isCurrentMonth: isCurrent, isToday: dateStr === todayStr,
      dateStr, hasRecord: recordDates.value.has(dateStr),
      steps: stepsMap.value[dateStr] || 0
    }
  }

  const days = []

  // 上月末尾日期
  const prevMonthLastDay = new Date(year, month - 1, 0).getDate()
  for (let i = startWeekDay - 1; i >= 0; i--) {
    days.push(buildDay(year, month - 1, prevMonthLastDay - i, false))
  }

  // 当月日期
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(buildDay(year, month, i, true))
  }

  // 下月开头日期
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push(buildDay(year, month + 1, i, false))
  }

  return days
})

// ===================== 方法 =====================
const isSelectedDay = (day) => {
  return selectedDate.value === day.dateStr
}

const selectDay = (day) => {
  selectedDate.value = day.dateStr
  if (!day.isCurrentMonth) {
    if (day.day > 20) {
      changeMonth(-1)
    } else {
      changeMonth(1)
    }
  }
  const d = new Date(day.dateStr)
  currentYear.value = d.getFullYear()
  currentMonth.value = d.getMonth() + 1
}

const changeMonth = (delta) => {
  let newMonth = currentMonth.value + delta
  let newYear = currentYear.value
  if (newMonth > 12) {
    newMonth = 1
    newYear++
  } else if (newMonth < 1) {
    newMonth = 12
    newYear--
  }
  currentMonth.value = newMonth
  currentYear.value = newYear
  loadStepsData(newYear, newMonth)
}

const changeView = (delta) => {
  changeMonth(delta)
}

const backToToday = () => {
  const now = new Date()
  currentYear.value = now.getFullYear()
  currentMonth.value = now.getMonth() + 1
  selectedDate.value = formatDate(now, 'YYYY-MM-DD')
  loadStepsData(currentYear.value, currentMonth.value)
}

// ===================== 步数数据加载 =====================
const loadStepsData = async (year, month) => {
  try {
    const firstDay = `${year}-${String(month).padStart(2, '0')}-01`
    const lastDayDate = new Date(year, month, 0)
    const lastDay = `${year}-${String(month).padStart(2, '0')}-${String(lastDayDate.getDate()).padStart(2, '0')}`
    const res = await getDailyStatsList(firstDay, lastDay)
    if (res.code === 200 && res.data) {
      const map = {}
      const list = Array.isArray(res.data) ? res.data : (res.data.list || [])
      list.forEach(item => {
        const dateStr = item.statDate || item.date || ''
        const steps = item.steps || item.stepCount || 0
        if (dateStr && steps > 0) map[dateStr] = steps
      })
      stepsMap.value = map
    }
  } catch (e) {
    console.warn('[日历步数] 加载失败:', e)
  }
}

// ===================== 数据加载 =====================
const loadTodayStats = async () => {
  try {
    const res = await getTodayStats()
    if (res.code === 200) {
      todayStats.value = res.data || {}
    }
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
}

const loadWorkoutRecords = async () => {
  try {
    const now = new Date()
    const startDate = formatDate(new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000), 'YYYY-MM-DD')
    const endDate = formatDate(now, 'YYYY-MM-DD')
    const res = await getWorkoutList({ page: 1, size: 100, startDate, endDate })

    if (res.code === 200 && res.data) {
      workoutRecords.value = res.data.list || []

      // 提取有记录的日期
      const dates = new Set()
      workoutRecords.value.forEach(item => {
        const timeStr = item.startTimeStr || item.startTime || ''
        if (timeStr) {
          dates.add(timeStr.substring(0, 10))
        }
      })
      recordDates.value = dates
    }
  } catch (error) {
    console.error('加载运动记录失败:', error)
  }
}

// ===================== 工具函数 =====================
const getTypeIcon = (type) => {
  const map = { 1: '🏃', 2: '🚶', 3: '🚴' }
  return map[type] || '🏃'
}

const getTypeName = (type) => {
  const map = { 1: '跑步', 2: '健走', 3: '骑行' }
  return map[type] || '运动'
}

const formatTime = (time) => {
  if (!time) return ''
  const str = typeof time === 'string' ? time : ''
  return str.substring(0, 16).replace('T', ' ')
}

const goToWorkoutDetail = (id) => {
  uni.navigateTo({ url: `/pages/workout/detail?id=${id}` })
}

const goToWorkoutList = () => {
  uni.navigateTo({ url: '/pages/workout/workout' })
}

// ===================== 生命周期 =====================
onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
  selectedDate.value = formatDate(new Date(), 'YYYY-MM-DD')
  loadTodayStats()
  loadWorkoutRecords()
  loadStepsData(currentYear.value, currentMonth.value)
})

onShow(() => {
  loadTodayStats()
  loadWorkoutRecords()
  loadStepsData(currentYear.value, currentMonth.value)
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-top: 20rpx;
  padding-bottom: 40rpx;
}

.status-bar-placeholder {
  width: 100%;
  flex-shrink: 0;
}

/* ===================== 日历卡片 ===================== */
.calendar-card {
  margin: 0 28rpx 20rpx;
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.month-nav {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  border-radius: 50%;

  &:active {
    background: var(--border-color);
  }
}

.nav-arrow {
  font-size: 32rpx;
  color: #22c55e;
  font-weight: bold;
}

.month-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.week-row {
  display: flex;
  margin-bottom: 12rpx;
}

.week-cell {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: var(--text-tertiary);
  padding: 10rpx 0;
}

.day-grid {
  display: flex;
  flex-wrap: wrap;
}

.day-cell {
  width: calc(100% / 7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14rpx 0;
  position: relative;

  .day-num {
    font-size: 28rpx;
    color: var(--text-primary);
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
  }

  .day-dot {
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: #22c55e;
    margin-top: 2rpx;
  }

  .day-steps {
    font-size: 18rpx;
    color: #94a3b8;
    margin-top: 2rpx;
    line-height: 1;
  }

  &.other-month {
    .day-num {
      color: var(--text-tertiary);
    }
    .day-steps {
      color: var(--text-tertiary);
    }
  }

  &.today {
    .day-num {
      background: #f0fdf4;
      color: #16a34a;
      font-weight: 600;
    }
  }

  &.selected {
    .day-num {
      background: #22c55e;
      color: #fff;
      font-weight: 600;
    }
  }

  &.has-record:not(.selected) {
    .day-num {
      color: #22c55e;
      font-weight: 600;
    }
  }
}

.calendar-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid var(--border-color);
}

.back-today {
  font-size: 24rpx;
  color: #22c55e;
  font-weight: 500;
}

/* ===================== 记录列表 ===================== */
.record-section {
  margin: 0 28rpx;
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.more-link {
  font-size: 26rpx;
  color: #22c55e;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: var(--bg-secondary);
  border-radius: 16rpx;

  &:active {
    background: #f1f5f9;
  }
}

.record-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 20rpx;
  background: #f0f0f0;

  &.type-1 { background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); }
  &.type-2 { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
  &.type-3 { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;

  .record-name {
    font-size: 30rpx;
    font-weight: 600;
    color: var(--text-primary);
  }

  .record-time {
    font-size: 24rpx;
    color: var(--text-tertiary);
  }
}

.record-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6rpx;

  .record-duration {
    font-size: 28rpx;
    font-weight: 600;
    color: #22c55e;
  }

  .record-distance,
  .record-calories {
    font-size: 22rpx;
    color: var(--text-tertiary);
  }
}

.empty {
  padding: 60rpx 0;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 28rpx;
}
</style>
