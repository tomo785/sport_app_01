<template>
  <view class="container">
    <!-- 顶部抬头 -->
    <view class="page-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-top">
        <view class="header-spacer"></view>
        <view class="header-center">
          <text class="header-title">运动计划</text>
          <text class="header-sub">定制你的专属训练</text>
        </view>
        <view class="header-add" @click="goCreatePlan">
          <text class="add-icon">+</text>
        </view>
      </view>
    </view>

    <scroll-view class="page-body" scroll-y :refresher-enabled="true"
      :refresher-triggered="refreshing" @refresherrefresh="onRefresh">

      <!-- ===== 本周训练概览 ===== -->
      <view class="week-overview" @click="goTodayPlan">
        <view class="wo-left">
          <view class="wo-ring-box">
            <svg class="wo-ring-svg" viewBox="0 0 120 120">
              <circle cx="60" cy="60" r="50" fill="none" stroke="var(--border-color)" stroke-width="10" />
              <circle cx="60" cy="60" r="50" fill="none" stroke="var(--accent-green)" stroke-width="10"
                stroke-linecap="round"
                :stroke-dasharray="ringCircumference"
                :stroke-dashoffset="ringCircumference - (weekProgress / 100) * ringCircumference"
                transform="rotate(-90 60 60)" />
            </svg>
            <view class="wo-ring-text-box">
              <text class="wo-ring-num">{{ weekDoneDays }}</text>
              <text class="wo-ring-den">/{{ weekTotalDays }}</text>
            </view>
          </view>
        </view>
        <view class="wo-right">
          <view class="wo-header">
            <text class="wo-title">本周训练</text>
            <text class="wo-subtitle">第 {{ weekNum }} 周</text>
          </view>
          <view class="wo-week-row">
            <view class="wo-day-item" v-for="(d, i) in weekDayPlans" :key="i">
              <view class="wo-day-dot" :class="d.cls"></view>
              <text class="wo-day-label">{{ dayNames[i] }}</text>
            </view>
          </view>
          <text class="wo-desc">已完成 {{ weekDoneDays }} 天训练，{{ weekRemainDays > 0 ? '还剩 ' + weekRemainDays + ' 天' : '本周目标达成！' }}</text>
        </view>
      </view>

      <!-- ===== 我的计划 ===== -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">我的计划</text>
        </view>

        <view class="plan-cards" v-if="myPlans.length > 0">
          <view class="plan-item" v-for="(plan, idx) in myPlans" :key="plan.id">
            <view class="pi-top">
              <view class="pi-name-row">
                <view class="pi-level-dot" :style="{ background: getLevelColor(plan.level) }"></view>
                <text class="pi-name">{{ plan.name }}</text>
              </view>
              <text class="pi-level-tag" :style="{ color: getLevelColor(plan.level), background: getLevelColor(plan.level) + '18' }">
                {{ plan.levelName || '入门' }}
              </text>
            </view>

            <!-- 进度条 -->
            <view class="pi-progress-row">
              <view class="pi-bar-track">
                <view class="pi-bar-fill" :style="{ width: getProgressPct(plan) + '%', background: getLevelColor(plan.level) }"></view>
              </view>
              <text class="pi-bar-text">{{ getProgressPct(plan) }}%</text>
            </view>

            <!-- 7天训练详情（展开式） -->
            <view class="pi-day-list">
              <view class="pi-day-item" v-for="d in 7" :key="d" @click.stop="toggleDayExpand(plan.id, d)">
                <view class="pi-day-head">
                  <text class="pi-day-label">周{{ dayNames[d-1] }}</text>
                  <text class="pi-day-status">{{ getDayStatusText(plan, d) }}</text>
                </view>
                <!-- 展开后显示活动详情 -->
                <view class="pi-day-detail" v-if="isDayExpanded(plan.id, d) && getDayCourses(plan, d).length > 0">
                  <view class="pi-act-card" v-for="(c, ci) in getDayCourses(plan, d)" :key="ci">
                    <text class="pi-act-icon">{{ courseTypeIcon(c.type) }}</text>
                    <text class="pi-act-name">{{ c.name }}</text>
                    <text class="pi-act-dur" v-if="c.duration">{{ c.duration }}分</text>
                  </view>
                  <view class="pi-act-card pi-act-rest" v-if="isRestDay(plan, d)">
                    <text class="pi-act-icon">😴</text>
                    <text class="pi-act-name">休息自由活动</text>
                  </view>
                </view>
              </view>
            </view>

            <view class="pi-footer">
              <text class="pi-stat">{{ plan.totalWeeks || 4 }} 周 · 每周 {{ getWorkoutsPerWeek(plan) }} 练</text>
              <view class="pi-actions">
                <text class="pi-action pi-action-edit" @click.stop="editPlan(plan)">修改</text>
                <text class="pi-action pi-action-del" @click.stop="confirmDeletePlan(plan)">删除</text>
              </view>
            </view>
          </view>
        </view>

        <view class="plan-empty" v-else>
          <view class="plan-empty-icon">📋</view>
          <text class="plan-empty-text">还没有训练计划</text>
          <text class="plan-empty-hint">创建一个计划，开始你的运动之旅</text>
        </view>
      </view>

      <!-- ===== 小项目设计器 ===== -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">小项目设计器</text>
          <text class="section-more" @click="goActivityEditor">创建完整活动 ›</text>
        </view>

        <view class="designer-section">
          <!-- tabs -->
          <view class="ds-tabs">
            <text class="ds-tab" :class="{ active: dsTab === 'preset' }" @click="dsTab = 'preset'">默认项目</text>
            <text class="ds-tab" :class="{ active: dsTab === 'custom' }" @click="dsTab = 'custom'">自定义项目</text>
          </view>

          <!-- 默认项目列表 -->
          <view class="ds-grid" v-if="dsTab === 'preset'">
            <view class="ds-card" v-for="(item, idx) in presetExercises" :key="idx">
              <text class="ds-card-icon">{{ item.icon }}</text>
              <view class="ds-card-info">
                <text class="ds-card-name">{{ item.name }}</text>
                <text class="ds-card-meta">{{ item.meta }}</text>
              </view>
              <text class="ds-card-add" @click="quickAddToMyActivities(item)">+</text>
            </view>
          </view>

          <!-- 自定义项目列表 -->
          <view class="ds-custom" v-if="dsTab === 'custom'">
            <!-- 已有自定义活动 -->
            <view class="ds-custom-list" v-if="myActivitiesList.length > 0">
              <view class="ds-custom-item" v-for="(act, idx) in myActivitiesList" :key="act.id || idx">
                <text class="ds-ci-icon">⚡</text>
                <view class="ds-ci-info">
                  <text class="ds-ci-name">{{ act.title || act.name }}</text>
                  <text class="ds-ci-meta">{{ (act.exercises || []).length }} 个步骤</text>
                </view>
                <text class="ds-ci-del" @click="deleteMyActivity(idx)">✕</text>
              </view>
            </view>

            <!-- 新建自定义活动表单 -->
            <view class="ds-custom-form">
              <text class="ds-cf-title">新建自定义项目</text>
              <input class="ds-cf-input" v-model="customActName" placeholder="项目名称（如：晨间唤醒组合）" />
              <view class="ds-cf-presets">
                <text class="ds-cf-preset" v-for="(ex, i) in selectedPresets" :key="i">
                  {{ ex.name }}
                  <text class="ds-cf-preset-del" @click="removePreset(i)">✕</text>
                </text>
              </view>
              <view class="ds-cf-row">
                <picker class="ds-cf-picker" :range="presetExerciseNames" :value="presetPickIdx"
                  @change="onPresetPickChange">
                  <text>{{ presetPickIdx >= 0 ? presetExerciseNames[presetPickIdx] : '选择预设步骤' }}</text>
                </picker>
                <view class="ds-cf-addstep" @click="addPresetToCustom">添加步骤</view>
              </view>
              <view class="ds-cf-save" @click="saveCustomActivity">保存自定义项目</view>
            </view>
          </view>
        </view>
      </view>

      <!-- ===== 快捷入口 ===== -->
      <view class="quick-actions">
        <view class="qa-btn qa-primary" @click="goCreatePlan">
          <text class="qa-btn-icon">+</text>
          <text class="qa-btn-text">创建新计划</text>
        </view>
        <view class="qa-btn qa-secondary" @click="goCommunity">
          <text class="qa-btn-icon">🌐</text>
          <text class="qa-btn-text">探索社区</text>
        </view>
      </view>

      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getMyPlans, deletePlan, getPlanDetail, updatePlan,
  createDefaultWeeklyPlan
} from '@/api/plan'

