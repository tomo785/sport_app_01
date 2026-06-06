<template>
  <view class="profile-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-header" @click="editProfile">
        <view class="user-avatar-wrap">
          <image class="user-avatar" :src="userStore.avatar" mode="aspectFill"></image>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userStore.nickname }}</text>
        </view>
        <AppIcon class="header-arrow" name="arrowRight" size="30" bold />
      </view>
      <!-- 个人数据行 -->
      <view class="personal-stats-row">
        <view class="ps-item">
          <text class="ps-num">{{ userStats.checkinDays }}</text>
          <text class="ps-label">打卡天数</text>
        </view>
        <view class="ps-divider"></view>
        <view class="ps-item">
          <text class="ps-num">{{ userStats.height || '--' }}</text>
          <text class="ps-label">身高(cm)</text>
        </view>
        <view class="ps-divider"></view>
        <view class="ps-item">
          <text class="ps-num">{{ userStats.weight || '--' }}</text>
          <text class="ps-label">体重(kg)</text>
        </view>
        <view class="ps-divider"></view>
        <view class="ps-item">
          <text class="ps-num">{{ genderLabel }}</text>
          <text class="ps-label">性别</text>
        </view>
      </view>
    </view>
    <view class="health-section">
      <view class="health-header">
        <text class="health-title">健康档案</text>
        <view class="health-edit" @click="editProfile">
          <text>修改</text>
          <AppIcon name="arrowRight" size="22" />
        </view>
      </view>
      <view class="health-grid">
        <view class="health-item">
          <text class="health-value">{{ formatMetric(userStats.waistCircumference, 'cm') }}</text>
          <text class="health-label">腰围</text>
        </view>
        <view class="health-item">
          <text class="health-value">{{ bloodPressureText }}</text>
          <text class="health-label">血压</text>
        </view>
        <view class="health-item">
          <text class="health-value">{{ formatMetric(userStats.restingHeartRate, '次/分') }}</text>
          <text class="health-label">静息心率</text>
        </view>
        <view class="health-item">
          <text class="health-value">{{ formatMetric(userStats.avgExerciseHeartRate, '次/分') }}</text>
          <text class="health-label">运动均心率</text>
        </view>
        <view class="health-item">
          <text class="health-value">{{ formatMetric(userStats.maxHeartRate, '次/分') }}</text>
          <text class="health-label">运动峰值心率</text>
        </view>
      </view>
    </view>
    <!-- 近七天运动峰值热度 -->
    <view class="heatmap-section">
      <view class="heatmap-header">
        <text class="heatmap-title">近七天运动峰值热度</text>
      </view>
      <view class="heatmap-body">
        <view class="heatmap-chart">
          <view class="hm-bar-wrap" v-for="(day, idx) in weekHeatData" :key="idx" @click="onBarClick(day, idx)">
            <view class="hm-bar-column">
              <view class="hm-bar-fill" :style="{ height: day.pct + '%', background: day.color }"></view>
            </view>
            <text class="hm-bar-value" v-if="day.active">{{ day.label }}</text>
            <text class="hm-bar-label">{{ day.dayLabel }}</text>
          </view>
        </view>
        <view class="heatmap-stats">
          <view class="hm-stat">
            <text class="hm-stat-val">{{ weekSummary.totalCount }}</text>
            <text class="hm-stat-label">总运动次数</text>
          </view>
          <view class="hm-stat">
            <text class="hm-stat-val">{{ weekSummary.totalDuration }}</text>
            <text class="hm-stat-label">总时长(分)</text>
          </view>
          <view class="hm-stat">
            <text class="hm-stat-val">{{ weekSummary.peakDay }}</text>
            <text class="hm-stat-label">峰值日</text>
          </view>
        </view>
      </view>
      <!-- 热度色阶 -->
      <view class="heatmap-legend">
        <text class="legend-label">低</text>
        <view class="legend-bar">
          <view class="legend-step" v-for="(c, i) in ['#22c55e','#84cc16','#eab308','#f97316','#ef4444']" :key="i" :style="{ background: c }"></view>
        </view>
        <text class="legend-label">高</text>
      </view>
    </view>
    <!-- 个人设置 -->
    <view class="menu-section">
      <view class="menu-title">个人设置</view>
      <view class="menu-list">
        <view class="menu-item" @click="editProfile">
          <AppIcon class="menu-icon" name="account" size="40" />
          <text class="menu-text">编辑资料</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
        <view class="menu-item" @click="goToDesign">
          <AppIcon class="menu-icon" name="theme" size="40" />
          <text class="menu-text">设计</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
        <view class="menu-item" @click="goToSettings">
          <AppIcon class="menu-icon" name="setting" size="40" />
          <text class="menu-text">账号设置</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
        <view class="menu-item" @click="themeStore.toggleTheme">
          <AppIcon class="menu-icon" :name="themeStore.isDark ? 'night' : 'day'" size="40" />
          <text class="menu-text">深色模式</text>
          <view class="theme-switch" :class="{ 'switch-on': themeStore.isDark }">
            <view class="switch-knob"></view>
          </view>
        </view>
        <view class="menu-item" @click="showAbout">
          <AppIcon class="menu-icon" name="info" size="40" />
          <text class="menu-text">关于我们</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
      </view>
    </view>
    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">
        {{ userStore.isGuest ? '退出游客模式' : '退出登录' }}
      </button>
    </view>
    <view class="version-info">
      <text>运动App v1.0.0</text>
    </view>
  </view>
