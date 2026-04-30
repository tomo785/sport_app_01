<template>
  <view class="container">
    <!-- 顶部信息 -->
    <view class="header" :style="{ background: getCoverBg(detail.level) }">
      <text class="h-emoji">{{ getLevelEmoji(detail.level) }}</text>
      <text class="h-name">{{ detail.name }}</text>
      <text class="h-desc" v-if="detail.description">{{ detail.description }}</text>
      <view class="h-tags">
        <text class="h-tag">{{ detail.levelName || '新手' }}</text>
        <text class="h-tag">{{ detail.totalWeeks || 4 }}周</text>
        <text class="h-tag" v-if="detail.workoutsPerWeek">每周{{ detail.workoutsPerWeek }}练</text>
      </view>
      <view class="h-stats">
        <text class="hs-item">⭐ {{ (detail.rating || 0).toFixed(1) }}</text>
        <text class="hs-item">👥 {{ detail.enrollCount || 0 }}人使用</text>
        <text class="hs-item">👀 {{ detail.viewCount || 0 }}次浏览</text>
      </view>

      <view class="h-actions" v-if="detail.userStatus !== 1">
        <view class="ha-btn" @click="doAdopt">采用此计划</view>
      </view>
      <view class="h-adopted" v-else>
        <text>✓ 已采用 · 第{{ detail.currentWeek || 1 }}/{{ detail.totalWeeks }}周</text>
      </view>
    </view>

    <!-- 课程详情 -->
    <scroll-view class="body" scroll-y>
      <view class="section-label">训练安排</view>

      <block v-for="(courses, weekLabel) in groupedCourses" :key="weekLabel">
        <view class="week-section">
          <view class="ws-header" @click="toggleWeek(weekLabel)">
            <text class="ws-title">{{ weekLabel }}</text>
            <text class="ws-toggle">{{ expandedWeeks[weekLabel] ? '▲' : '▼' }}</text>
          </view>

          <view class="ws-body" v-if="expandedWeeks[weekLabel]">
            <view class="day-group" v-for="(dayCourses, dayLabel) in courses" :key="dayLabel">
              <text class="dg-label">星期{{ dayLabelsObj[dayLabel] }}</text>
              <view class="course-card" v-for="course in dayCourses" :key="course.id">
                <view class="cc-head">
                  <text class="cc-emoji">{{ getTypeEmoji(course.type) }}</text>
                  <text class="cc-name">{{ course.name }}</text>
                  <text class="cc-time" v-if="course.duration">{{ course.duration }}分钟</text>
                </view>

                <view class="cc-steps" v-if="course.exercises && course.exercises.length > 0">
                  <view class="cc-step" v-for="(ex, ei) in course.exercises" :key="ei">
                    <view class="cs-dot"></view>
                    <text class="cs-name">{{ ex.name }}</text>
                    <text class="cs-meta">
                      <text v-if="ex.duration">{{ formatTime(ex.duration) }}</text>
                      <text v-if="ex.sets && ex.reps">{{ ex.sets }}组×{{ ex.reps }}次</text>
                      <text v-if="ex.distance">{{ (ex.distance / 1000).toFixed(1) }}km</text>
                      <text v-if="ex.restTime"> 休息{{ ex.restTime }}s</text>
                    </text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </block>

      <view class="empty-state" v-if="Object.keys(groupedCourses).length === 0">
        <text class="empty-icon">📭</text>
        <text class="empty-title">暂无训练内容</text>
      </view>

      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { getPlanDetail, adoptPlan } from '@/api/plan'

const detail = reactive({
  id: null,
  name: '',
  description: '',
  level: 1,
  levelName: '',
  totalWeeks: 4,
  rating: 0,
  enrollCount: 0,
  viewCount: 0,
  currentWeek: 1,
  userStatus: 0,
  courses: []
})

const expandedWeeks = reactive({})
const dayLabelsObj = { 1: '一', 2: '二', 3: '三', 4: '四', 5: '五', 6: '六', 7: '日' }