const statusBarHeight = ref(0)
const refreshing = ref(false)
const myPlans = ref([])
const expandedDays = ref({}) // { 'planId_day': true }

const dayNames = ['一', '二', '三', '四', '五', '六', '日']

const ringCircumference = 2 * Math.PI * 50

const courseTypeIcons = { 1: '🏃', 2: '💪', 3: '🧘', 4: '⚡', 5: '⚡', 6: '😴' }
const courseTypeNames = { 1: '跑步', 2: '力量', 3: '拉伸', 4: 'HIIT', 5: '综合', 6: '休息' }

// ---- 小项目设计器 ----
const dsTab = ref('preset')
const customActName = ref('')
const selectedPresets = ref([])
const presetPickIdx = ref(-1)
const myActivitiesList = ref([])

const presetExercises = [
  { icon: '🏃', name: '慢跑热身', meta: '5-10分 · 1-2km', type: 1, duration: 600, distance: 2000 },
  { icon: '🏃', name: '400米冲刺', meta: '90秒 · 1组', type: 1, duration: 90, distance: 400 },
  { icon: '🏃', name: '800米间歇', meta: '180秒 · 4组', type: 1, duration: 180, distance: 800, sets: 4 },
  { icon: '💪', name: '深蹲', meta: '4组×12次', type: 2, sets: 4, reps: 12 },
  { icon: '💪', name: '俯卧撑', meta: '4组×15次', type: 2, sets: 4, reps: 15 },
  { icon: '💪', name: '平板支撑', meta: '3组×60秒', type: 2, sets: 3, reps: 60 },
  { icon: '💪', name: '卷腹', meta: '3组×20次', type: 2, sets: 3, reps: 20 },
  { icon: '💪', name: '弓步蹲', meta: '3组×12次/腿', type: 2, sets: 3, reps: 12 },
  { icon: '🧘', name: '全身拉伸', meta: '10分钟', type: 3, duration: 600 },
  { icon: '🧘', name: '哈他瑜伽', meta: '30分钟', type: 3, duration: 1800 },
  { icon: '🧘', name: '泡沫轴放松', meta: '15分钟', type: 3, duration: 900 },
  { icon: '🚶', name: '缓和散步', meta: '5-10分 · 0.5km', type: 1, duration: 300, distance: 500 },
]

