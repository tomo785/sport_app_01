<template>
  <view class="container">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <scroll-view class="type-filter" scroll-x :show-scrollbar="false">
        <view
          class="filter-chip"
          :class="{ active: workoutStore.filters.type === null }"
          @click="onTypeChange(null)"
        >
          <text>全部</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: workoutStore.filters.type === 1 }"
          @click="onTypeChange(1)"
        >
          <text>跑步</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: workoutStore.filters.type === 2 }"
          @click="onTypeChange(2)"
        >
          <text>健走</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: workoutStore.filters.type === 3 }"
          @click="onTypeChange(3)"
        >
          <text>骑行</text>
        </view>
      </scroll-view>

      <view class="date-filter">
        <view
          class="date-chip"
          :class="{ active: dateRange === 'all' }"
          @click="onDateChange('all')"
        >
          <text>全部</text>
        </view>
        <view
          class="date-chip"
          :class="{ active: dateRange === '7days' }"
          @click="onDateChange('7days')"
        >
          <text>近7天</text>
        </view>
        <view
          class="date-chip"
          :class="{ active: dateRange === '30days' }"
          @click="onDateChange('30days')"
        >
          <text>近30天</text>
        </view>
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view
      class="record-scroll"
      scroll-y
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view class="record-list" v-if="workoutStore.hasRecords">
        <view
          class="record-card"
          v-for="item in workoutStore.records"
          :key="item.id"
          @click="goToDetail(item.id)"
        >
          <view class="record-header">
            <view class="record-type" :class="'type-' + item.type">
              <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
              <text class="type-name">{{ item.typeName || getTypeName(item.type) }}</text>
            </view>
            <text class="record-date">{{ formatDateTime(item.startTime) }}</text>
          </view>

          <view class="record-body">
            <view class="stat-item">
              <text class="stat-value">{{ formatDistance(item.distance) }}</text>
              <text class="stat-label">距离</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ formatDuration(item.duration) }}</text>
              <text class="stat-label">时长</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ item.calories || 0 }}</text>
              <text class="stat-label">千卡</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ formatPace(item.avgPace) }}</text>
              <text class="stat-label">配速</text>
            </view>
          </view>

          <view class="record-footer" v-if="item.status === 1">
            <text class="status-tag completed">已完成</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more">
          <text v-if="workoutStore.loading" class="load-text">加载中...</text>
          <text v-else-if="workoutStore.finished" class="load-text finished">没有更多了</text>
          <text v-else class="load-text">上拉加载更多</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-else-if="!workoutStore.loading">
        <text class="empty-icon">📋</text>
        <text class="empty-title">暂无运动记录</text>
        <text class="empty-desc">快去开始一次运动吧</text>
        <view class="empty-btn" @click="goToRunning">
          <text>去运动</text>
        </view>
      </view>

      <!-- 初始加载中 -->
      <view class="loading-state" v-if="workoutStore.loading && !workoutStore.hasRecords">
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useWorkoutStore } from '@/stores/workout'
import { formatDate, formatDuration, formatPace, formatDistance } from '@/utils'

const workoutStore = useWorkoutStore()
const isRefreshing = ref(false)
const dateRange = ref('all')

// ===================== 工具函数 =====================
const getTypeIcon = (type) => {
  const map = { 1: '跑', 2: '走', 3: '骑' }
  return map[type] || '运'
}

const getTypeName = (type) => {
  const map = { 1: '跑步', 2: '健走', 3: '骑行' }
  return map[type] || '运动'
}

const formatDateTime = (time) => {
  if (!time) return ''
  // 兼容 ISO 格式和普通格式
  const str = typeof time === 'string' ? time : ''
  return str.substring(0, 16).replace('T', ' ')
}

// ===================== 筛选 =====================
const onTypeChange = (type) => {
  workoutStore.setFilters({ type })
}

