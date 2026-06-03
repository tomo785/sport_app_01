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

      <!-- 已有计划 -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">已有计划</text>
        </view>

        <view class="plan-list" v-if="myPlans.length > 0">
          <view class="plan-swipe-item" v-for="(plan, idx) in myPlans" :key="plan.id"
            @touchstart="touchStart($event, idx)" @touchmove="touchMove($event, idx)" @touchend="touchEnd($event, idx)">
            <view class="plan-actions">
              <view class="plan-action-btn modify" @click.stop="editPlan(plan)">修改</view>
              <view class="plan-action-btn delete" @click.stop="confirmDeletePlan(plan)">删除</view>
            </view>
            <view class="plan-row" :class="{ shifted: swipeOpenIndex === idx }" @click="onRowClick(plan)">
              <view class="plan-bar" :style="{ background: getLevelColor(plan.level) }"></view>
              <view class="plan-info">
                <text class="plan-name">{{ plan.name }}</text>
                <view class="plan-tags">
                  <text class="plan-tag" :style="{ background: getLevelColor(plan.level) + '1A', color: getLevelColor(plan.level) }">{{ plan.levelName || '新手' }}</text>
                  <text class="plan-tag tag-gray">{{ plan.totalWeeks || 4 }}周</text>
                  <text class="plan-tag tag-gray" v-if="plan.courses && plan.courses.length">每周{{ getWorkoutsPerWeek(plan) }}练</text>
                </view>
                <text class="plan-desc" v-if="plan.description">{{ plan.description }}</text>
                <text class="plan-desc" v-else>第{{ plan.currentWeek || 1 }}/{{ plan.totalWeeks || 4 }}周 · 已完成 {{ getProgressPct(plan) }}%</text>
              </view>
              <view class="plan-right">
                <view class="ring-wrap">
                  <svg class="ring-svg" viewBox="0 0 100 100">
                    <circle cx="50" cy="50" r="40" fill="none" stroke="#f1f5f9" stroke-width="10"/>
                    <circle cx="50" cy="50" r="40" fill="none" stroke="#22c55e" stroke-width="10"
                      stroke-linecap="round"
                      stroke-dasharray="251.3"
                      :stroke-dashoffset="251.3 - (getProgressPct(plan) / 100) * 251.3"
                      transform="rotate(-90 50 50)"/>
                  </svg>
                  <text class="ring-text">{{ getProgressPct(plan) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="plan-empty" v-else>
          <text class="plan-empty-text">暂无计划，从热门推荐开始吧</text>
        </view>
      </view>

      <!-- 操作按钮区 -->
      <view class="action-buttons">
        <view class="action-btn action-btn-primary" @click="goCreatePlan">
          <text class="action-btn-text action-btn-text-primary">+ 创建计划</text>
        </view>
        <view class="action-btn action-btn-secondary" @click="goCommunity">
          <text class="action-btn-text action-btn-text-secondary">社区</text>
        </view>
      </view>

      <!-- 热门计划 -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">热门计划</text>
          <!-- 查看更多已移除，由下方社区按钮统一入口 -->
        </view>
        <scroll-view class="hot-scroll" scroll-x :show-scrollbar="false" @scroll="onHotScroll">
          <view class="hot-track">
            <view class="hot-card" v-for="(item, idx) in hotPlans" :key="idx" @click="goPlanDetail(item)">
              <view class="hot-img" :class="'gradient-' + (idx % 3)">
                <view class="hot-mask"></view>
                <text class="hot-name">{{ item.name }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="hot-dots">
          <view class="hot-dot" :class="{ active: hotPage === i }" v-for="i in 3" :key="i"></view>
        </view>
      </view>

      <!-- 创建活动 -->
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
import { ref, reactive, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getMyPlans,
  deletePlan, uploadPlanToCommunity, getPlanDetail,
  updatePlan
} from '@/api/plan'

const statusBarHeight = ref(0)
const refreshing = ref(false)
const hotPage = ref(0)
const avatarUrl = ref('')
const myPlans = ref([])
const selectedPlan = ref(null)
const activityMode = ref('add')

const dayLabels = ['一', '二', '三', '四', '五', '六', '日']
const activityCats = ['有氧', '力量', '拉伸', 'HIIT', '综合']

const customTypeIdx = ref(2)
const customWeekIdx = ref(0)
const customDayIdx = ref(0)
const customCourse = reactive({ name: '', type: 3, duration: 30 })

const selectedActivities = ref([])
const activeFilter = ref('run')

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

const createdActivities = ref([])

const activityEmojiMap = {
  '线下跑步': { desc: '户外跑步训练' },
  '线上骑行': { desc: '室内骑行台训练' },
  '徒步': { desc: '户外徒步活动' },
  '室内跑步': { desc: '跑步机训练' },
  '骑行': { desc: '户外骑行活动' },
  '健步走': { desc: '日常健步走' },
  '游泳': { desc: '游泳训练' },
  '登山': { desc: '登山活动' },
  '瑜伽': { desc: '瑜伽练习' },
  '力量训练': { desc: '力量训练课程' },
  '有氧操': { desc: '有氧操课程' },
  '拉伸': { desc: '拉伸放松' },
}

const activityTypes = [
  '线下跑步', '线上骑行', '徒步',
  '室内跑步', '骑行', '健步走',
  '游泳', '登山', '瑜伽',
  '力量训练', '有氧操', '拉伸'
]

const hotPlans = ref([
  { id: 1, name: '晨跑30分钟', tag: '晨跑' },
  { id: 2, name: '瑜伽入门', tag: '瑜伽' },
  { id: 3, name: '力量训练', tag: '力量' },
  { id: 4, name: '户外骑行', tag: '骑行' },
  { id: 5, name: '间歇冲刺', tag: '跑步' },
  { id: 6, name: '全身燃脂', tag: '减脂' },
])

const customWeeks = computed(() => {
  const w = selectedPlan.value ? selectedPlan.value.totalWeeks : 4
  return Array.from({ length: w }, (_, i) => i + 1)
})

const quickActivities = [
  { type: 1, name: '热身跑', duration: 10, warmUpDuration: 10, exercises: [{ name: '慢跑热身', type: 1, duration: 600 }] },
  { type: 2, name: '间歇跑', duration: 45, exercises: [{ name: '热身', type: 1, duration: 600 }, { name: '冲刺×8', type: 1, duration: 90, sets: 8, reps: 1, restTime: 90 }, { name: '缓和', type: 3, duration: 600 }] },
  { type: 2, name: '长距离慢跑', duration: 60, exercises: [{ name: '热身', type: 1, duration: 300 }, { name: '慢跑10km', type: 1, duration: 3000, distance: 10000 }, { name: '缓和', type: 3, duration: 300 }] },
  { type: 2, name: '节奏跑', duration: 50, exercises: [{ name: '热身2km', type: 1, duration: 600, distance: 2000 }, { name: '节奏5km', type: 1, duration: 1800, distance: 5000 }, { name: '冷身', type: 3, duration: 600 }] },
  { type: 2, name: '全身力量', duration: 45, exercises: [{ name: '深蹲', type: 2, sets: 4, reps: 12, restTime: 60 }, { name: '俯卧撑', type: 2, sets: 4, reps: 15, restTime: 60 }] },
  { type: 3, name: '瑜伽拉伸', duration: 30, exercises: [{ name: '全身拉伸', type: 3, duration: 1800 }] },
  { type: 1, name: '骑行', duration: 60 },
  { type: 1, name: '游泳', duration: 45 },
]

// ---- 初始化 ----
onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
  loadAvatar()
  loadMyPlans()
})

onShow(() => {
  loadAvatar()
  loadMyPlans()
  loadCreatedActivities()
  if (selectedPlan.value && selectedPlan.value.id) {
    refreshSelectedPlan()
  }
})

function loadAvatar() {
  const saved = uni.getStorageSync('avatarUrl')
  if (saved) avatarUrl.value = saved
}

function onRefresh() {
  refreshing.value = true
  loadMyPlans()
}

// ---- 导航 ----
function goProfile() {
  uni.switchTab({ url: '/pages/user/profile' })
}

function goMessages() {
  uni.showToast({ title: '消息功能开发中', icon: 'none' })
}

function goCreatePlan() {
  uni.navigateTo({ url: '/pages/goal/create' })
}

function goDiscover() {
  uni.navigateTo({ url: '/pages/goal/community' })
}

function goCommunity() {
  uni.navigateTo({ url: '/pages/goal/community' })
}

function goActivityCreate() {
  uni.navigateTo({ url: '/pages/goal/activity-editor' })
}

function goPlanDetail(item) {
  if (item && item.id) {
    uni.navigateTo({ url: `/pages/goal/detail?id=${item.id}` })
  }
}

const swipeOpenIndex = ref(-1)
const touchData = ref({})

function touchStart(e, idx) {
  touchData.value = {
    startX: e.touches[0].clientX,
    startY: e.touches[0].clientY,
    idx
  }
}

function touchMove(e, idx) {
  const data = touchData.value
  if (!data || data.idx !== idx) return
  data.currentX = e.touches[0].clientX
  data.currentY = e.touches[0].clientY
}

function touchEnd(e, idx) {
  const data = touchData.value
  if (!data || data.idx !== idx) return
  const deltaX = data.startX - (data.currentX || data.startX)
  const deltaY = Math.abs((data.currentY || data.startY) - data.startY)

  if (Math.abs(deltaX) < 10 && deltaY < 10) {
    if (swipeOpenIndex.value === idx) {
      swipeOpenIndex.value = -1
    }
    return
  }

  if (deltaX > 50 && deltaX > deltaY) {
    swipeOpenIndex.value = idx
  } else if (deltaX < -30) {
    swipeOpenIndex.value = -1
  }
}

function onRowClick(plan) {
  if (swipeOpenIndex.value !== -1) {
    swipeOpenIndex.value = -1
  } else {
    goPlanDetail(plan)
  }
}

function editPlan(plan) {
  swipeOpenIndex.value = -1
  uni.navigateTo({ url: `/pages/goal/create?id=${plan.id}` })
}

function goToTaskDetail(plan) {
  uni.navigateTo({ url: '/pages/task/task-detail' })
}

// ---- 热门计划 ----
function onHotScroll(e) {
  const width = e.detail.scrollWidth
  const left = e.detail.scrollLeft
  const itemWidth = width / hotPlans.value.length
  if (itemWidth > 0) {
    hotPage.value = Math.min(2, Math.round(left / itemWidth / 2))
  }
}

function toggleActivity(act) {
  const idx = selectedActivities.value.indexOf(act)
  if (idx > -1) {
    selectedActivities.value.splice(idx, 1)
  } else {
    selectedActivities.value.push(act)
  }
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
            name: item.title,
            desc: `${steps}个步骤 · ${item.warmup?.length ? '热身' : ''}${item.main?.length ? '+主项' : ''}${item.cooldown?.length ? '+缓和' : ''}`,
            time: item.createdAt || ''
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
  // 同步删除 localStorage 中的活动
  try {
    const raw = uni.getStorageSync('userActivities')
    const list = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
    const filtered = list.filter(a => a.title !== item.name)
    uni.setStorageSync('userActivities', JSON.stringify(filtered))
  } catch (e) { /* ignore */ }
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
}

function confirmDeletePlan(plan) {
  uni.showModal({
    title: '删除计划',
    content: `确定删除"${plan.name}"？不可恢复。`,
    success: res => {
      if (!res.confirm) return
      deletePlan(plan.id)
        .then(() => {
          myPlans.value = myPlans.value.filter(p => p.id !== plan.id)
          if (selectedPlan.value && selectedPlan.value.id === plan.id) selectedPlan.value = null
        })
        .catch(() => {
          myPlans.value = myPlans.value.filter(p => p.id !== plan.id)
          if (selectedPlan.value && selectedPlan.value.id === plan.id) selectedPlan.value = null
        })
    }
  })
}

function uploadPlan(plan) {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showModal({
      title: '需要登录',
      content: '上传计划到社区需要先登录账号',
      confirmText: '去登录',
      success: res => {
        if (res.confirm) uni.navigateTo({ url: '/pages/user/login' })
      }
    })
    return
  }
  uni.showModal({
    title: '上传到社区',
    content: '其他用户可以看到和使用你的计划，确定上传？',
    success: res => {
      if (!res.confirm) return
      uploadPlanToCommunity(plan.id)
        .then(() => { uni.showToast({ title: '已上传', icon: 'success' }); plan.isOfficial = true })
        .catch(() => uni.showToast({ title: '上传失败', icon: 'none' }))
    }
  })
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

// ---- 活动管理 ----
const selectedPlanCourses = computed(() => {
  if (!selectedPlan.value || !selectedPlan.value._courses) return []
  return selectedPlan.value._courses.sort((a, b) => {
    if (a.week !== b.week) return a.week - b.week
    if (a.day !== b.day) return a.day - b.day
    return 0
  })
})

function selectPlanForActivity(plan) {
  if (selectedPlan.value && selectedPlan.value.id === plan.id) {
    refreshSelectedPlan()
    return
  }
  getPlanDetail(plan.id)
    .then(res => {
      if (res.code === 200 && res.data) {
        selectedPlan.value = { ...plan, _courses: res.data.courses || [] }
      }
    })
    .catch(() => {
      selectedPlan.value = { ...plan, _courses: plan.courses || [] }
    })
}

function refreshSelectedPlan() {
  if (!selectedPlan.value) return
  getPlanDetail(selectedPlan.value.id)
    .then(res => {
      if (res.code === 200 && res.data) {
        selectedPlan.value._courses = res.data.courses || []
      }
    })
    .catch(() => {})
}

function quickAddToPlan(act) {
  if (!selectedPlan.value) {
    uni.showToast({ title: '请先选择一个计划', icon: 'none' })
    return
  }
  const course = {
    week: customWeeks.value[customWeekIdx.value],
    day: customDayIdx.value + 1,
    name: act.name,
    type: act.type,
    duration: act.duration,
    warmUpDuration: act.warmUpDuration || 0,
    coolDownDuration: 0,
    exercises: (act.exercises || []).map(e => ({ ...e }))
  }
  if (!selectedPlan.value._courses) selectedPlan.value._courses = []
  selectedPlan.value._courses.push(course)
  uni.showToast({ title: '已添加', icon: 'success' })
  savePlanCourses()
}

function addCustomToPlan() {
  if (!selectedPlan.value) {
    uni.showToast({ title: '请先选择一个计划', icon: 'none' })
    return
  }
  if (!customCourse.name.trim()) {
    uni.showToast({ title: '请输入活动名称', icon: 'none' })
    return
  }
  const course = {
    week: customWeeks.value[customWeekIdx.value],
    day: customDayIdx.value + 1,
    name: customCourse.name.trim(),
    type: customTypeIdx.value + 1,
    duration: customCourse.duration,
    warmUpDuration: 0,
    coolDownDuration: 0,
    exercises: [{ name: customCourse.name.trim(), type: customTypeIdx.value + 1, duration: customCourse.duration * 60 }]
  }
  if (!selectedPlan.value._courses) selectedPlan.value._courses = []
  selectedPlan.value._courses.push(course)
  customCourse.name = ''
  uni.showToast({ title: '已添加', icon: 'success' })
  savePlanCourses()
}

function removeCourseFromPlan(idx) {
  uni.showModal({
    title: '删除活动',
    content: '确定从计划中删除此活动？',
    success: res => {
      if (!res.confirm) return
      selectedPlan.value._courses.splice(idx, 1)
      savePlanCourses()
      uni.showToast({ title: '已删除', icon: 'success' })
    }
  })
}

function editExerciseInCourse(ci, ei) {
  const course = selectedPlan.value._courses[ci]
  uni.showModal({
    title: `修改步骤: ${course.exercises[ei].name}`,
    content: '编辑功能在完整编辑器中可用',
    showCancel: false
  })
}

function savePlanCourses() {
  if (!selectedPlan.value || !selectedPlan.value.id) return
  const data = {
    name: selectedPlan.value.name,
    description: selectedPlan.value.description,
    totalWeeks: selectedPlan.value.totalWeeks,
    level: selectedPlan.value.level,
    courses: (selectedPlan.value._courses || []).map(c => ({
      name: c.name,
      week: c.week,
      day: c.day,
      type: c.type,
      duration: c.duration,
      warmUpDuration: c.warmUpDuration,
      coolDownDuration: c.coolDownDuration,
      exercises: (c.exercises || []).map(e => ({
        name: e.name,
        type: e.type,
        duration: e.duration,
        sets: e.sets,
        reps: e.reps,
        restTime: e.restTime,
        distance: e.distance
      }))
    }))
  }
  updatePlan(selectedPlan.value.id, data)
    .then(() => {})
    .catch(() => {
      const local = uni.getStorageSync('myTrainingPlans')
      let plans = []
      try { plans = JSON.parse(local || '[]') } catch (e) {}
      const idx = plans.findIndex(p => p.id === selectedPlan.value.id)
      if (idx > -1) plans[idx] = { ...plans[idx], courses: data.courses }
      uni.setStorageSync('myTrainingPlans', JSON.stringify(plans))
    })
}

function onCustomTypeChange(e) { customTypeIdx.value = e.detail.value }
function onCustomWeekChange(e) { customWeekIdx.value = e.detail.value }
function onCustomDayChange(e) { customDayIdx.value = e.detail.value }

// ---- 通用工具 ----
function getCoverBg(level) {
  const c = { 1: 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)', 2: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)', 3: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', 4: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' }
  return c[level] || c[1]
}
function getLevelColor(level) {
  const c = { 1: '#22c55e', 2: '#2563eb', 3: '#f59e0b', 4: '#ef4444' }
  return c[level] || c[1]
}
function getLevelEmoji(level) { return '' }
function getTypeEmoji(type) { return '' }
</script>