const presetExerciseNames = computed(() => presetExercises.map(e => e.name))

function goActivityEditor() {
  uni.navigateTo({ url: '/pages/goal/activity-editor' })
}

function loadMyActivities() {
  try {
    const raw = uni.getStorageSync('userActivities')
    if (raw) {
      const list = typeof raw === 'string' ? JSON.parse(raw) : raw
      myActivitiesList.value = Array.isArray(list) ? list : []
    }
  } catch (e) { myActivitiesList.value = [] }
}

function onPresetPickChange(e) {
  presetPickIdx.value = e.detail.value
}

function addPresetToCustom() {
  if (presetPickIdx.value < 0) { uni.showToast({ title: '请选择一个预设步骤', icon: 'none' }); return }
  const ex = presetExercises[presetPickIdx.value]
  if (selectedPresets.value.some(s => s.name === ex.name)) {
    uni.showToast({ title: '该步骤已添加', icon: 'none' }); return
  }
  selectedPresets.value.push({ ...ex })
  presetPickIdx.value = -1
}

function removePreset(idx) {
  selectedPresets.value.splice(idx, 1)
}

function saveCustomActivity() {
  if (!customActName.value.trim()) { uni.showToast({ title: '请输入项目名称', icon: 'none' }); return }
  if (selectedPresets.value.length === 0) { uni.showToast({ title: '请至少添加一个步骤', icon: 'none' }); return }

  const newAct = {
    id: Date.now(),
    title: customActName.value.trim(),
    type: 1,
    duration: selectedPresets.value.reduce((s, e) => s + (e.duration || 60), 0) / 60,
    exercises: selectedPresets.value.map(e => ({
      name: e.name, type: e.type, duration: e.duration || 60,
      sets: e.sets || null, reps: e.reps || null, distance: e.distance || null
    }))
  }

  myActivitiesList.value.push(newAct)
  uni.setStorageSync('userActivities', JSON.stringify(myActivitiesList.value))
  customActName.value = ''
  selectedPresets.value = []
  uni.showToast({ title: '自定义项目已保存', icon: 'success' })
}

