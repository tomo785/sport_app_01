<template>
  <view class="container">
    <!-- 头部汇总 -->
    <view class="header">
      <view class="header-title">运动统计</view>
      <view class="summary-cards">
        <view class="summary-card">
          <view class="summary-value">{{ summary.totalWorkouts || 0 }}</view>
          <view class="summary-label">总次数</view>
        </view>
        <view class="summary-card">
          <view class="summary-value">{{ summary.totalDistanceStr || '0km' }}</view>
          <view class="summary-label">总距离</view>
        </view>
        <view class="summary-card">
          <view class="summary-value">{{ summary.totalCaloriesStr || '0千卡' }}</view>
          <view class="summary-label">总消耗</view>
        </view>
        <view class="summary-card">
          <view class="summary-value">{{ summary.streakDays || 0 }}</view>
          <view class="summary-label">连续天数</view>
        </view>
      </view>
    </view>
    <!-- 本周/本月统计 -->
    <view class="section">
      <view class="section-title">近期概况</view>
      <view class="period-stats">
        <view class="period-item">
          <text class="period-label">本周运动</text>
          <text class="period-value">{{ summary.weeklyWorkouts || 0 }} 次</text>
        </view>
        <view class="period-item">
          <text class="period-label">本月运动</text>
          <text class="period-value">{{ summary.monthlyWorkouts || 0 }} 次</text>
        </view>
      </view>
    </view>
    <!-- 趋势图表 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">运动趋势</view>
        <view class="tab-switch">
          <text class="tab" :class="{ active: trendType === 'week' }" @click="switchTrend('week')">周</text>
          <text class="tab" :class="{ active: trendType === 'month' }" @click="switchTrend('month')">月</text>
          <text class="tab" :class="{ active: trendType === 'year' }" @click="switchTrend('year')">年</text>
        </view>
      </view>
      <!-- 趋势数据 -->
      <view class="trend-content" v-if="trendData.dates && trendData.dates.length > 0">
        <view class="trend-chart">
          <!-- 距离柱状图 -->
          <view class="chart-title">距离趋势 (米)</view>
          <view class="chart-bars">
            <view class="bar-item" v-for="(value, index) in trendData.distances" :key="index">
              <view class="bar-wrapper">
                <view class="bar" :style="{ height: getBarHeight(value, maxDistance) + '%' }"></view>
              </view>
              <text class="bar-label">{{ trendData.dates[index] }}</text>
            </view>
          </view>
        </view>
        <!-- 统计数据 -->
        <view class="trend-stats">
          <view class="trend-stat-item">
            <text class="trend-stat-label">总距离</text>
            <text class="trend-stat-value">{{ formatNumber(trendTotal.distance) }}米</text>
          </view>
          <view class="trend-stat-item">
            <text class="trend-stat-label">总时长</text>
            <text class="trend-stat-value">{{ formatDuration(trendTotal.duration) }}</text>
          </view>
          <view class="trend-stat-item">
            <text class="trend-stat-label">总消耗</text>
            <text class="trend-stat-value">{{ formatNumber(trendTotal.calories) }}千卡</text>
          </view>
          <view class="trend-stat-item">
            <text class="trend-stat-label">运动次数</text>
            <text class="trend-stat-value">{{ trendTotal.count }}次</text>
          </view>
        </view>
      </view>
      <view class="empty" v-else>
        <text>暂无数据</text>
      </view>
    </view>
    <!-- 今日统计 -->
    <view class="section">
      <view class="section-title">今日运动</view>
      <view class="today-stats" v-if="todayStats.statDate">
        <view class="today-item">
          <text class="today-value">{{ todayStats.distanceStr || '0m' }}</text>
          <text class="today-label">距离</text>
        </view>
        <view class="today-item">
          <text class="today-value">{{ todayStats.durationStr || '0分' }}</text>
          <text class="today-label">时长</text>
        </view>
        <view class="today-item">
          <text class="today-value">{{ todayStats.caloriesStr || '0千卡' }}</text>
          <text class="today-label">消耗</text>
        </view>
        <view class="today-item">
          <text class="today-value">{{ todayStats.recordCount || 0 }}</text>
          <text class="today-label">次数</text>
        </view>
      </view>
      <view class="empty" v-else>
        <text>今日暂无运动记录</text>
      </view>
    </view>
    <!-- 最近运动打卡记录 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">运动打卡记录</view>
        <view class="more-link" @click="goToWorkoutList">
          <text>查看更多</text>
          <AppIcon name="arrowRight" size="22" bold />
        </view>
      </view>
      <view class="record-list" v-if="workoutRecords.length > 0">
        <view class="record-item" v-for="(item, index) in workoutRecords" :key="index" @click="goToWorkoutDetail(item.id)">
          <view class="record-icon" :class="'type-' + item.type">
            <text>{{ getTypeIcon(item.type) }}</text>
          </view>
          <view class="record-info">
            <text class="record-name">{{ item.typeName || getTypeName(item.type) }}</text>
            <text class="record-time">{{ item.startTimeStr || formatTime(item.startTime) }}</text>
          </view>
          <view class="record-stats">
            <text class="record-duration">{{ formatDuration(item.duration) }}</text>
            <text class="record-distance" v-if="item.distance">{{ (item.distance / 1000).toFixed(2) }}km</text>
            <text class="record-calories" v-if="item.calories">{{ item.calories }}千卡</text>
          </view>
        </view>
      </view>
      <view class="empty" v-else>
        <text>暂无运动打卡记录</text>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatsSummary, getTodayStats, getWeeklyTrend, getMonthlyTrend, getYearlyTrend } from '@/api/stats'