<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
}

// ========== 顶部抬头 ==========
.page-header {
  background: #fff;
  padding: 0 28rpx;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
}

.header-spacer {
  width: 64rpx;
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #000000;
}

.header-sub {
  font-size: 28rpx;
  color: #9CA3AF;
  margin-top: 4rpx;
}

.header-add {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-icon {
  font-size: 48rpx;
  color: #2563EB;
  font-weight: 300;
  line-height: 1;
}

// ========== 页面滚动区 ==========
.page-body {
  flex: 1;
  padding: 0 28rpx;
}

// ========== 通用 section ==========
.section {
  margin-bottom: 32rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #000000;
}

.section-more {
  font-size: 28rpx;
  color: #2563EB;
}

// ========== 热门计划 ==========
.hot-scroll {
  white-space: nowrap;
}

.hot-track {
  display: flex;
  gap: 24rpx;
}

.hot-card {
  flex-shrink: 0;
  width: 240rpx;
  height: 320rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.hot-img {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 20rpx;

  &.gradient-0 {
    background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
  }
  &.gradient-1 {
    background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  }
  &.gradient-2 {
    background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%);
  }
}

.hot-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80rpx;
  background: linear-gradient(to top, rgba(255,255,255,0.9) 0%, rgba(255,255,255,0) 100%);
}