function quickAddToMyActivities(item) {
  const newAct = {
    id: Date.now() + Math.random(),
    title: item.name,
    type: item.type || 1,
    duration: (item.duration || 60) / 60,
    exercises: [{ name: item.name, type: item.type || 1, duration: item.duration || 60, sets: item.sets || null, reps: item.reps || null }]
  }
  myActivitiesList.value.push(newAct)
  uni.setStorageSync('userActivities', JSON.stringify(myActivitiesList.value))
  uni.showToast({ title: `已添加「${item.name}」到自定义项目`, icon: 'success' })
}

function deleteMyActivity(idx) {
  uni.showModal({
    title: '删除项目',
    content: '确定删除该自定义项目？',
    success: (res) => {
      if (!res.confirm) return
      myActivitiesList.value.splice(idx, 1)
      uni.setStorageSync('userActivities', JSON.stringify(myActivitiesList.value))
    }
  })
}

function courseTypeIcon(type) { return courseTypeIcons[type] || '🏃' }
function courseTypeName(type) { return courseTypeNames[type] || '运动' }

function toggleDayExpand(planId, day) {
  const key = planId + '_' + day
  expandedDays.value[key] = !expandedDays.value[key]
}
function isDayExpanded(planId, day) {
  return !!expandedDays.value[planId + '_' + day]
}

function getDayCourses(plan, dayOfWeek) {
  if (!plan.courses) return []
  return plan.courses.filter(c => c.day === dayOfWeek)
}

function getDayStatusText(plan, dayOfWeek) {
  const courses = getDayCourses(plan, dayOfWeek)
  if (courses.length === 0) return '—'
  if (courses.some(c => c.type === 6)) return '😴 休息'
  const names = courses.map(c => courseTypeName(c.type))
  return [...new Set(names)].join(' · ')
}

// ---- 本周训练概览数据 ----
const weekDayPlans = ref(Array(7).fill({ cls: 'dot-none' }))
const weekDoneDays = ref(0)
const weekTotalDays = ref(7)
const weekNum = ref(1)
const weekProgress = computed(() => weekTotalDays.value > 0 ? Math.round((weekDoneDays.value / weekTotalDays.value) * 100) : 0)
const weekRemainDays = computed(() => Math.max(0, weekTotalDays.value - weekDoneDays.value))

// 周二到周日的 dayOfWeek 映射 (getDay() 返回 0=周日)
function getDayOfWeek(date = new Date()) {
  const d = date.getDay()
  return d === 0 ? 7 : d
}

function getWeekNumber() {
  const now = new Date()
  const start = new Date(now.getFullYear(), 0, 1)
  const diff = now - start
  return Math.ceil((diff / 86400000 + start.getDay() + 1) / 7)
}

function loadWeekOverview() {
  const todayDay = getDayOfWeek()
  const raw = uni.getStorageSync('weeklyPlan_current')
  const plans = []

  if (raw) {
    try {
      const weekly = JSON.parse(raw)
      const days = weekly.days || []
      // 重置一周计划
      for (let i = 0; i < 7; i++) {
        const dayPlan = days.find(d => d.dayOfWeek === i + 1)
        if (dayPlan) {
          const isRest = dayPlan.type === 'rest'
          const isDone = dayPlan.isCompleted
          let cls = 'dot-rest'
          if (isDone) cls = 'dot-done'
          else if (!isRest) cls = 'dot-active'
          plans.push({ cls, plan: dayPlan })
        } else {
          plans.push({ cls: 'dot-none', plan: null })
        }
      }
      weekNum.value = getWeekNumber()
    } catch (e) {
      for (let i = 0; i < 7; i++) plans.push({ cls: 'dot-none', plan: null })
    }
  } else {
    for (let i = 0; i < 7; i++) plans.push({ cls: 'dot-none', plan: null })
  }
  weekDayPlans.value = plans

  // 计算已完成天数：从 checkin_records 获取本周完成情况
  const now = new Date()
  const currentDay = now.getDay()
  const monday = new Date(now)
  monday.setDate(now.getDate() - (currentDay === 0 ? 6 : currentDay - 1))
  let done = 0
  const checkinRaw = uni.getStorageSync('checkin_records')
  if (checkinRaw) {
    try {
      const records = JSON.parse(checkinRaw)
      for (let i = 0; i < 7; i++) {
        const d = new Date(monday)
        d.setDate(monday.getDate() + i)
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
        if (records[key]) done++
      }
    } catch (e) {}
  }
  weekDoneDays.value = done
}