</template>
<script setup>
import { reactive, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { useThemeStore } from '../../stores/theme'
import { formatDate, formatDuration, formatDistance } from '../../utils/index'
import { getWorkoutList } from '../../api/workout'
const userStore = useUserStore()
const themeStore = useThemeStore()
const weekHeatData = reactive([])
const weekSummary = reactive({
  totalCount: 0,
  totalDuration: 0,
  peakDay: '-'
})
const dayLabels = ['日', '一', '二', '三', '四', '五', '六']
const userStats = reactive({
  checkinDays: 0,
  height: '--',
  weight: '--',
  gender: 0,
  waistCircumference: null,
  systolicPressure: null,
  diastolicPressure: null,
  restingHeartRate: null,
  avgExerciseHeartRate: null,
  maxHeartRate: null
})
const genderLabel = computed(() => {
  const map = { 0: '未设', 1: '男', 2: '女' }
  return map[userStats.gender] || '未设'
})
const bloodPressureText = computed(() => {
  if (!userStats.systolicPressure || !userStats.diastolicPressure) return '--'
  return `${userStats.systolicPressure}/${userStats.diastolicPressure}`
})
function loadUserStats() {
  const info = uni.getStorageSync('userInfo') || {}
  userStats.height = info.height || '--'
  userStats.weight = info.weight || '--'
  userStats.gender = info.gender ?? 0
  userStats.waistCircumference = info.waistCircumference || null
  userStats.systolicPressure = info.systolicPressure || null
  userStats.diastolicPressure = info.diastolicPressure || null
  userStats.restingHeartRate = info.restingHeartRate || null
  userStats.avgExerciseHeartRate = info.avgExerciseHeartRate || null
  userStats.maxHeartRate = info.maxHeartRate || null
}
function formatMetric(value, unit) {
  if (!value) return '--'
  return `${value}${unit ? ' ' + unit : ''}`
}
function loadCheckinData() {
  const raw = uni.getStorageSync('checkin_records')
  if (raw) {
    try { userStats.checkinDays = Object.keys(JSON.parse(raw)).length } catch (e) { userStats.checkinDays = 0 }
  } else {
    userStats.checkinDays = 0
  }
}
function loadWeekHeatmap() {
  const now = new Date()
  const sevenDaysAgo = new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000)
  const startStr = formatDate(sevenDaysAgo, 'YYYY-MM-DD')
  const endStr = formatDate(now, 'YYYY-MM-DD')
  getWorkoutList({ page: 1, size: 200, startDate: startStr, endDate: endStr })
    .then(res => {
      const list = res.code === 200 && res.data ? (res.data.list || []) : []
      const dayMap = {}
      for (let i = 0; i < 7; i++) {
        const d = new Date(sevenDaysAgo)
        d.setDate(sevenDaysAgo.getDate() + i)
        const key = formatDate(d, 'YYYY-MM-DD')
        dayMap[key] = { date: key, duration: 0, count: 0, dayOfWeek: d.getDay(), label: '' }
      }
      list.forEach(item => {
        const timeStr = item.startTime || item.startTimeStr || ''
        const key = timeStr.substring(0, 10)
        if (dayMap[key]) {
          dayMap[key].duration += (item.duration || 0)
          dayMap[key].count++
        }
      })
      let maxDur = 0
      const entries = Object.entries(dayMap)
      entries.forEach(([key, val]) => {
        if (val.duration > maxDur) maxDur = val.duration
      })
      let peakDayKey = entries[0]?.[0] || ''
      weekHeatData.length = 0
      let totalCount = 0
      let totalDuration = 0
      entries.forEach(([key, val], idx) => {
        const pct = maxDur > 0 ? Math.round((val.duration / maxDur) * 100) : 0
        const color = getHeatColor(pct)
        const d = new Date(key)
        weekHeatData.push({
          date: key,
          dayLabel: '周' + dayLabels[d.getDay()],
          label: val.duration > 0 ? Math.round(val.duration / 60) + '分' : '-',
          pct: pct > 0 ? Math.max(pct, 8) : 4,
          color,
          active: val.duration > 0
        })
        totalCount += val.count
        totalDuration += Math.round(val.duration / 60)
        if (val.duration > (dayMap[peakDayKey]?.duration || 0)) peakDayKey = key
      })
      weekSummary.totalCount = totalCount
      weekSummary.totalDuration = totalDuration
      weekSummary.peakDay = peakDayKey ? formatDate(new Date(peakDayKey), 'MM/DD') : '-'
    })
    .catch(e => console.error('[峰值热度] 加载失败:', e))
}
function getHeatColor(pct) {
  if (pct <= 20) return '#22c55e'
  if (pct <= 40) return '#84cc16'
  if (pct <= 60) return '#eab308'
  if (pct <= 80) return '#f97316'
  return '#ef4444'
}
function onBarClick(day, idx) {
  const item = weekHeatData[idx]
  if (!item.active) return
  uni.showToast({ title: item.date + ' 运动 ' + item.label, icon: 'none' })
}
const editProfile = () => {
  if (userStore.isGuest) {
    uni.showModal({
      title: '提示', content: '游客无法编辑资料，请先注册账号',
      confirmText: '去注册',
      success: (res) => { if (res.confirm) uni.navigateTo({ url: '/pages/user/register' }) }
    })
    return
  }
  uni.navigateTo({ url: '/pages/user/profile-edit' })
}
const goToSettings = () => uni.navigateTo({ url: '/pages/user/settings' })
const goToDesign = () => uni.navigateTo({ url: '/pages/design/design' })
const showAbout = () => {
  uni.showModal({ title: '关于运动App', content: '运动App v1.0.0\n\n记录每一次运动，见证更好的自己。', showCancel: false })
}
const handleLogout = () => {
  const isGuest = userStore.isGuest
  const title = isGuest ? '退出游客模式' : '退出登录'
  const content = isGuest ? '退出后需要重新登录才能使用完整功能' : '确定要退出当前账号吗？'
  uni.showModal({
    title, content, confirmText: '确定', cancelText: '取消',
    success: (res) => { if (res.confirm) userStore.logout() }
  })
}
onMounted(() => { loadUserStats(); loadWeekHeatmap(); loadCheckinData() })
onShow(() => { loadUserStats(); loadWeekHeatmap(); loadCheckinData() })
</script>
<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40rpx;
  transition: background 0.3s;
}
// ===== 用户卡片 =====
.user-card {
  background: var(--bg-card);
  padding: 40rpx 30rpx 30rpx;
  transition: background 0.3s;
}
.user-header {
  display: flex;
  align-items: center;
  padding: 0 10rpx;
}
.user-avatar-wrap {
  width: 96rpx; height: 96rpx; border-radius: 50%;
  border: 2rpx solid var(--border-color);
  overflow: hidden; flex-shrink: 0;
}
.user-avatar { width: 100%; height: 100%; }
.user-info { flex: 1; margin-left: 24rpx; }
.user-name { font-size: 36rpx; font-weight: 600; color: var(--text-primary); }
.header-arrow {
  width: 58rpx;
  height: 58rpx;
  color: var(--text-primary);
  flex-shrink: 0;
}
// ===== 个人数据行 =====
.personal-stats-row {
  display: flex; justify-content: center; align-items: center; margin-top: 32rpx; padding: 0 10rpx;
}
.ps-item {
  display: flex; flex-direction: column; align-items: center; padding: 0 30rpx;
}
.ps-num { font-size: 30rpx; font-weight: 700; color: var(--accent-green); margin-bottom: 4rpx; }
.ps-label { font-size: 22rpx; color: var(--text-tertiary); }
.ps-divider { width: 1rpx; height: 36rpx; background: var(--border-color); }
// ===== 健康档案 =====
.health-section {
  margin: 30rpx;
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 26rpx;
}
.health-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.health-title { font-size: 32rpx; font-weight: 700; color: var(--text-primary); }
.health-edit {
  display: flex;
  align-items: center;
  gap: 4rpx;
  color: var(--accent-green);
  font-size: 24rpx;
}
.health-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}
.health-item {
  background: var(--bg-secondary);
  border-radius: 16rpx;
  padding: 20rpx;
}
.health-value {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6rpx;
}
.health-label { font-size: 22rpx; color: var(--text-tertiary); }
// ===== 近七天热度图 =====
.heatmap-section {
  margin: 30rpx; background: var(--bg-card); border-radius: 20rpx; overflow: hidden; padding: 28rpx;
  transition: background 0.3s;
}
.heatmap-header { margin-bottom: 24rpx; }
.heatmap-title { font-size: 32rpx; font-weight: 700; color: var(--text-primary); }
.heatmap-body { display: flex; gap: 24rpx; }
.heatmap-chart {
  flex: 1; display: flex; align-items: flex-end; justify-content: space-between; height: 240rpx;
}
.hm-bar-wrap {
  display: flex; flex-direction: column; align-items: center; flex: 1; gap: 8rpx;
}
.hm-bar-column {
  flex: 1; width: 40rpx; display: flex; align-items: flex-end; justify-content: center;
}
.hm-bar-fill {
  width: 100%; border-radius: 8rpx; min-height: 6rpx; transition: height 0.4s;
}
.hm-bar-value {
  font-size: 20rpx; color: var(--text-secondary); white-space: nowrap;
}
.hm-bar-label { font-size: 22rpx; color: var(--text-tertiary); }
.heatmap-stats {
  width: 200rpx; display: flex; flex-direction: column; justify-content: center; gap: 20rpx;
}
.hm-stat { display: flex; flex-direction: column; gap: 4rpx; }
.hm-stat-val { font-size: 32rpx; font-weight: 700; color: var(--text-primary); }
.hm-stat-label { font-size: 22rpx; color: var(--text-tertiary); }
// 热度色阶
.heatmap-legend {
  display: flex; align-items: center; gap: 8rpx; margin-top: 16rpx;
}
.legend-label { font-size: 20rpx; color: var(--text-tertiary); }
.legend-bar { flex: 1; display: flex; height: 12rpx; border-radius: 6rpx; overflow: hidden; }
.legend-step { flex: 1; }
// ===== 菜单区域 =====
.menu-section {
  margin: 30rpx; background: var(--bg-card); border-radius: 20rpx; overflow: hidden;
  transition: background 0.3s;
}
.menu-title {
  font-size: 28rpx; color: var(--text-tertiary); padding: 24rpx 30rpx 16rpx;
  border-bottom: 1rpx solid var(--border-color);
}
.menu-list { padding: 0 20rpx; }
.menu-item {
  display: flex; align-items: center; padding: 28rpx 20rpx;
  border-bottom: 1rpx solid var(--border-color);
  &:last-child { border-bottom: none; }
  &:active { background: var(--bg-secondary); }
}
.menu-icon { font-size: 40rpx; margin-right: 20rpx; }
.menu-text { flex: 1; font-size: 30rpx; color: var(--text-primary); }
.menu-arrow {
  width: 52rpx;
  height: 52rpx;
  color: var(--text-secondary);
  flex-shrink: 0;
}
// ===== 主题开关 =====
.theme-switch {
  width: 96rpx; height: 52rpx; background: var(--border-color); border-radius: 26rpx; position: relative;
  transition: background 0.3s; flex-shrink: 0;
  &.switch-on {
    background: var(--accent-purple);
    .switch-knob { left: 48rpx; }
  }
}
.switch-knob {
  position: absolute; top: 4rpx; left: 4rpx;
  width: 44rpx; height: 44rpx; background: var(--bg-card); border-radius: 50%;
  box-shadow: 0 2rpx 8rpx var(--shadow-color);
  transition: left 0.3s;
  border: 1rpx solid var(--border-color);
}
// ===== 退出 =====
.logout-section { margin: 40rpx 30rpx; }
.logout-btn {
  width: 100%; height: 90rpx; background: var(--bg-card); color: #f56c6c;
  font-size: 32rpx; border-radius: 16rpx; border: none;
  &:active { background: #fef0f0; }
}
.version-info { text-align: center; padding: 20rpx; text { font-size: 24rpx; color: var(--text-tertiary); } }
</style>