.hot-name {
  position: relative;
  z-index: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: #374151;
}

.hot-dots {
  display: flex;
  justify-content: center;
  gap: 16rpx;
  margin-top: 20rpx;
}

.hot-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #D1D5DB;

  &.active {
    background: #000000;
  }
}

// ========== 操作按钮区 ==========
.action-buttons {
  display: flex;
  gap: 2%;
  margin-top: 24rpx;
  margin-bottom: 32rpx;
}

.action-btn {
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn-primary {
  width: 65%;
  background: #3366FF;

  &:active {
    background: #2952CC;
  }
}

.action-btn-secondary {
  width: 33%;
  background: #EFF6FF;

  &:active {
    background: #DBEAFE;
  }
}

.action-btn-text-secondary {
  color: #3366FF;
}

.action-btn-text {
  font-size: 30rpx;
  font-weight: 500;
  white-space: nowrap;
}

.action-btn-text-primary {
  color: #FFFFFF;
}

.action-btn-text-secondary {
  color: #374151;
}

// ========== 已有计划 ==========
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.plan-swipe-item {
  position: relative;
  overflow: hidden;
  border-radius: 24rpx;
  background: #fff;
  border: 2rpx solid #E5E7EB;
}
.plan-row {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  gap: 20rpx;
  transition: transform 0.25s ease;
  position: relative;
  z-index: 2;

  &:active {
    opacity: 0.8;
  }

  &.shifted {
    transform: translateX(-240rpx);
  }
}
.plan-actions {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}
.plan-action-btn {
  width: 50%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
  background: #3b82f6;
}
.plan-action-btn.modify {
  background: #3b82f6;
}
.plan-action-btn.delete {
  background: #ef4444;
  border-radius: 0 24rpx 24rpx 0;
}

.plan-bar {
  width: 8rpx;
  height: 64rpx;
  border-radius: 4rpx;
  flex-shrink: 0;
}

.plan-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.plan-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #000000;
}