// ---- 计划数据 ----
function loadMyPlans() {
  getMyPlans()
    .then(res => { if (res.code === 200 && res.data) myPlans.value = res.data })
    .catch(() => loadLocalPlans())
    .finally(() => { refreshing.value = false })
}

function loadLocalPlans() {
  const saved = uni.getStorageSync('myTrainingPlans')
  myPlans.value = saved ? (() => { try { return JSON.parse(saved) } catch (e) { return [] } })() : []
  // 如果没有任何计划，自动创建默认计划
  if (myPlans.value.length === 0) {
    createDefaultWeeklyPlan()
    const savedAgain = uni.getStorageSync('myTrainingPlans')
    if (savedAgain) {
      try { myPlans.value = JSON.parse(savedAgain) } catch (e) {}
    }
  }
}

function isRestDay(plan, dayOfWeek) {
  if (!plan.courses) return false
  return plan.courses.some(c => c.day === dayOfWeek && c.type === 6)
}

function getProgressPct(plan) {
  if (!plan.totalWeeks) return 0
  if (plan.totalCourses > 0) return Math.round((plan.completedCourses || 0) / plan.totalCourses * 100)
  return Math.round((plan.currentWeek || 0) / plan.totalWeeks * 100)
}

function getWorkoutsPerWeek(plan) {
  if (!plan.courses || !plan.courses.length) return 0
  const days = new Set(plan.courses.map(c => c.day))
  return days.size
}

function getLevelColor(level) {
  const c = { 1: '#22c55e', 2: '#3b82f6', 3: '#f59e0b', 4: '#ef4444' }
  return c[level] || c[1]
}

// ---- 导航 ----
function goCreatePlan() {
  uni.navigateTo({ url: '/pages/goal/create' })
}

function goCommunity() {
  uni.navigateTo({ url: '/pages/goal/community' })
}

function goTodayPlan() {
  uni.switchTab({ url: '/pages/index/index' })
}

function editPlan(plan) {
  uni.navigateTo({ url: `/pages/goal/create?id=${plan.id}` })
}

function confirmDeletePlan(plan) {
  uni.showModal({
    title: '删除计划',
    content: `确定删除"${plan.name}"？不可恢复。`,
    success: res => {
      if (!res.confirm) return
      deletePlan(plan.id)
        .then(() => { myPlans.value = myPlans.value.filter(p => p.id !== plan.id) })
        .catch(() => { myPlans.value = myPlans.value.filter(p => p.id !== plan.id) })
    }
  })
}

function onRefresh() {
  refreshing.value = true
  loadMyPlans()
}

// ---- 生命周期 ----
onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
  loadWeekOverview()
  loadMyPlans()
  loadMyActivities()
})