const groupedCourses = computed(() => {
  const groups = {}
  if (!detail.courses) return groups

  detail.courses.forEach(c => {
    const weekKey = `第${c.week}周`
    if (!groups[weekKey]) groups[weekKey] = {}
    if (!groups[weekKey][c.day]) groups[weekKey][c.day] = []
    groups[weekKey][c.day].push(c)
  })

  // 默认展开第一周
  if (Object.keys(groups).length > 0 && Object.keys(expandedWeeks).length === 0) {
    expandedWeeks[Object.keys(groups)[0]] = true
  }

  return groups
})

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  if (page && page.options && page.options.id) {
    loadDetail(Number(page.options.id))
  }
})

function loadDetail(id) {
  getPlanDetail(id).then(res => {
    if (res.code === 200 && res.data) {
      Object.assign(detail, res.data)
      // 展开所有周
      Object.keys(groupedCourses.value).forEach(k => {
        expandedWeeks[k] = true
      })
    }
  }).catch(() => {
    uni.showToast({ title: '加载失败', icon: 'none' })
  })
}

function toggleWeek(key) {
  expandedWeeks[key] = !expandedWeeks[key]
}

function doAdopt() {
  adoptPlan(detail.id).then(() => {
    uni.showToast({ title: '已采用', icon: 'success' })
    detail.userStatus = 1
  }).catch(() => {
    uni.showToast({ title: '采用失败', icon: 'none' })
  })
}

function getTypeEmoji(type) {
  const emojis = { 1: '🏃', 2: '💪', 3: '🧘', 4: '⚡', 5: '🔀', 6: '🧊', 7: '🔀' }
  return emojis[type] || '📌'
}

function getCoverBg(level) {
  const colors = {
    1: 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)',
    2: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
    3: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    4: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
  }
  return colors[level] || colors[1]
}

function getLevelEmoji(level) {
  const emojis = { 1: '🌱', 2: '🔥', 3: '💪', 4: '🏆' }
  return emojis[level] || '🌱'
}

function formatTime(seconds) {
  if (seconds >= 60) {
    const min = Math.floor(seconds / 60)
    const sec = seconds % 60
    return sec > 0 ? `${min}分${sec}秒` : `${min}分钟`
  }
  return `${seconds}秒`
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 40rpx 28rpx 32rpx;
  color: #fff;
}

.h-emoji {
  font-size: 60rpx;
  display: block;
}

.h-name {
  font-size: 38rpx;
  font-weight: 800;
  display: block;
  margin-top: 12rpx;
}

.h-desc {
  font-size: 26rpx;
  opacity: 0.85;
  display: block;
  margin-top: 8rpx;
}

.h-tags {
  display: flex;
  gap: 14rpx;
  margin-top: 16rpx;
}

.h-tag {
  font-size: 22rpx;
  padding: 6rpx 18rpx;
  border-radius: 6rpx;
  background: rgba(255, 255, 255, 0.2);
}

.h-stats {
  display: flex;
  gap: 24rpx;
  margin-top: 16rpx;
}

.hs-item {
  font-size: 24rpx;
  opacity: 0.8;
}

.h-actions {
  margin-top: 24rpx;
}

.ha-btn {
  height: 80rpx;
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.95);
  color: #16a34a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 700;
}

.h-adopted {
  margin-top: 20rpx;
  font-size: 26rpx;
  opacity: 0.9;
}

.body {
  flex: 1;
  padding: 20rpx 24rpx;
}

.section-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16rpx;
}

.week-section {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
}

.ws-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #f8fafc;
}

.ws-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1e293b;
}

.ws-toggle {
  font-size: 22rpx;
  color: #94a3b8;
}

.ws-body {
  padding: 0 24rpx 20rpx;
}

.day-group {
  margin-top: 16rpx;
}

.dg-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #64748b;
  display: block;
  margin-bottom: 10rpx;
}

.course-card {
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 10rpx;
}

.cc-head {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.cc-emoji {
  font-size: 26rpx;
}

.cc-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  flex: 1;
}

.cc-time {
  font-size: 24rpx;
  color: #22c55e;
  font-weight: 600;
}

.cc-steps {
  margin-top: 12rpx;
  padding-left: 8rpx;
}

.cc-step {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 8rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}

.cs-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #cbd5e1;
  flex-shrink: 0;
}

.cs-name {
  font-size: 26rpx;
  color: #475569;
}

.cs-meta {
  font-size: 22rpx;
  color: #94a3b8;
  margin-left: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 80rpx;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #334155;
  margin-top: 16rpx;
}

.bottom-spacer {
  height: 40rpx;
}
</style>