const onDateChange = (range) => {
  dateRange.value = range
  const now = new Date()
  let startDate = ''
  let endDate = ''

  if (range === '7days') {
    const d = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
    startDate = formatDate(d, 'YYYY-MM-DD')
    endDate = formatDate(now, 'YYYY-MM-DD')
  } else if (range === '30days') {
    const d = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    startDate = formatDate(d, 'YYYY-MM-DD')
    endDate = formatDate(now, 'YYYY-MM-DD')
  }

  workoutStore.setFilters({ startDate, endDate })
}

// ===================== 列表交互 =====================
const onRefresh = async () => {
  isRefreshing.value = true
  await workoutStore.fetchRecords(true)
  isRefreshing.value = false
}

const onLoadMore = () => {
  if (workoutStore.loading || workoutStore.finished) return
  workoutStore.fetchRecords()
}

const goToDetail = (id) => {
  uni.navigateTo({ url: `/pages/workout/detail?id=${id}` })
}

const goToRunning = () => {
  uni.switchTab({ url: '/pages/running/running' })
}

// ===================== 生命周期 =====================
onMounted(() => {
  if (!workoutStore.hasRecords) {
    workoutStore.fetchRecords(true)
  }
})

onShow(() => {
  // 从详情返回时可以考虑刷新，但暂不自动刷新以免打断用户浏览
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* ===================== 筛选栏 ===================== */
.filter-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.type-filter {
  white-space: nowrap;
  margin-bottom: 16rpx;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 28rpx;
  margin-right: 16rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
  transition: all 0.2s;

  &.active {
    background: #22c55e;
    color: #fff;
    font-weight: 600;
  }
}

.date-filter {
  display: flex;
  gap: 16rpx;
}

.date-chip {
  padding: 10rpx 24rpx;
  background: #f5f5f5;
  border-radius: 28rpx;
  font-size: 24rpx;
  color: #666;

  &.active {
    background: #22c55e;
    color: #fff;
    font-weight: 600;
  }
}

/* ===================== 列表 ===================== */
.record-scroll {
  flex: 1;
  padding: 20rpx 30rpx;
}

.record-list {
  padding-bottom: 40rpx;
}

.record-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);

  &:active {
    opacity: 0.9;
  }
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.record-type {
  display: flex;
  align-items: center;
  gap: 10rpx;

  .type-icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    background: #f0f0f0;
  }

  .type-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
  }

  &.type-1 .type-icon { background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); }
  &.type-2 .type-icon { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
  &.type-3 .type-icon { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
}

.record-date {
  font-size: 24rpx;
  color: #999;
}

.record-body {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-top: 1rpx solid #f5f5f5;
  border-bottom: 1rpx solid #f5f5f5;
}

.stat-item {
  text-align: center;
  flex: 1;

  .stat-value {
    display: block;
    font-size: 32rpx;
    font-weight: 700;
    color: #333;
    margin-bottom: 8rpx;
  }

  .stat-label {
    display: block;
    font-size: 22rpx;
    color: #999;
  }
}

.record-footer {
  margin-top: 20rpx;
  display: flex;
  justify-content: flex-end;
}

.status-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;

  &.completed {
    background: #f0fdf4;
    color: #16a34a;
  }
}

/* ===================== 加载更多 ===================== */
.load-more {
  text-align: center;
  padding: 30rpx 0;

  .load-text {
    font-size: 24rpx;
    color: #999;

    &.finished {
      color: #ccc;
    }
  }
}

/* ===================== 空状态 ===================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 30rpx;
  }

  .empty-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 12rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: #999;
    margin-bottom: 40rpx;
  }

  .empty-btn {
    padding: 20rpx 60rpx;
    background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
    border-radius: 40rpx;

    text {
      color: #fff;
      font-size: 28rpx;
      font-weight: 600;
    }
  }
}

/* ===================== 加载中 ===================== */
.loading-state {
  text-align: center;
  padding-top: 200rpx;

  .loading-text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
