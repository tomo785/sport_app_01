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

      <!-- ===== 热门计划 ===== -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">热门计划</text>
        </view>
        <view class="hot-carousel">
          <swiper class="hot-swiper" :autoplay="false" :duration="400"
            previous-margin="160rpx" next-margin="160rpx"
            :current="hotCurrent"
            @change="onHotChange">
            <swiper-item class="hot-slide" v-for="(item, idx) in hotPlans" :key="idx" @click="goPlanDetail(item)">
              <view class="hot-slide-inner" :class="'gradient-' + (idx % 3)">
                <text class="hot-slide-name">{{ item.name }}</text>
                <text class="hot-slide-tag">{{ item.tag }}</text>
              </view>
            </swiper-item>
          </swiper>
          <view class="hot-dots">
            <view class="hot-dot" :class="{ active: hotPage + 1 === i }" v-for="i in hotPlans.length" :key="i" @click="goToHotSlide(i - 1)"></view>
          </view>
        </view>
      </view>

      <!-- ===== 创建活动 ===== -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">创建活动</text>
        </view>
        <view class="filter-layout">
          <view class="filter-side">
            <view class="filter-item" v-for="cat in filterCategories" :key="cat.key"
              :class="{ active: activeFilter === cat.key }" @click="activeFilter = cat.key">
              <text class="fi-text">{{ cat.label }}</text>
            </view>
          </view>
          <view class="filter-content">
            <view class="fc-chip" v-for="act in filteredActivityTypes" :key="act"
              :class="{ selected: selectedActivities.includes(act) }" @click="toggleActivity(act)">
              <text class="fc-text">{{ act }}</text>
              <text class="fc-check" v-if="selectedActivities.includes(act)">✓</text>
            </view>
            <view class="fc-empty" v-if="filteredActivityTypes.length === 0">
              <text>暂无匹配活动</text>
            </view>
          </view>
        </view>
        <view class="create-activity-btn" @click="goActivityCreate">
          <text class="cab-text">创建活动</text>
        </view>
        <view class="created-list" v-if="createdActivities.length > 0">
          <view class="created-item" v-for="(item, idx) in createdActivities" :key="idx">
            <view class="ci-left">
              <view class="ci-info">
                <text class="ci-name">{{ item.name }}</text>
                <text class="ci-desc">{{ item.desc }}</text>
              </view>
            </view>
            <view class="ci-right">
              <text class="ci-delete" @click="removeCreatedActivity(idx)">✕</text>
            </view>
          </view>
        </view>
        <view class="empty-hint" v-else>
          <text>选择上方活动类型，点击「创建活动」开始</text>
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
  if (plan.courses) {
    return plan.courses.filter(c => c.day === dayOfWeek)
  }
  if (plan.days) {
    const dayPlan = plan.days.find(d => d.dayOfWeek === dayOfWeek)
    if (!dayPlan) return []
    const acts = dayPlan.activities || []
    return acts.map(act => ({
      name: act.name,
      type: act.type,
      duration: act.duration || 0
    }))
  }
  return []
}

function getDayStatusText(plan, dayOfWeek) {
  const courses = getDayCourses(plan, dayOfWeek)
  if (courses.length === 0) return '无安排'
  if (courses.some(c => c.type === 6 || c.type === 'rest')) return '😴 休息'
  const names = courses.map(c => {
    if (typeof c.type === 'number') return courseTypeName(c.type)
    const map = { run: '跑步', strength: '力量', yoga: '拉伸', rest: '休息', custom: '综合' }
    return map[c.type] || '运动'
  })
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
    .then(res => {
      if (res.code === 200 && res.data) {
        const backendPlans = res.data
        // 读取本地完整数据做兜底补充
        const saved = uni.getStorageSync('myTrainingPlans')
        const localPlans = saved ? (() => { try { return JSON.parse(saved) } catch (e) { return [] } })() : []
        myPlans.value = backendPlans.map(bp => {
          const localPlan = localPlans.find(lp => String(lp.id) === String(bp.id))
          // 后端若无 courses，优先用本地 courses；若有则直接用后端的
          if ((!bp.courses || bp.courses.length === 0) && localPlan && localPlan.courses) {
            return { ...bp, courses: localPlan.courses }
          }
          return bp
        })
      }
    })
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
  const courses = getDayCourses(plan, dayOfWeek)
  return courses.some(c => c.type === 6 || c.type === 'rest')
}