.plan-desc {
  font-size: 24rpx;
  color: #9CA3AF;
}

.plan-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.plan-pct {
  font-size: 32rpx;
  font-weight: 700;
  color: #10B981;
}

.plan-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 4rpx;
  margin-bottom: 4rpx;
}

.plan-tag {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.tag-gray {
  background: #f3f4f6;
  color: #6b7280;
}

.ring-wrap {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-svg {
  width: 80rpx;
  height: 80rpx;
  position: absolute;
  top: 0;
  left: 0;
}

.ring-text {
  font-size: 22rpx;
  font-weight: 700;
  color: #22c55e;
  position: relative;
  z-index: 1;
}

.plan-empty {
  padding: 60rpx 0;
  text-align: center;
}

.plan-empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

// ========== 创建活动 ==========
.filter-layout {
  display: flex;
  gap: 16rpx;
  min-height: 360rpx;
  margin-bottom: 20rpx;
}

.filter-side {
  width: 136rpx;
  flex-shrink: 0;
  background: #F5F5F7;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  &::-webkit-scrollbar { display: none; }
}

.filter-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80rpx;
  position: relative;
  transition: background 0.2s;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 20rpx;
    right: 20rpx;
    height: 1rpx;
    background: #E5E7EB;
  }

  &.active {
    background: #DBEAFE;
    .fi-text { color: #2563EB; font-weight: 600; }
  }
}

.fi-text {
  font-size: 22rpx;
  color: #6B7280;
  font-weight: 500;
}

.filter-content {
  flex: 1;
  background: #F5F5F7;
  border-radius: 16rpx;
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.fc-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64rpx;
  padding: 0 14rpx;
  border-radius: 12rpx;
  background: #fff;
  transition: all 0.2s;

  &.selected {
    background: #DBEAFE;
    border: 2rpx solid #2563EB;

    .fc-text {
      color: #2563EB;
      font-weight: 600;
    }
  }

  &:active {
    background: #EFF6FF;
  }
}

.fc-text {
  font-size: 26rpx;
  color: #374151;
}

.fc-check {
  font-size: 22rpx;
  color: #2563EB;
  font-weight: 700;
}

.fc-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  font-size: 24rpx;
}

.create-activity-btn {
  width: 100%;
  height: 80rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.cab-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
}

.created-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.created-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #F5F5F7;
  border-radius: 16rpx;
}

.ci-left {
  flex: 1;
  min-width: 0;
}

.ci-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.ci-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #000000;
}

.ci-desc {
  font-size: 22rpx;
  color: #9CA3AF;
}

.ci-delete {
  font-size: 24rpx;
  color: #EF4444;
  padding: 8rpx;
}

.empty-hint {
  text-align: center;
  padding: 40rpx 0;
  color: #9CA3AF;
  font-size: 26rpx;
}

// ========== 底部 ==========
.bottom-spacer {
  height: 60rpx;
}
</style>