import { getWorkoutList } from '@/api/workout'
import { formatDate, formatDuration } from '@/utils'
const summary = ref({})
const todayStats = ref({})
const trendData = ref({})
const trendType = ref('week')
const workoutRecords = ref([])
// 计算最大距离用于图表比例
const maxDistance = computed(() => {
  if (!trendData.value.distances || trendData.value.distances.length === 0) return 1
  return Math.max(...trendData.value.distances, 1)
})
// 计算趋势总计
const trendTotal = computed(() => {
  const data = trendData.value
  if (!data.distances) return { distance: 0, duration: 0, calories: 0, count: 0 }
  return {
    distance: data.distances.reduce((a, b) => a + b, 0),
    duration: data.durations ? data.durations.reduce((a, b) => a + b, 0) : 0,
    calories: data.calories ? data.calories.reduce((a, b) => a + b, 0) : 0,
    count: data.counts ? data.counts.reduce((a, b) => a + b, 0) : 0
  }
})
// 加载统计数据
const loadSummary = async () => {
  try {
    const res = await getStatsSummary()
    if (res.code === 200) {
      summary.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计汇总失败:', error)
  }
}
// 加载今日统计
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
// 加载趋势数据
const loadTrend = async () => {
  try {
    let res
    switch (trendType.value) {
      case 'week':
        res = await getWeeklyTrend()
        break
      case 'month':
        res = await getMonthlyTrend()
        break
      case 'year':
        res = await getYearlyTrend()
        break
    }
    if (res.code === 200) {
      trendData.value = res.data || {}
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}
// 切换趋势类型
const switchTrend = (type) => {
  trendType.value = type
  loadTrend()
}
// 获取柱状图高度百分比
const getBarHeight = (value, max) => {
  if (max === 0) return 0
  const height = (value / max) * 100
  return Math.max(height, 5) // 最小高度5%
}
// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}
// 加载最近运动记录
const loadWorkoutRecords = async () => {
  try {
    const res = await getWorkoutList({ page: 1, size: 10 })
    if (res.code === 200 && res.data) {
      workoutRecords.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载运动记录失败:', error)
  }
}
const getTypeIcon = (type) => {
  const map = { 1: 'run', 2: 'walk', 3: 'cycling' }
  return map[type] || 'run'
}
const getTypeName = (type) => {
  const map = { 1: '跑步', 2: '健走', 3: '骑行' }
  return map[type] || '运动'
}
const formatTime = (time) => {
  if (!time) return ''
  return time.substring(0, 16).replace('T', ' ')
}
const goToWorkoutDetail = (id) => {
  uni.navigateTo({ url: `/pages/workout/detail?id=${id}` })
}
const goToWorkoutList = () => {
  uni.navigateTo({ url: '/pages/workout/workout' })
}
onMounted(() => {
  loadSummary()
  loadTodayStats()
  loadTrend()
  loadWorkoutRecords()
})
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 30rpx 40rpx;
  &-title {
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 30rpx;
  }
}
.summary-cards {
  display: flex;
  justify-content: space-between;
}
.summary-card {
  flex: 1;
  text-align: center;
  padding: 20rpx 10rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16rpx;
  margin: 0 8rpx;
  &:first-child {
    margin-left: 0;
  }
  &:last-child {
    margin-right: 0;
  }
}
.summary-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8rpx;
}
.summary-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}
.section {
  margin: 20rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.tab-switch {
  display: flex;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 4rpx;
}
.tab {
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 28rpx;
  &.active {
    background: #667eea;
    color: #fff;
  }
}
.period-stats {
  display: flex;
}
.period-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  &:first-child {
    border-right: 1rpx solid #eee;
  }
}
.period-label {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-bottom: 12rpx;
}
.period-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
}
// 趋势图表
.trend-content {
  margin-top: 20rpx;
}
.chart-title {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 20rpx;
}
.chart-bars {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 200rpx;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #eee;
}
.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 4rpx;
}
.bar-wrapper {
  width: 100%;
  height: 160rpx;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.bar {
  width: 60%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 8rpx 8rpx 0 0;
  transition: height 0.3s ease;
}
.bar-label {
  margin-top: 10rpx;
  font-size: 20rpx;
  color: #999;
  transform: scale(0.85);
}
.trend-stats {
  display: flex;
  flex-wrap: wrap;
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #eee;
}
.trend-stat-item {
  width: 50%;
  padding: 16rpx 0;
}
.trend-stat-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}
.trend-stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
// 今日统计
.today-stats {
  display: flex;
  justify-content: space-between;
}
.today-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 10rpx;
}
.today-value {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10rpx;
}
.today-label {
  font-size: 24rpx;
  color: #999;
}
.empty {
  padding: 60rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}
.more-link {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 26rpx;
  color: #667eea;
  font-weight: 700;
}
.record-list {
  margin-top: 10rpx;
}
.record-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  &:last-child {
    border-bottom: none;
  }
  &:active {
    background: #f9f9f9;
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
    color: #333;
  }
  .record-time {
    font-size: 24rpx;
    color: #999;
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
    color: #667eea;
  }
  .record-distance,
  .record-calories {
    font-size: 22rpx;
    color: #999;
  }
}
</style>