onShow(() => {
  loadWeekOverview()
  loadMyPlans()
  loadMyActivities()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

// ===== 顶部抬头 =====
.page-header {
  background: var(--bg-card);
  padding: 0 28rpx;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
}

.header-spacer { width: 64rpx; }

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-title { font-size: 40rpx; font-weight: 700; color: var(--text-primary); }
.header-sub { font-size: 24rpx; color: var(--text-tertiary); margin-top: 2rpx; }

.header-add {
  width: 64rpx; height: 64rpx;
  display: flex; align-items: center; justify-content: center;
}

.add-icon { font-size: 48rpx; color: var(--accent-green); font-weight: 300; line-height: 1; }

// ===== 页面滚动区 =====
.page-body {
  flex: 1;
  padding: 0 28rpx;
}

// ===== 本周训练概览 =====
.week-overview {
  margin: 28rpx 0;
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  gap: 32rpx;
  align-items: center;
  transition: background 0.3s;
}

.wo-left { flex-shrink: 0; }

.wo-ring-box {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wo-ring-svg {
  width: 160rpx;
  height: 160rpx;
  transform: rotate(0deg);
}

.wo-ring-text-box {
  position: absolute;
  display: flex;
  align-items: baseline;
  gap: 2rpx;
}

.wo-ring-num { font-size: 44rpx; font-weight: 800; color: var(--accent-green); }
.wo-ring-den { font-size: 24rpx; color: var(--text-tertiary); }

.wo-right { flex: 1; min-width: 0; }

.wo-header {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.wo-title { font-size: 34rpx; font-weight: 700; color: var(--text-primary); }
.wo-subtitle { font-size: 24rpx; color: var(--text-tertiary); }

.wo-week-row {
  display: flex;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.wo-day-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  flex: 1;
}

.wo-day-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;

  &.dot-active {
    background: var(--accent-green);
    box-shadow: 0 0 0 4rpx rgba(34, 197, 94, 0.2);
  }
  &.dot-rest {
    background: var(--text-tertiary);
    opacity: 0.5;
  }
  &.dot-done {
    background: var(--accent-blue);
  }
  &.dot-none {
    background: var(--border-color);
  }
}

.wo-day-label { font-size: 20rpx; color: var(--text-tertiary); }

.wo-desc { font-size: 24rpx; color: var(--text-tertiary); line-height: 1.4; }

// ===== 通用 section =====
.section { margin-bottom: 28rpx; }

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title { font-size: 34rpx; font-weight: 700; color: var(--text-primary); }

.section-more { font-size: 24rpx; color: var(--accent-blue); font-weight: 500; }

// ===== 我的计划（卡片式） =====
.plan-cards {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.plan-item {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx;
  transition: background 0.3s;
}

.pi-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.pi-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.pi-level-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.pi-name { font-size: 32rpx; font-weight: 700; color: var(--text-primary); }

.pi-level-tag {
  font-size: 20rpx;
  font-weight: 600;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

// 进度条
.pi-progress-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.pi-bar-track {
  flex: 1;
  height: 8rpx;
  background: var(--border-color);
  border-radius: 4rpx;
  overflow: hidden;
}

.pi-bar-fill {
  height: 100%;
  border-radius: 4rpx;
  transition: width 0.5s;
}

.pi-bar-text { font-size: 24rpx; font-weight: 600; color: var(--text-secondary); flex-shrink: 0; }

// ===== 7天展开式日视图 =====
.pi-day-list {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-bottom: 20rpx;
}

.pi-day-item {
  background: var(--bg-secondary);
  border-radius: 12rpx;
  overflow: hidden;
}

.pi-day-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 20rpx;
  &:active { opacity: 0.7; }
}

.pi-day-label {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-secondary);
  width: 60rpx;
}

.pi-day-status {
  flex: 1;
  text-align: right;
  font-size: 22rpx;
  color: var(--text-tertiary);
}

.pi-day-detail {
  padding: 0 20rpx 12rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.pi-act-card {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  background: var(--bg-card);
  border-radius: 10rpx;

  &.pi-act-rest {
    background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f5 100%);
    border: 1rpx dashed var(--border-color);
  }
}

.pi-act-icon { font-size: 28rpx; width: 36rpx; text-align: center; }

.pi-act-name {
  flex: 1;
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.pi-act-dur {
  font-size: 22rpx;
  color: var(--accent-green);
  font-weight: 500;
}

// 底部
.pi-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid var(--border-color);
}

.pi-stat { font-size: 24rpx; color: var(--text-tertiary); }

.pi-actions {
  display: flex;
  gap: 16rpx;
}

.pi-action {
  font-size: 24rpx;
  font-weight: 500;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;

  &:active { opacity: 0.7; }
}

.pi-action-edit {
  color: var(--accent-blue);
  background: var(--bg-secondary);
}

.pi-action-del {
  color: #ef4444;
  background: var(--bg-secondary);
}

// 空计划
.plan-empty {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 80rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  transition: background 0.3s;
}

.plan-empty-icon { font-size: 80rpx; opacity: 0.6; }

.plan-empty-text { font-size: 30rpx; font-weight: 600; color: var(--text-secondary); }

.plan-empty-hint { font-size: 24rpx; color: var(--text-tertiary); }

// ===== 小项目设计器 =====
.designer-section {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 24rpx;
  transition: background 0.3s;
}

.ds-tabs {
  display: flex; gap: 12rpx; margin-bottom: 20rpx;
}

.ds-tab {
  flex: 1; text-align: center; padding: 14rpx; border-radius: 12rpx;
  font-size: 26rpx; font-weight: 500;
  color: var(--text-tertiary); background: var(--bg-secondary);
  border: 1rpx solid var(--border-color);
  transition: all 0.2s;

  &.active {
    color: var(--accent-green); background: var(--bg-secondary);
    border-color: var(--accent-green); font-weight: 700;
  }
}

// 默认项目网格
.ds-grid {
  display: flex; flex-wrap: wrap; gap: 12rpx;
}

.ds-card {
  display: flex; align-items: center; gap: 10rpx;
  padding: 14rpx 16rpx; border-radius: 12rpx;
  background: var(--bg-secondary); border: 1rpx solid var(--border-color);
  min-width: calc(50% - 6rpx); box-sizing: border-box;
  position: relative;
}

.ds-card-icon { font-size: 32rpx; width: 44rpx; text-align: center; }

.ds-card-info { flex: 1; min-width: 0; }

.ds-card-name {
  font-size: 26rpx; font-weight: 600; color: var(--text-primary); display: block;
}

.ds-card-meta {
  font-size: 20rpx; color: var(--text-tertiary); margin-top: 2rpx; display: block;
}

.ds-card-add {
  width: 44rpx; height: 44rpx; border-radius: 50%;
  background: var(--accent-green); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 28rpx; font-weight: bold; flex-shrink: 0;
  &:active { opacity: 0.7; }
}

// 自定义项目
.ds-custom-list {
  display: flex; flex-direction: column; gap: 10rpx; margin-bottom: 20rpx;
}

.ds-custom-item {
  display: flex; align-items: center; gap: 12rpx;
  padding: 14rpx 16rpx; border-radius: 10rpx;
  background: var(--bg-secondary); border: 1rpx solid var(--border-color);
}

.ds-ci-icon { font-size: 28rpx; }
.ds-ci-info { flex: 1; }
.ds-ci-name { font-size: 26rpx; font-weight: 600; color: var(--text-primary); display: block; }
.ds-ci-meta { font-size: 20rpx; color: var(--text-tertiary); }
.ds-ci-del { font-size: 24rpx; color: #ef4444; padding: 8rpx; }

.ds-custom-form {
  background: var(--bg-secondary); border-radius: 14rpx; padding: 20rpx;
}

.ds-cf-title { font-size: 24rpx; font-weight: 600; color: var(--text-primary); margin-bottom: 12rpx; }

.ds-cf-input {
  height: 64rpx; border-radius: 10rpx; padding: 0 16rpx;
  background: var(--bg-card); font-size: 26rpx; color: var(--text-primary);
  border: 1rpx solid var(--border-color); width: 100%; box-sizing: border-box;
  margin-bottom: 12rpx;
}

.ds-cf-presets {
  display: flex; flex-wrap: wrap; gap: 8rpx; margin-bottom: 12rpx;
}

.ds-cf-preset {
  display: inline-flex; align-items: center; gap: 6rpx;
  padding: 8rpx 14rpx; border-radius: 8rpx;
  background: var(--accent-green); color: #fff; font-size: 22rpx; font-weight: 500;
}

.ds-cf-preset-del { font-size: 18rpx; padding: 2rpx; }

.ds-cf-row {
  display: flex; gap: 10rpx; margin-bottom: 12rpx;
}

.ds-cf-picker {
  flex: 1; height: 64rpx; border-radius: 10rpx; padding: 0 16rpx;
  background: var(--bg-card); border: 1rpx solid var(--border-color);
  display: flex; align-items: center; font-size: 24rpx; color: var(--text-primary);
}

.ds-cf-addstep {
  height: 64rpx; padding: 0 24rpx; border-radius: 10rpx;
  background: var(--bg-secondary); border: 1rpx solid var(--border-color);
  display: flex; align-items: center; justify-content: center;
  font-size: 24rpx; color: var(--accent-green); font-weight: 600;
}

.ds-cf-save {
  height: 72rpx; border-radius: 12rpx;
  background: linear-gradient(135deg, var(--accent-green), #16a34a);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 28rpx; font-weight: 700;
  &:active { opacity: 0.85; }
}

// ===== 快捷入口 =====
.quick-actions {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.qa-btn {
  flex: 1;
  height: 100rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  transition: all 0.3s;

  &:active { transform: scale(0.96); opacity: 0.85; }
}

.qa-primary {
  background: linear-gradient(135deg, var(--accent-green), #16a34a);
}

.qa-secondary {
  background: var(--bg-card);
  border: 2rpx solid var(--border-color);
}

.qa-btn-icon { font-size: 36rpx; }
.qa-btn-text { font-size: 30rpx; font-weight: 600; }

.qa-primary .qa-btn-text { color: #fff; }
.qa-secondary .qa-btn-text { color: var(--text-primary); }

// ===== 底部 =====
.bottom-spacer { height: 40rpx; }
</style>
