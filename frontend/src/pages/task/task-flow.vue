<template>
  <view class="container">
    <view class="status-bar-placeholder" :style="{ height: statusBarHeight + 'px' }"></view>
    <!-- 训练状态卡片 -->
    <TrainingStatusCard />
    <!-- 打卡日历 -->
    <view class="calendar-card">
      <view class="calendar-header">
        <view class="month-nav" @click="changeView(-1)">
          <AppIcon class="nav-arrow" name="arrowLeft" size="28" bold />
        </view>
        <text class="month-title">{{ currentYear }}年{{ currentMonth }}月</text>
        <view class="month-nav" @click="changeView(1)">
          <AppIcon class="nav-arrow" name="arrowRight" size="28" bold />
        </view>
      </view>
      <scroll-view
        class="calendar-scroll"
        scroll-x
        :show-scrollbar="false"
        scroll-with-animation
        :scroll-left="calendarScrollLeft"
      >
        <view class="day-strip">
        <view
          :id="day.id"
          class="day-cell"
          v-for="(day, index) in calendarDays"
          :key="day.dateStr"
          :class="{
            'other-month': !day.isCurrentMonth,
            'today': day.isToday,
            'selected': isSelectedDay(day),
            'has-record': day.hasRecord
          }"
          @click="selectDay(day)"
        >
          <text class="day-week">{{ day.week }}</text>
          <text class="day-num">{{ day.day }}</text>
        </view>
        </view>
      </scroll-view>
      <view class="calendar-footer">
        <text class="selected-day">{{ selectedDateStr || '选择日期' }}</text>
        <text class="selected-steps" v-if="selectedDateSteps">{{ selectedDateSteps }}</text>
        <text class="back-today" v-if="!isCurrentMonthView" @click="backToToday">回到今天</text>
      </view>
    </view>
    <!-- 近期运动记录 -->
    <view class="record-section">
      <view class="section-header">
        <text class="section-title">最近运动</text>
        <view class="more-link" @click="goToWorkoutList">
          <text>查看全部</text>
          <AppIcon name="arrowRight" size="22" bold />
        </view>
      </view>
      <scroll-view class="record-scroll" scroll-y v-if="filteredRecords.length > 0">
        <view class="record-list">
          <view
            class="record-item"
            v-for="item in filteredRecords"
            :key="item.id"
            @click="goToWorkoutDetail(item.id)"
          >
            <view class="record-icon" :class="'type-' + item.type">
              <AppIcon :name="getTypeIcon(item.type)" size="30" bold />
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
      </scroll-view>
      <view class="empty" v-else>
        <text>暂无运动记录</text>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
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
const calendarScrollLeft = ref(0)
// ===================== 计算属性 =====================
const isCurrentMonthView = computed(() => {
  const now = new Date()
  return currentYear.value === now.getFullYear() && currentMonth.value === now.getMonth() + 1
})
const selectedDateStr = computed(() => {
  if (!selectedDate.value) return ''
  const [, month, day] = selectedDate.value.split('-')
  return `${Number(month)}月${Number(day)}日`
})
const selectedDateSteps = computed(() => {
  if (!selectedDate.value) return ''
  const steps = stepsMap.value[selectedDate.value] || 0
  if (!steps) return ''
  const formatted = steps > 999 ? `${(steps / 1000).toFixed(1)}k` : steps
  return `${formatted}步`
})
const filteredRecords = computed(() => {
  return workoutRecords.value.slice(0, 12)
})
const calendarDays = computed(() => {
  const todayStr = formatDate(new Date(), 'YYYY-MM-DD')
  const year = currentYear.value
  const month = currentMonth.value
  const buildDay = (y, m, d, isCurrentMonth) => {
    const dateStr = formatDate(new Date(y, m - 1, d), 'YYYY-MM-DD')
    const week = weekDays[new Date(y, m - 1, d).getDay()]
    return {
      id: `cal-${dateStr}`,
      day: d, week, isCurrentMonth, isToday: dateStr === todayStr,
      dateStr, hasRecord: recordDates.value.has(dateStr),
      steps: stepsMap.value[dateStr] || 0
    }
  }
  const days = []
  for (let offset = -1; offset <= 1; offset++) {
    const monthDate = new Date(year, month - 1 + offset, 1)
    const y = monthDate.getFullYear()
    const m = monthDate.getMonth() + 1
    const daysInMonth = new Date(y, m, 0).getDate()
    for (let i = 1; i <= daysInMonth; i++) {
      days.push(buildDay(y, m, i, offset === 0))
    }
  }
  return days
})
// ===================== 方法 =====================
const isSelectedDay = (day) => {
  return selectedDate.value === day.dateStr
}
const selectDay = (day) => {
  selectedDate.value = day.dateStr
  const d = new Date(day.dateStr)
  const year = d.getFullYear()
  const month = d.getMonth() + 1
  if (year !== currentYear.value || month !== currentMonth.value) {
    currentYear.value = year
    currentMonth.value = month
    loadStepsData(year, month)
  }
  scrollToSelectedDay()
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
  const monthPrefix = `${newYear}-${String(newMonth).padStart(2, '0')}`
  if (!selectedDate.value.startsWith(monthPrefix)) {
    selectedDate.value = `${monthPrefix}-01`
  }
  loadStepsData(newYear, newMonth)
  scrollToSelectedDay()
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
  scrollToSelectedDay()
}
// ===================== 步数数据加载 =====================
const loadStepsData = async (year, month) => {
  try {
    const rangeStart = new Date(year, month - 2, 1)
    const rangeEnd = new Date(year, month + 1, 0)
    const firstDay = formatDate(rangeStart, 'YYYY-MM-DD')
    const lastDay = formatDate(rangeEnd, 'YYYY-MM-DD')
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
  const map = { 1: 'run', 2: 'walk', 3: 'cycling' }
  return map[type] || 'run'
}
const getTypeName = (type) => {
  const map = { 1: '跑步', 2: '健走', 3: '骑行' }
  return map[type] || '运动'
}
const scrollToSelectedDay = () => {
  nextTick(() => {
    const index = calendarDays.value.findIndex(day => day.dateStr === selectedDate.value)
    if (index < 0) return
    const info = uni.getSystemInfoSync()
    const rpxToPx = (value) => value * info.windowWidth / 750
    const itemWidth = rpxToPx(82)
    const itemGap = rpxToPx(8)
    const cardHorizontalSpace = rpxToPx(28 * 2 + 24 * 2)
    const visibleWidth = Math.max(0, info.windowWidth - cardHorizontalSpace)
    const target = index * (itemWidth + itemGap) - (visibleWidth - itemWidth) / 2
    calendarScrollLeft.value = Math.max(0, target)
  })
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
  scrollToSelectedDay()
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
  background:
    radial-gradient(circle at 18% 0%, var(--page-radial-a), transparent 34%),
    radial-gradient(circle at 88% 8%, var(--page-radial-b), transparent 30%),
    var(--bg-primary);
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
  background: var(--bg-elevated);
  border: 1rpx solid var(--card-border);
  border-radius: 24rpx;
  padding: 22rpx 24rpx;
  box-shadow: var(--card-shadow);
}
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.month-nav {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  &:active {
    opacity: 0.7;
  }
}
.nav-arrow {
  width: 100%;
  height: 100%;
  color: var(--accent-green);
}
.month-title {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text-primary);
}
.calendar-scroll {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
}
.day-strip {
  display: inline-flex;
  gap: 8rpx;
  padding: 2rpx 0 6rpx;
}
.day-cell {
  width: 82rpx;
  height: 108rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 22rpx;
  background: var(--bg-secondary);
  border: 1rpx solid transparent;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  .day-week {
    font-size: 20rpx;
    color: var(--text-tertiary);
    line-height: 1;
    margin-bottom: 8rpx;
  }
  .day-num {
    font-size: 28rpx;
    color: var(--text-primary);
    width: 46rpx;
    height: 46rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
  }
  &.other-month {
    background: rgba(148, 163, 184, 0.12);
    .day-week,
    .day-num {
      color: var(--text-tertiary);
    }
  }
  &.today {
    border-color: rgba(34, 197, 94, 0.28);
  }
  &.selected {
    background: var(--accent-green);
    border-color: var(--accent-green);
    box-shadow: 0 12rpx 24rpx var(--accent-glow);
    .day-week,
    .day-num {
      color: #fff;
    }
  }
  &.has-record:not(.selected) {
    .day-num {
      color: var(--accent-green);
      font-weight: 600;
    }
  }
}
.calendar-footer {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 16rpx;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid var(--border-color);
  min-height: 36rpx;
}
.selected-day {
  font-size: 22rpx;
  color: var(--text-tertiary);
}
.selected-steps {
  font-size: 22rpx;
  color: var(--accent-green);
  font-weight: 700;
}
.back-today {
  margin-left: auto;
  font-size: 24rpx;
  color: var(--accent-green);
  font-weight: 500;
}
/* ===================== 记录列表 ===================== */
.record-section {
  margin: 0 28rpx;
  background: var(--bg-elevated);
  border: 1rpx solid var(--card-border);
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: var(--card-shadow);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: var(--text-primary);
}
.more-link {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 26rpx;
  color: var(--accent-green);
  font-weight: 700;
}
.record-scroll {
  max-height: 470rpx;
}
.record-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding-right: 2rpx;
}
.record-item {
  display: flex;
  align-items: center;
  padding: 18rpx 20rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-color);
  border-radius: 18rpx;
  &:active {
    background: #f1f5f9;
  }
}
.record-icon {
  width: 58rpx;
  height: 58rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18rpx;
  background: #f0f0f0;
  &.type-1 { background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%); }
  &.type-2 { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
  &.type-3 { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
}
.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  .record-name {
    font-size: 28rpx;
    font-weight: 700;
    color: var(--text-primary);
  }
  .record-time {
    font-size: 22rpx;
    color: var(--text-tertiary);
  }
}
.record-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
  .record-duration {
    font-size: 26rpx;
    font-weight: 700;
    color: var(--accent-green);
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