function getProgressPct(plan) {
  if (!plan.totalWeeks) return 0
  if (plan.totalCourses > 0) return Math.round((plan.completedCourses || 0) / plan.totalCourses * 100)
  return Math.round((plan.currentWeek || 0) / plan.totalWeeks * 100)
}

function getWorkoutsPerWeek(plan) {
  if (plan.courses && plan.courses.length > 0) {
    const days = new Set(plan.courses.map(c => c.day))
    return days.size
  }
  if (plan.days && plan.days.length > 0) {
    return plan.days.filter(d => {
      const acts = d.activities || []
      return acts.length > 0 && !acts.every(a => a.type === 'rest')
    }).length
  }
  return 0
}

function getLevelColor(level) {
  const c = { 1: '#22c55e', 2: '#3b82f6', 3: '#f59e0b', 4: '#ef4444' }
  return c[level] || c[1]
}

// ---- 热门计划和创建活动 ----
const hotPlans = ref([
  { id: 1, name: '晨跑30分钟', emoji: '🏃', tag: '有氧 · 初级', desc: '慢跑30分钟，开启活力一天' },
  { id: 2, name: '瑜伽入门', emoji: '🧘', tag: '柔韧 · 初级', desc: '基础体式练习，改善体态' },
  { id: 3, name: '力量训练', emoji: '💪', tag: '增肌 · 中级', desc: '全身肌肉群均衡训练' },
  { id: 4, name: '户外骑行', emoji: '🚴', tag: '有氧 · 中级', desc: '城市绿道骑行，畅快体验' },
  { id: 5, name: '间歇冲刺', emoji: '⚡', tag: '燃脂 · 高级', desc: '高强度间歇，快速燃脂' },
  { id: 6, name: '全身燃脂', emoji: '🔥', tag: '减脂 · 中级', desc: 'HIIT组合，高效燃脂' },
])
const hotPage = ref(0)
const hotCurrent = ref(0)
const selectedActivities = ref([])
const activeFilter = ref('run')
const createdActivities = ref([])

const filterCategories = [
  { key: 'run',  label: '跑步',  types: ['线下跑步', '室内跑步'] },
  { key: 'bike', label: '骑行',  types: ['线上骑行', '骑行'] },
  { key: 'outdoor', label: '户外', types: ['徒步', '健步走', '登山'] },
  { key: 'swim', label: '水中',  types: ['游泳'] },
]

const filteredActivityTypes = computed(() => {
  const cat = filterCategories.find(c => c.key === activeFilter.value)
  return cat ? cat.types : []
})

function onHotChange(e) {
  hotPage.value = e.detail.current
  hotCurrent.value = e.detail.current
}

function goToHotSlide(index) {
  hotCurrent.value = index
  hotPage.value = index
}

function toggleActivity(act) {
  const idx = selectedActivities.value.indexOf(act)
  if (idx > -1) selectedActivities.value.splice(idx, 1)
  else selectedActivities.value.push(act)
}

function goPlanDetail(item) {
  if (item && item.id) uni.navigateTo({ url: `/pages/goal/detail?id=${item.id}` })
}

function goActivityCreate() {
  uni.navigateTo({ url: '/pages/goal/activity-editor' })
}

function loadCreatedActivities() {
  try {
    const raw = uni.getStorageSync('userActivities')
    if (raw) {
      const list = typeof raw === 'string' ? JSON.parse(raw) : raw
      const merged = [...createdActivities.value]
      list.forEach(item => {
        if (!merged.find(m => m.name === item.title)) {
          const steps = (item.main || []).length + (item.warmup || []).length + (item.cooldown || []).length
          merged.push({
            name: item.title, desc: `${steps}个步骤 · ${item.warmup?.length ? '热身' : ''}${item.main?.length ? '+主项' : ''}${item.cooldown?.length ? '+缓和' : ''}`, time: item.createdAt || ''
          })
        }
      })
      createdActivities.value = merged
    }
  } catch (e) { /* ignore */ }
}

function removeCreatedActivity(idx) {
  const item = createdActivities.value[idx]
  createdActivities.value.splice(idx, 1)
  try {
    const raw = uni.getStorageSync('userActivities')
    const list = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
    uni.setStorageSync('userActivities', JSON.stringify(list.filter(a => a.title !== item.name)))
  } catch (e) { /* ignore */ }
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
  loadCreatedActivities()
})

