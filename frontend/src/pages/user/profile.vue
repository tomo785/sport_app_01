<template>
  <view class="profile-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <!-- 用户信息行 -->
      <view class="user-header" @click="editProfile">
        <view class="user-avatar-wrap">
          <image class="user-avatar" :src="userStore.avatar" mode="aspectFill"></image>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userStore.nickname }}</text>
        </view>
        <text class="header-arrow">›</text>
      </view>

      <!-- 数据统计行 -->
      <view class="social-row">
        <view class="social-item">
          <text class="social-num">0</text>
          <text class="social-label">关注</text>
        </view>
        <view class="social-divider"></view>
        <view class="social-item">
          <text class="social-num">0</text>
          <text class="social-label">粉丝</text>
        </view>
        <view class="social-divider"></view>
        <view class="social-item">
          <text class="social-num">0</text>
          <text class="social-label">加油</text>
        </view>
      </view>

      <!-- 成就标签行 -->
      <view class="achievement-bar">
        <scroll-view class="achievement-scroll" scroll-x :show-scrollbar="false">
          <view class="achievement-tag">
            <text class="ach-icon">🏅</text>
            <text class="ach-text">最新成就</text>
          </view>
          <view class="achievement-tag">
            <text class="ach-icon">🔥</text>
            <text class="ach-text">连续打卡 0 天</text>
          </view>
          <view class="achievement-tag">
            <text class="ach-icon">⭐</text>
            <text class="ach-text">运动新手</text>
          </view>
          <view class="achievement-tag">
            <text class="ach-icon">🎯</text>
            <text class="ach-text">目标达成 0 次</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-title">我的运动</view>
      <view class="menu-list">
        <view class="menu-item" @click="goToWorkoutHistory">
          <text class="menu-icon">📋</text>
          <text class="menu-text">运动记录</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToStats">
          <text class="menu-icon">📊</text>
          <text class="menu-text">数据统计</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToGoals">
          <text class="menu-icon">🎯</text>
          <text class="menu-text">运动目标</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-title">个人设置</view>
      <view class="menu-list">
        <view class="menu-item" @click="editProfile">
          <text class="menu-icon">👤</text>
          <text class="menu-text">编辑资料</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToSettings">
          <text class="menu-icon">⚙️</text>
          <text class="menu-text">账号设置</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="showAbout">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于我们</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录按钮 -->
    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">
        {{ userStore.isGuest ? '退出游客模式' : '退出登录' }}
      </button>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text>运动App v1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { formatDuration, formatDistance } from '../../utils/index'
import { getWorkoutList } from '../../api/workout'

const userStore = useUserStore()

// 用户统计数据
const userStats = reactive({
  workoutCount: 0,
  totalDistance: 0,
  totalDuration: 0,
  totalCalories: 0
})

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    const res = await getWorkoutList({ page: 1, size: 1000 })
    if (res.code === 200) {
      const list = res.data?.list || []
      userStats.workoutCount = list.length
      userStats.totalDistance = list.reduce((sum, item) => sum + (item.distance || 0), 0)
      userStats.totalDuration = list.reduce((sum, item) => sum + (item.duration || 0), 0)
      userStats.totalCalories = list.reduce((sum, item) => sum + (item.calories || 0), 0)
    }
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

// 编辑资料
const editProfile = () => {
  if (userStore.isGuest) {
    uni.showModal({
      title: '提示',
      content: '游客无法编辑资料，请先注册账号',
      confirmText: '去注册',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/user/register' })
        }
      }
    })
    return
  }
  uni.navigateTo({ url: '/pages/user/profile-edit' })
}

// 跳转运动记录
const goToWorkoutHistory = () => {
  uni.switchTab({ url: '/pages/stats/stats' })
}

// 跳转数据统计
const goToStats = () => {
  uni.switchTab({ url: '/pages/stats/stats' })
}

// 跳转目标设置
const goToGoals = () => {
  uni.switchTab({ url: '/pages/goal/goal' })
}

// 账号设置
const goToSettings = () => {
  uni.navigateTo({ url: '/pages/user/settings' })
}

// 关于我们
const showAbout = () => {
  uni.showModal({
    title: '关于运动App',
    content: '运动App v1.0.0\n\n记录每一次运动，见证更好的自己。',
    showCancel: false
  })
}

// 退出登录
const handleLogout = () => {
  const isGuest = userStore.isGuest
  const title = isGuest ? '退出游客模式' : '退出登录'
  const content = isGuest 
    ? '退出后需要重新登录才能使用完整功能' 
    : '确定要退出当前账号吗？'
  
  uni.showModal({
    title,
    content,
    confirmText: '确定',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}

// 生命周期
onMounted(() => {
  loadUserStats()
})

onShow(() => {
  loadUserStats()
})
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

// 用户卡片
.user-card {
  background: #fff;
  padding: 40rpx 30rpx 30rpx;
}

.user-header {
  display: flex;
  align-items: center;
  padding: 0 10rpx;
}

.user-avatar-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 2rpx solid #e5e5e5;
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar {
  width: 100%;
  height: 100%;
}

.user-info {
  flex: 1;
  margin-left: 24rpx;
}

.user-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1c1c1e;
}

.header-arrow {
  font-size: 40rpx;
  color: #c7c7cc;
  padding: 10rpx;
}

// 社交统计
.social-row {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 32rpx;
  padding: 0 10rpx;
}

.social-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 40rpx;
}

.social-num {
  font-size: 30rpx;
  font-weight: 600;
  color: #1c1c1e;
  margin-bottom: 6rpx;
}

.social-label {
  font-size: 24rpx;
  color: #8e8e93;
}

.social-divider {
  width: 1rpx;
  height: 40rpx;
  background: #e5e5e5;
}

// 成就标签
.achievement-bar {
  margin-top: 28rpx;
}

.achievement-scroll {
  white-space: nowrap;
}

.achievement-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 24rpx;
  margin-right: 16rpx;
  background: #f2f2f7;
  border-radius: 28rpx;

  &:last-child {
    margin-right: 30rpx;
  }

  &:first-child {
    margin-left: 10rpx;
  }
}

.ach-icon {
  font-size: 24rpx;
}

.ach-text {
  font-size: 24rpx;
  color: #1c1c1e;
  font-weight: 500;
}

// 菜单区域
.menu-section {
  margin: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.menu-title {
  font-size: 28rpx;
  color: #999;
  padding: 24rpx 30rpx 16rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-list {
  padding: 0 20rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: #f9f9f9;
  }
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.menu-arrow {
  font-size: 32rpx;
  color: #ccc;
}

// 退出登录
.logout-section {
  margin: 40rpx 30rpx;
}

.logout-btn {
  width: 100%;
  height: 90rpx;
  background: #fff;
  color: #f56c6c;
  font-size: 32rpx;
  border-radius: 16rpx;
  border: none;
  
  &:active {
    background: #fef0f0;
  }
}

// 版本信息
.version-info {
  text-align: center;
  padding: 20rpx;
  
  text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
