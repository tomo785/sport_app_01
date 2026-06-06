<template>
  <view class="container">
    <!-- 加载中 -->
    <view class="loading-wrap" v-if="workoutStore.loading && !workoutStore.currentDetail">
      <text class="loading-text">加载中...</text>
    </view>
    <!-- 错误状态 -->
    <view class="error-wrap" v-else-if="workoutStore.error">
      <AppIcon class="error-icon" name="warning" size="48" />
      <text class="error-text">{{ workoutStore.error }}</text>
      <view class="retry-btn" @click="loadDetail">
        <text>重新加载</text>
      </view>
    </view>
    <!-- 详情内容 -->
    <block v-else-if="workoutStore.currentDetail">
      <scroll-view class="detail-scroll" scroll-y>
        <!-- 地图区域 -->
        <view class="map-section">
          <map
            v-if="mapCenter.latitude"
            class="detail-map"
            :latitude="mapCenter.latitude"
            :longitude="mapCenter.longitude"
            :scale="15"
            :show-location="false"
            :enable-3D="false"
            :enable-zoom="true"
            :enable-scroll="true"
            :enable-rotate="false"
            :polyline="polylineData"
          />
          <view class="map-placeholder" v-else>
            <AppIcon class="map-placeholder-icon" name="map" size="64" />
            <text class="map-placeholder-text">暂无轨迹数据</text>
          </view>
          <!-- 返回按钮 -->
          <view class="back-btn" @click="goBack">
            <AppIcon class="back-icon" name="arrowLeft" size="30" bold />
          </view>
        </view>
        <!-- 核心数据卡片 -->
        <view class="data-card">
          <view class="data-header">
            <view class="data-type" :class="'type-' + detail.type">
              <text class="type-icon">{{ getTypeIcon(detail.type) }}</text>
              <text class="type-name">{{ detail.typeName || getTypeName(detail.type) }}</text>
            </view>
            <view class="goal-badge" v-if="detail.goalAchieved">
              <view class="goal-achieved"><AppIcon name="achievement" size="24" /><text>目标达成</text></view>
            </view>
          </view>
          <view class="hero-data">
            <view class="hero-item">
              <text class="hero-value">{{ formatDistance(detail.distance) }}</text>
              <text class="hero-label">距离</text>
            </view>
            <view class="hero-item">
              <text class="hero-value">{{ formatDuration(detail.duration) }}</text>
              <text class="hero-label">时长</text>
            </view>
          </view>
          <view class="sub-data">
            <view class="sub-item">
              <text class="sub-value">{{ formatPace(detail.avgPace) }}</text>
              <text class="sub-label">平均配速</text>
            </view>
            <view class="sub-item">
              <text class="sub-value">{{ detail.calories || 0 }}</text>
              <text class="sub-label">千卡</text>
            </view>
            <view class="sub-item">
              <text class="sub-value">{{ formatSpeedKmh(detail.avgSpeed) }}</text>
              <text class="sub-label">km/h</text>
            </view>
            <view class="sub-item">
              <text class="sub-value">{{ detail.steps || 0 }}</text>
              <text class="sub-label">步数</text>
            </view>
          </view>
        </view>
        <!-- 时间信息 -->
        <view class="info-card">
          <view class="info-row">
            <text class="info-label">开始时间</text>
            <text class="info-value">{{ formatDateTime(detail.startTime || detail.startTimeStr) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">结束时间</text>
            <text class="info-value">{{ formatDateTime(detail.endTime || detail.endTimeStr) }}</text>
          </view>
          <view class="info-row" v-if="detail.goalType">
            <text class="info-label">运动目标</text>
            <text class="info-value">{{ goalText }}</text>
          </view>
        </view>
        <!-- 操作按钮 -->
        <view class="action-bar">
          <view class="action-btn primary" @click="shareRecord">
            <text>分享记录</text>
          </view>
          <view class="action-btn secondary" @click="goBack">
            <text>返回列表</text>
          </view>
        </view>
        <view class="spacer"></view>
      </scroll-view>
    </block>
  </view>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useWorkoutStore } from '@/stores/workout'
import { formatDate, formatDuration, formatPace, formatDistance } from '@/utils'
const workoutStore = useWorkoutStore()
const recordId = ref(null)
// ===================== 计算属性 =====================
const detail = computed(() => workoutStore.currentDetail || {})
const mapCenter = computed(() => {
  const traj = workoutStore.currentTrajectory
  if (traj && traj.length > 0) {
    return {
      latitude: traj[0].latitude,
      longitude: traj[0].longitude
    }
  }
  // 尝试使用详情中的起点坐标
  const d = detail.value
  if (d.startLat && d.startLng) {
    return { latitude: d.startLat, longitude: d.startLng }
  }
  return { latitude: 0, longitude: 0 }
})
const polylineData = computed(() => {
  const traj = workoutStore.currentTrajectory
  if (!traj || traj.length < 2) return []
  return [{
    points: traj.map(p => ({
      latitude: p.latitude,
      longitude: p.longitude
    })),
    color: '#667eea',
    width: 4,
    arrowLine: false
  }]
})
const goalText = computed(() => {
  const d = detail.value
  if (!d.goalType) return ''
  if (d.goalType === 'distance') return `目标距离 ${d.goalValue}km`
  if (d.goalType === 'duration') return `目标时长 ${d.goalValue}分钟`
  return '自定义目标'
})
// ===================== 工具函数 =====================
const getTypeIcon = (type) => {
  const map = { 1: 'run', 2: 'walk', 3: 'cycling' }
  return map[type] || 'run'
}
const getTypeName = (type) => {
  const map = { 1: '跑步', 2: '健走', 3: '骑行' }
  return map[type] || '运动'
}
const formatDateTime = (time) => {
  if (!time) return '-'
  const str = typeof time === 'string' ? time : ''
  return str.substring(0, 16).replace('T', ' ')
}
const formatSpeedKmh = (speed) => {
  const value = Number(speed || 0)
  return value > 0 ? (value * 3.6).toFixed(1) : '0.0'
}
// ===================== 交互 =====================
const loadDetail = () => {
  if (recordId.value) {
    workoutStore.fetchDetail(recordId.value)
  }
}
const goBack = () => {
  uni.navigateBack()
}
const shareRecord = () => {
  const d = detail.value
  const title = `${getTypeName(d.type)} ${formatDistance(d.distance)}`
  const content = `时长 ${formatDuration(d.duration)}，配速 ${formatPace(d.avgPace)}，消耗 ${d.calories || 0} 千卡`
  uni.share({
    provider: 'weixin',
    title,
    content,
    success: () => uni.showToast({ title: '分享成功', icon: 'success' }),
    fail: () => {
      uni.setClipboardData({
        data: `${title}\n${content}`
      })
      uni.showToast({ title: '已复制到剪贴板', icon: 'none' })
    }
  })
}
// ===================== 生命周期 =====================
onLoad((options) => {
  if (options && options.id) {
    recordId.value = options.id
    loadDetail()
  }
})
onMounted(() => {
  // onLoad 中已处理
})
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: var(--bg-primary);
}
/* ===================== 加载 / 错误 ===================== */
.loading-wrap,
.error-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
.loading-text {
  font-size: 28rpx;
  color: var(--text-tertiary);
}
.error-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}
.error-text {
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-bottom: 40rpx;
}
.retry-btn {
  padding: 16rpx 48rpx;
  background: linear-gradient(135deg, var(--accent-green), var(--accent-blue));
  border-radius: 32rpx;
  text {
    color: #fff;
    font-size: 28rpx;
  }
}
/* ===================== 地图区域 ===================== */
.detail-scroll {
  min-height: 100vh;
}
.map-section {
  position: relative;
  width: 100%;
  height: 500rpx;
  background: var(--bg-secondary);
}
.detail-map {
  width: 100%;
  height: 100%;
}
.map-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .map-placeholder-icon {
    font-size: 60rpx;
    margin-bottom: 16rpx;
  }
  .map-placeholder-text {
    font-size: 28rpx;
      color: var(--text-tertiary);
  }
}
.back-btn {
  position: absolute;
  top: 60rpx;
  left: 30rpx;
  width: 64rpx;
  height: 64rpx;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  .back-icon {
    color: var(--text-primary);
  }
}
/* ===================== 数据卡片 ===================== */
.data-card {
  margin: -40rpx 30rpx 20rpx;
  background: var(--bg-elevated);
  border: 1rpx solid var(--card-border);
  border-radius: 30rpx;
  padding: 32rpx;
  box-shadow: var(--card-shadow);
  position: relative;
  z-index: 10;
}
.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}
.data-type {
  display: flex;
  align-items: center;
  gap: 12rpx;
  .type-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 18rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    background: var(--soft-neutral);
  }
  .type-name {
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-primary);
  }
  &.type-1 .type-icon { background: linear-gradient(135deg, var(--soft-green) 0%, var(--soft-blue) 100%); }
  &.type-2 .type-icon { background: linear-gradient(135deg, var(--soft-blue) 0%, var(--soft-green) 100%); }
  &.type-3 .type-icon { background: linear-gradient(135deg, var(--soft-orange) 0%, var(--soft-green) 100%); }
}
.goal-badge {
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 24rpx;
  text {
    font-size: 24rpx;
    color: #92400e;
    font-weight: 600;
  }
}
.hero-data {
  display: flex;
  justify-content: center;
  gap: 80rpx;
  padding: 20rpx 0 32rpx;
  border-bottom: 1rpx solid var(--border-color);
  margin-bottom: 24rpx;
}
.hero-item {
  text-align: center;
  .hero-value {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: var(--accent-green);
    margin-bottom: 8rpx;
  }
  .hero-label {
    display: block;
    font-size: 26rpx;
    color: var(--text-tertiary);
  }
}
.sub-data {
  display: flex;
  justify-content: space-between;
}
.sub-item {
  text-align: center;
  flex: 1;
  .sub-value {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8rpx;
  }
  .sub-label {
    display: block;
    font-size: 22rpx;
    color: var(--text-tertiary);
  }
}
/* ===================== 信息卡片 ===================== */
.info-card {
  margin: 0 30rpx 20rpx;
  background: var(--bg-elevated);
  border: 1rpx solid var(--card-border);
  border-radius: 24rpx;
  padding: 20rpx 32rpx;
  box-shadow: var(--card-shadow-soft);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid var(--border-color);
  &:last-child {
    border-bottom: none;
  }
}
.info-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}
.info-value {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
}
/* ===================== 操作栏 ===================== */
.action-bar {
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx;
  padding-top: 20rpx;
}
.action-btn {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  &.primary {
    background: linear-gradient(135deg, var(--accent-green) 0%, var(--accent-blue) 100%);
    color: #fff;
  }
  &.secondary {
    background: var(--bg-elevated);
    color: var(--accent-green);
    border: 2rpx solid var(--accent-green);
  }
}
.spacer {
  height: 60rpx;
}
</style>