onShow(() => {
  loadWeekOverview()
  loadMyPlans()
  loadCreatedActivities()
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

// ===== 热门计划（轮播） =====
.hot-carousel { margin-bottom: 28rpx; }

.hot-swiper {
  height: 260rpx;
}

.hot-slide { display: flex; align-items: center; justify-content: center; }

.hot-slide-inner {
  width: 100%; height: 200rpx; border-radius: 24rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8rpx; transition: all 0.3s; transform: scale(0.88); opacity: 0.7;

  &.gradient-0 { background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%); }
  &.gradient-1 { background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%); }
  &.gradient-2 { background: linear-gradient(135deg, #FDF2F8 0%, #FCE7F3 100%); }
}

.uni-swiper-slide-active .hot-slide-inner {
  transform: scale(1); opacity: 1;
}

.hot-slide-name { font-size: 32rpx; font-weight: 700; color: #1c1c1e; }
.hot-slide-tag { font-size: 22rpx; color: #6b7280; }

.hot-dots { display: flex; justify-content: center; gap: 16rpx; margin-top: 12rpx; }
.hot-dot {
  width: 16rpx; height: 16rpx; border-radius: 50%; background: #D1D5DB;
  &.active { background: var(--accent-green); }
}

// ===== 创建活动 =====
.filter-layout { display: flex; gap: 16rpx; min-height: 360rpx; margin-bottom: 20rpx; }
.filter-side {
  width: 136rpx; flex-shrink: 0; background: var(--bg-secondary);
  border-radius: 16rpx; display: flex; flex-direction: column; overflow-y: auto;
  &::-webkit-scrollbar { display: none; }
}
.filter-item {
  display: flex; align-items: center; justify-content: center;
  height: 80rpx; position: relative; transition: background 0.2s;
  &:not(:last-child)::after {
    content: ''; position: absolute; bottom: 0; left: 20rpx; right: 20rpx;
    height: 1rpx; background: var(--border-color);
  }
  &.active { background: var(--bg-secondary); .fi-text { color: var(--accent-green); font-weight: 600; } }
}
.fi-text { font-size: 22rpx; color: var(--text-tertiary); font-weight: 500; }
.filter-content {
  flex: 1; background: var(--bg-secondary); border-radius: 16rpx;
  padding: 16rpx; display: flex; flex-direction: column; gap: 12rpx;
}
.fc-chip {
  display: flex; align-items: center; justify-content: space-between;
  height: 64rpx; padding: 0 14rpx; border-radius: 12rpx; background: var(--bg-card);
  transition: all 0.2s;
  &.selected { background: var(--bg-card); border: 2rpx solid var(--accent-green); .fc-text { color: var(--accent-green); font-weight: 600; } }
  &:active { background: var(--bg-secondary); }
}
.fc-text { font-size: 26rpx; color: var(--text-primary); }
.fc-check { font-size: 22rpx; color: var(--accent-green); font-weight: 700; }
.fc-empty { flex: 1; display: flex; align-items: center; justify-content: center; color: var(--text-tertiary); font-size: 24rpx; }
.create-activity-btn {
  width: 100%; height: 80rpx; border-radius: 16rpx;
  background: linear-gradient(135deg, var(--accent-green), #16a34a);
  display: flex; align-items: center; justify-content: center; margin-bottom: 20rpx;
}
.cab-text { font-size: 28rpx; color: #fff; font-weight: 600; }
.created-list { display: flex; flex-direction: column; gap: 12rpx; }
.created-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20rpx; background: var(--bg-secondary); border-radius: 16rpx;
}
.ci-left { flex: 1; min-width: 0; }
.ci-info { display: flex; flex-direction: column; gap: 6rpx; }
.ci-name { font-size: 28rpx; font-weight: 600; color: var(--text-primary); }
.ci-desc { font-size: 22rpx; color: var(--text-tertiary); }
.ci-delete { font-size: 24rpx; color: #EF4444; padding: 8rpx; }
.empty-hint { text-align: center; padding: 40rpx 0; color: var(--text-tertiary); font-size: 26rpx; }

.bottom-spacer { height: 40rpx; }
</style>
