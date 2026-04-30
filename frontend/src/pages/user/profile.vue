<template>
  <view class="profile-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-header">
        <image class="user-avatar" :src="userStore.avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="user-name">{{ userStore.nickname }}</text>
          <view class="user-tags">
            <text class="tag admin-tag" v-if="userStore.isAdmin">管理员</text>
            <text class="tag guest-tag" v-else-if="userStore.isGuest">游客</text>
            <text class="tag user-tag" v-else>普通用户</text>
          </view>
        </view>
        <text class="edit-icon" @click="editProfile">✏️</text>
      </view>
      
      <!-- 运动数据概览 -->
      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-num">{{ userStats.workoutCount }}</text>
          <text class="stat-label">运动次数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ formatDistance(userStats.totalDistance) }}</text>
          <text class="stat-label">总距离</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ formatDuration(userStats.totalDuration) }}</text>
          <text class="stat-label">总时长</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ userStats.totalCalories }}</text>
          <text class="stat-label">卡路里</text>
        </view>
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

    <!-- 管理员专区 -->
    <view class="menu-section" v-if="userStore.isAdmin">
      <view class="menu-title admin-title">管理员专区</view>
      <view class="menu-list">
        <view class="menu-item" @click="goToUserManage">
          <text class="menu-icon">👥</text>
          <text class="menu-text">用户管理</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToSystemSettings">
          <text class="menu-icon">🔧</text>
          <text class="menu-text">系统设置</text>
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

// 用户管理（管理员）
const goToUserManage = () => {
  uni.showToast({ title: '用户管理功能开发中', icon: 'none' })
}

// 系统设置（管理员）
const goToSystemSettings = () => {
  uni.showToast({ title: '系统设置功能开发中', icon: 'none' })
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
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  padding: 60rpx 40rpx 40rpx;
  border-radius: 0 0 40rpx 40rpx;
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  background: #fff;
}

.user-info {
  flex: 1;
  margin-left: 30rpx;
}

.user-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 12rpx;
}

.user-tags {
  display: flex;
  gap: 12rpx;
}

.tag {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.admin-tag {
  background: #FF5722;
  color: #fff;
}

.guest-tag {
  background: #9E9E9E;
  color: #fff;
}

.user-tag {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.edit-icon {
  font-size: 36rpx;
  padding: 10rpx;
}

// 统计数据
.stats-row {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20rpx;
  padding: 30rpx 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
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

.admin-title {
  color: #FF5722;
  background: #FFF3E0;
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
