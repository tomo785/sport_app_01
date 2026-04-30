<template>
  <view class="container">
    <scroll-view class="page-scroll" scroll-y :refresher-enabled="true"
      :refresher-triggered="refreshing" @refresherrefresh="onRefresh">

      <!-- ========== 顶部操作按钮 ========== -->
      <view class="top-actions">
        <view class="ta-btn create-btn" @click="goCreatePlan">
          <text class="ta-icon">+</text>
          <text class="ta-label">添加计划</text>
        </view>
        <view class="ta-btn discover-btn" @click="showDiscover = !showDiscover">
          <text class="ta-icon">🔍</text>
          <text class="ta-label">发现计划</text>
        </view>
      </view>

      <!-- ========== 发现计划区域（可折叠） ========== -->
      <view class="discover-section" v-if="showDiscover">
        <!-- 引导问卷 -->
        <view class="questionnaire">
          <view class="q-header" @click="qExpanded = !qExpanded">
            <text class="q-title">智能匹配</text>
            <text class="q-toggle">{{ qExpanded ? '收起 ▲' : '展开问卷 ▼' }}</text>
          </view>
          <view class="q-body" v-if="qExpanded">
            <view class="q-item">
              <text class="q-label">1. 训练类型</text>
              <view class="q-options">
                <view class="q-opt" :class="{ selected: questionnaire.trainingType === 1 }"
                  @click="questionnaire.trainingType = 1">跑步</view>
                <view class="q-opt" :class="{ selected: questionnaire.trainingType === 2 }"
                  @click="questionnaire.trainingType = 2">力量</view>
                <view class="q-opt" :class="{ selected: questionnaire.trainingType === 3 }"
                  @click="questionnaire.trainingType = 3">有氧</view>
              </view>
            </view>
            <view class="q-item">
              <text class="q-label">2. 经验水平</text>
              <view class="q-options">
                <view class="q-opt" :class="{ selected: questionnaire.level === 1 }"
                  @click="questionnaire.level = 1">新手</view>
                <view class="q-opt" :class="{ selected: questionnaire.level === 2 }"
                  @click="questionnaire.level = 2">进阶</view>
                <view class="q-opt" :class="{ selected: questionnaire.level === 3 }"
                  @click="questionnaire.level = 3">高手</view>
              </view>
            </view>
            <view class="q-item">
              <text class="q-label">3. 计划周数</text>
              <view class="q-options">
                <view class="q-opt" v-for="w in [2,4,6,8,12]" :key="w"
                  :class="{ selected: questionnaire.totalWeeks === w }"
                  @click="questionnaire.totalWeeks = w">{{ w }}周</view>
              </view>
            </view>
            <view class="q-submit" @click="doMatchPlans">
              <text class="q-submit-text">匹配计划</text>
            </view>
          </view>
        </view>

        <!-- 匹配结果 -->
        <view class="discover-results" v-if="discoverPlans.length > 0">
          <view class="section-label">推荐 {{ discoverPlans.length }} 个计划</view>
          <view class="discover-card" v-for="plan in discoverPlans" :key="'d'+plan.id">
            <view class="dc-top">
              <view class="dc-cover" :style="{ background: getCoverBg(plan.level) }">
                <text class="cover-emoji">{{ getLevelEmoji(plan.level) }}</text>
              </view>
              <view class="dc-info">
                <text class="dc-name">{{ plan.name }}</text>
                <text class="dc-desc">{{ plan.description || '暂无简介' }}</text>
                <view class="dc-stats">
                  <text class="dc-stat">⭐{{ (plan.rating||0).toFixed(1) }}</text>
                  <text class="dc-stat">{{ plan.enrollCount||0 }}人用</text>
                </view>
              </view>
            </view>
            <view class="dc-actions">
              <view class="dc-btn detail-btn" @click="goPlanDetail(plan.id)">详情</view>
              <view class="dc-btn adopt-btn" @click="doAdoptPlan(plan)"
                :class="{ adopted: plan.userStatus === 1 }">
                {{ plan.userStatus === 1 ? '已采用' : '采用' }}
              </view>
            </view>
          </view>
        </view>
        <view class="empty-hint" v-else-if="qSearched && discoverPlans.length === 0">
          <text>暂无匹配结果，试试调整条件</text>
        </view>
      </view>

      <!-- ========== 已有计划列表 ========== -->
      <view class="plans-area">
        <view class="section-label">已有计划</view>

        <view class="plan-list" v-if="myPlans.length > 0">
          <view class="plan-card" v-for="plan in myPlans" :key="plan.id"
            @click="editPlan(plan)">
            <view class="card-top">
              <view class="card-cover" :style="{ background: getCoverBg(plan.level) }">
                <text class="cover-emoji">{{ getLevelEmoji(plan.level) }}</text>
              </view>
              <view class="card-info">
                <text class="card-name">{{ plan.name }}</text>
                <text class="card-desc" v-if="plan.description">{{ plan.description }}</text>
                <view class="card-tags">
                  <text class="tag level-tag">{{ plan.levelName || '新手' }}</text>
                  <text class="tag week-tag">{{ plan.totalWeeks || 4 }}周</text>
                  <text class="tag status-tag" :class="'s'+plan.userStatus">{{ plan.userStatusName || '进行中' }}</text>
                </view>
              </view>
              <text class="card-arrow">›</text>
            </view>
            <view class="card-progress" v-if="plan.totalCourses > 0">
              <view class="progress-head">
                <text class="progress-label">第{{ plan.currentWeek||1 }}/{{ plan.totalWeeks||4 }}周</text>
                <text class="progress-pct">{{ getProgressPct(plan) }}%</text>
              </view>
              <view class="progress-track">
                <view class="progress-fill" :style="{ width: getProgressPct(plan) + '%' }"></view>
              </view>
            </view>
            <view class="card-actions" @click.stop>
              <view class="action-btn" @click="editPlan(plan)">
                <text class="action-text">修改计划</text>
              </view>
              <view class="action-btn" @click="selectPlanForActivity(plan)">
                <text class="action-text">管理活动</text>
              </view>
              <view class="action-btn upload-btn" v-if="!plan.isOfficial" @click="uploadPlan(plan)">
                <text class="action-text">上传</text>
              </view>
              <view class="action-btn del-btn" @click="confirmDeletePlan(plan)">
                <text class="action-text">删除</text>
              </view>
            </view>
          </view>
        </view>

        <view class="empty-state" v-else>
          <text class="empty-icon">📋</text>
          <text class="empty-title">还没有训练计划</text>
          <text class="empty-sub">点击上方「添加计划」创建你的专属训练计划</text>
        </view>
      </view>

      <!-- ========== 活动管理区域 ========== -->
      <view class="activity-area" v-if="selectedPlan">
        <view class="section-label">
          {{ selectedPlan.name }} · 活动管理
        </view>

        <!-- 添加/修改活动选项卡 -->
        <view class="activity-tabs">
          <view class="at-tab" :class="{ active: activityMode === 'add' }"
            @click="activityMode = 'add'">
            <text>添加活动</text>
          </view>
          <view class="at-tab" :class="{ active: activityMode === 'edit' }"
            @click="activityMode = 'edit'">
            <text>修改活动</text>
          </view>
        </view>

        <!-- 修改活动：展示已有活动列表 -->
        <view class="at-body" v-if="activityMode === 'edit'">
          <view v-if="selectedPlanCourses.length > 0">
            <view class="course-editor-card" v-for="(course, ci) in selectedPlanCourses" :key="ci">
              <view class="ce-head">
                <text class="ce-emoji">{{ getTypeEmoji(course.type) }}</text>
                <view class="ce-info">
                  <text class="ce-name">第{{ course.week }}周 · 周{{ dayLabels[course.day-1] }} · {{ course.name }}</text>
                  <text class="ce-meta">{{ course.duration || 0 }}分钟</text>
                </view>
                <text class="ce-del" @click="removeCourseFromPlan(ci)">✕</text>
              </view>
              <!-- 步骤 -->
              <view class="ce-steps" v-if="course.exercises && course.exercises.length > 0">
                <view class="ce-step" v-for="(ex, ei) in course.exercises" :key="ei">
                  <text class="cs-dot">·</text>
                  <text class="cs-name">{{ ex.name }}</text>
                  <text class="cs-meta">
                    <text v-if="ex.duration">{{ ex.duration }}s</text>
                    <text v-if="ex.sets && ex.reps">{{ ex.sets }}组×{{ ex.reps }}</text>
                    <text v-if="ex.distance">{{ (ex.distance/1000).toFixed(1) }}km</text>
                  </text>
                  <text class="cs-edit" @click="editExerciseInCourse(ci, ei)">✎</text>
                </view>
              </view>
            </view>
          </view>
          <view class="empty-hint" v-else>
            <text>该计划暂无活动，请切换到「添加活动」</text>
          </view>
        </view>

        <!-- 添加活动：表单 -->
        <view class="at-body" v-if="activityMode === 'add'">
          <view class="add-course-form">
            <!-- 快速添加 -->
            <text class="acf-label">快速添加</text>
            <view class="quick-add">
              <view class="qa-chip" v-for="act in quickActivities" :key="act.name"
                @click="quickAddToPlan(act)">
                <text class="qa-emoji">{{ act.emoji }}</text>
                <text class="qa-name">{{ act.name }}</text>
              </view>
            </view>

            <!-- 自定义添加 -->
            <text class="acf-label" style="margin-top: 24rpx;">自定义活动</text>
            <view class="custom-add">
              <input class="ca-input" v-model="customCourse.name" placeholder="活动名称" />
              <view class="ca-row">
                <picker class="ca-picker" :range="activityTypes" :value="customTypeIdx"
                  @change="onCustomTypeChange">
                  <text>{{ activityTypes[customTypeIdx] }}</text>
                </picker>
                <input class="ca-input sm" v-model.number="customCourse.duration" placeholder="时长(分)" type="number" />
              </view>
              <view class="ca-row">
                <picker class="ca-picker" :range="customWeeks" :value="customWeekIdx"
                  @change="onCustomWeekChange">
                  <text>第{{ customWeeks[customWeekIdx] }}周</text>
                </picker>
                <picker class="ca-picker" :range="dayLabels" :value="customDayIdx"
                  @change="onCustomDayChange">
                  <text>周{{ dayLabels[customDayIdx] }}</text>
                </picker>
              </view>
              <view class="ca-btn" @click="addCustomToPlan">添加到计划</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 未选择计划时的活动区提示 -->
      <view class="activity-area" v-else-if="myPlans.length > 0">
        <view class="section-label">活动管理</view>
        <view class="at-pick-hint">
          <text>👆 点击上方计划卡片的「管理活动」来选择要编辑的计划</text>
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
  getMyPlans, getCommunityPlans, matchPlans,
  deletePlan, uploadPlanToCommunity, adoptPlan, getPlanDetail,
  updatePlan
} from '@/api/plan'

const refreshing = ref(false)
const showDiscover = ref(false)
const qExpanded = ref(true)
const qSearched = ref(false)
const discoverLoading = ref(false)

const myPlans = ref([])
const discoverPlans = ref([])
const selectedPlan = ref(null)
const activityMode = ref('add')

const dayLabels = ['一', '二', '三', '四', '五', '六', '日']
const activityTypes = ['有氧', '力量', '拉伸', 'HIIT', '综合']

const customTypeIdx = ref(2)
const customWeekIdx = ref(0)
const customDayIdx = ref(0)
const customCourse = reactive({ name: '', type: 3, duration: 30 })

const customWeeks = computed(() => {
  const w = selectedPlan.value ? selectedPlan.value.totalWeeks : 4
  return Array.from({ length: w }, (_, i) => i + 1)
})

const questionnaire = reactive({
  trainingType: null,
  level: null,
  totalWeeks: null
})

const quickActivities = [
  { type: 1, name: '热身跑', emoji: '🏃', duration: 10, warmUpDuration: 10, exercises: [{ name: '慢跑热身', type: 1, duration: 600 }] },
  { type: 2, name: '间歇跑', emoji: '⚡', duration: 45, exercises: [{ name: '热身', type: 1, duration: 600 }, { name: '冲刺×8', type: 1, duration: 90, sets: 8, reps: 1, restTime: 90 }, { name: '缓和', type: 3, duration: 600 }] },
  { type: 2, name: '长距离慢跑', emoji: '🏃', duration: 60, exercises: [{ name: '热身', type: 1, duration: 300 }, { name: '慢跑10km', type: 1, duration: 3000, distance: 10000 }, { name: '缓和', type: 3, duration: 300 }] },
  { type: 2, name: '节奏跑', emoji: '🏃', duration: 50, exercises: [{ name: '热身2km', type: 1, duration: 600, distance: 2000 }, { name: '节奏5km', type: 1, duration: 1800, distance: 5000 }, { name: '冷身', type: 3, duration: 600 }] },
  { type: 2, name: '全身力量', emoji: '💪', duration: 45, exercises: [{ name: '深蹲', type: 2, sets: 4, reps: 12, restTime: 60 }, { name: '俯卧撑', type: 2, sets: 4, reps: 15, restTime: 60 }] },
  { type: 3, name: '瑜伽拉伸', emoji: '🧘', duration: 30, exercises: [{ name: '全身拉伸', type: 3, duration: 1800 }] },
  { type: 1, name: '骑行', emoji: '🚴', duration: 60 },
  { type: 1, name: '游泳', emoji: '🏊', duration: 45 },
]

// ---- 计划数据 ----

onMounted(() => loadMyPlans())

onShow(() => {
  loadMyPlans()
  // 如果选择了计划，重新加载其最新数据
  if (selectedPlan.value && selectedPlan.value.id) {
    refreshSelectedPlan()
  }
})

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

function onRefresh() {
  refreshing.value = true
  loadMyPlans()
}

function goCreatePlan() {
  uni.navigateTo({ url: '/pages/goal/create' })
}

function goPlanDetail(id) {
  uni.navigateTo({ url: `/pages/goal/detail?id=${id}` })
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
  // 检查是否已登录（游客模式需要先登录）
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

// ---- 发现计划 ----

function doMatchPlans() {
  qSearched.value = true
  discoverLoading.value = true
  const params = {
    trainingType: questionnaire.trainingType,
    level: questionnaire.level,
    totalWeeks: questionnaire.totalWeeks
  }
  matchPlans(params)
    .then(res => { if (res.code === 200 && res.data) discoverPlans.value = res.data })
    .catch(() => {
      const tpl = uni.getStorageSync('communityTemplates')
      if (tpl) {
        try { discoverPlans.value = JSON.parse(tpl).map(t => ({ ...t, level: 1, levelName: '通用', rating: t.rating || 5, enrollCount: t.usageCount || 0, userStatus: 0 })) }
        catch (e) { discoverPlans.value = [] }
      }
    })
    .finally(() => { discoverLoading.value = false })
}

function doAdoptPlan(plan) {
  if (plan.userStatus === 1) return
  adoptPlan(plan.id)
    .then(() => { plan.userStatus = 1; uni.showToast({ title: '已采用', icon: 'success' }); loadMyPlans() })
    .catch(() => uni.showToast({ title: '采用失败', icon: 'none' }))
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
    // 重新加载
    refreshSelectedPlan()
    return
  }
  // 加载计划完整数据
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
  const ex = course.exercises[ei]
  // 简化：弹出修改提示
  uni.showModal({
    title: `修改步骤: ${ex.name}`,
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
      // 降级本地保存
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

function getCoverBg(level) {
  const c = { 1: 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)', 2: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)', 3: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', 4: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' }
  return c[level] || c[1]
}
function getLevelEmoji(level) { const e = { 1: '🌱', 2: '🔥', 3: '💪', 4: '🏆' }; return e[level] || '🌱' }
function getTypeEmoji(type) { const e = { 1: '🏃', 2: '💪', 3: '🧘', 4: '⚡', 5: '🔀' }; return e[type] || '📌' }
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
}
.page-scroll {
  height: 100vh;
}

// ====== 顶部操作按钮 ======
.top-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 24rpx;
}
.ta-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}
.ta-icon { font-size: 32rpx; }
.ta-label { font-size: 28rpx; font-weight: 700; }
.create-btn {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
  box-shadow: 0 6rpx 20rpx rgba(34,197,94,0.3);
}
.discover-btn {
  background: #fff;
  color: #334155;
  border: 2rpx solid #e2e8f0;
}

// ====== 发现计划 ======
.discover-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 20rpx;
  overflow: hidden;
}
.questionnaire {
  border-bottom: 1rpx solid #f1f5f9;
}
.q-header {
  display: flex;
  justify-content: space-between;
  padding: 24rpx;
}
.q-title { font-size: 30rpx; font-weight: 700; color: #1e293b; }
.q-toggle { font-size: 24rpx; color: #22c55e; }
.q-body { padding: 0 24rpx 24rpx; }
.q-item { margin-bottom: 24rpx; }
.q-label { font-size: 26rpx; font-weight: 600; color: #334155; display: block; margin-bottom: 12rpx; }
.q-options { display: flex; gap: 14rpx; flex-wrap: wrap; }
.q-opt {
  padding: 12rpx 28rpx; border-radius: 999rpx; font-size: 26rpx;
  color: #64748b; background: #f8fafc; border: 2rpx solid #e2e8f0;
}
.q-opt.selected { background: #f0fdf4; color: #16a34a; border-color: #22c55e; }
.q-submit {
  height: 72rpx; border-radius: 14rpx; background: linear-gradient(135deg,#22c55e,#16a34a);
  display: flex; align-items: center; justify-content: center; margin-top: 10rpx;
}
.q-submit-text { font-size: 28rpx; color: #fff; font-weight: 600; }

.discover-results { padding: 0 24rpx 20rpx; }
.discover-card {
  background: #f8fafc; border-radius: 14rpx; padding: 20rpx; margin-bottom: 14rpx;
}
.dc-top { display: flex; gap: 16rpx; align-items: center; }
.dc-cover {
  width: 72rpx; height: 72rpx; border-radius: 14rpx;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.cover-emoji { font-size: 34rpx; }
.dc-info { flex: 1; min-width: 0; }
.dc-name { font-size: 28rpx; font-weight: 700; color: #1e293b; display: block; }
.dc-desc { font-size: 22rpx; color: #94a3b8; margin-top: 4rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; }
.dc-stats { display: flex; gap: 18rpx; margin-top: 6rpx; }
.dc-stat { font-size: 22rpx; color: #94a3b8; }
.dc-actions { display: flex; gap: 14rpx; margin-top: 16rpx; }
.dc-btn { flex: 1; height: 56rpx; border-radius: 10rpx; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 600; }
.detail-btn { background: #fff; color: #475569; border: 2rpx solid #e2e8f0; }
.adopt-btn { background: #22c55e; color: #fff; }
.adopt-btn.adopted { background: #f1f5f9; color: #94a3b8; }

// ====== 已有计划 ======
.plans-area { padding: 0 24rpx; }
.section-label {
  font-size: 28rpx; font-weight: 700; color: #1e293b;
  padding: 0 0 16rpx;
}
.plan-list { margin-bottom: 16rpx; }
.plan-card {
  background: #fff; border-radius: 20rpx; padding: 24rpx;
  margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.card-top { display: flex; gap: 20rpx; align-items: center; }
.card-cover {
  width: 80rpx; height: 80rpx; border-radius: 16rpx;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.card-info { flex: 1; min-width: 0; }
.card-name { font-size: 30rpx; font-weight: 700; color: #1e293b; display: block; }
.card-desc { font-size: 24rpx; color: #94a3b8; margin-top: 4rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; }
.card-arrow { font-size: 36rpx; color: #cbd5e1; font-weight: 300; }
.card-tags { display: flex; gap: 10rpx; margin-top: 8rpx; }
.tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx; background: #f1f5f9; color: #64748b; }
.level-tag { background: #dcfce7; color: #16a34a; }
.week-tag { background: #dbeafe; color: #2563eb; }
.status-tag { background: #fef3c7; color: #d97706; }
.status-tag.s1 { background: #dcfce7; color: #16a34a; }
.status-tag.s2 { background: #dbeafe; color: #2563eb; }

.card-progress { margin-top: 16rpx; }
.progress-head { display: flex; justify-content: space-between; margin-bottom: 6rpx; }
.progress-label { font-size: 22rpx; color: #94a3b8; }
.progress-pct { font-size: 22rpx; color: #22c55e; font-weight: 600; }
.progress-track { height: 8rpx; border-radius: 999rpx; background: #f1f5f9; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 999rpx; background: linear-gradient(90deg,#22c55e,#16a34a); }

.card-actions { display: flex; gap: 12rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f1f5f9; }
.action-btn {
  flex: 1; height: 52rpx; border-radius: 10rpx; background: #f8fafc;
  display: flex; align-items: center; justify-content: center;
}
.action-text { font-size: 22rpx; color: #475569; font-weight: 500; }
.upload-btn { background: #f0fdf4; }
.upload-btn .action-text { color: #16a34a; }
.del-btn { background: #fef2f2; }
.del-btn .action-text { color: #ef4444; }

// ====== 活动管理区域 ======
.activity-area {
  margin: 8rpx 24rpx 0;
  background: #fff; border-radius: 20rpx; padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.activity-tabs {
  display: flex; background: #f1f5f9; border-radius: 12rpx; padding: 4rpx;
  margin-bottom: 20rpx;
}
.at-tab {
  flex: 1; height: 64rpx; border-radius: 10rpx;
  display: flex; align-items: center; justify-content: center;
  font-size: 26rpx; color: #64748b; font-weight: 600;
}
.at-tab.active { background: #22c55e; color: #fff; }
.at-body { min-height: 120rpx; }

// 修改活动 - 课程列表
.course-editor-card {
  background: #f8fafc; border-radius: 12rpx; padding: 16rpx; margin-bottom: 12rpx;
}
.ce-head { display: flex; align-items: center; gap: 12rpx; }
.ce-emoji { font-size: 28rpx; }
.ce-info { flex: 1; min-width: 0; }
.ce-name { font-size: 26rpx; font-weight: 600; color: #1e293b; display: block; }
.ce-meta { font-size: 22rpx; color: #94a3b8; }
.ce-del { font-size: 26rpx; color: #ef4444; padding: 8rpx; }
.ce-steps { margin-top: 10rpx; padding-left: 8rpx; }
.ce-step { display: flex; align-items: center; gap: 8rpx; padding: 6rpx 0; }
.cs-dot { color: #cbd5e1; }
.cs-name { font-size: 24rpx; color: #475569; }
.cs-meta { font-size: 20rpx; color: #94a3b8; margin-left: auto; }
.cs-edit { font-size: 22rpx; color: #22c55e; padding: 4rpx; }

// 添加活动表单
.acf-label { font-size: 26rpx; font-weight: 600; color: #475569; display: block; margin-bottom: 12rpx; }
.quick-add { display: flex; flex-wrap: wrap; gap: 12rpx; }
.qa-chip {
  padding: 12rpx 20rpx; border-radius: 10rpx; background: #f8fafc;
  border: 2rpx solid #e2e8f0; display: flex; align-items: center; gap: 8rpx;
}
.qa-emoji { font-size: 22rpx; }
.qa-name { font-size: 24rpx; color: #475569; }
.custom-add {
  background: #f8fafc; border-radius: 14rpx; padding: 20rpx;
}
.ca-input {
  height: 64rpx; border-radius: 10rpx; padding: 0 14rpx; background: #fff;
  font-size: 26rpx; color: #1e293b; border: 2rpx solid #e2e8f0; margin-bottom: 12rpx;
}
.ca-input.sm { flex: 1; margin-bottom: 0; }
.ca-row { display: flex; gap: 12rpx; margin-bottom: 12rpx; }
.ca-picker {
  flex: 1; height: 64rpx; border-radius: 10rpx; padding: 0 14rpx;
  background: #fff; border: 2rpx solid #e2e8f0;
  display: flex; align-items: center; font-size: 26rpx; color: #475569;
}
.ca-btn {
  height: 64rpx; border-radius: 10rpx; background: #22c55e;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 26rpx; font-weight: 600;
}

.at-pick-hint {
  text-align: center; padding: 40rpx 20rpx;
  font-size: 26rpx; color: #94a3b8;
}

// 共用
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 80rpx 40rpx; text-align: center;
}
.empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.empty-title { font-size: 30rpx; font-weight: 700; color: #334155; }
.empty-sub { font-size: 26rpx; color: #94a3b8; margin-top: 8rpx; }
.empty-hint { text-align: center; padding: 30rpx; font-size: 24rpx; color: #94a3b8; }

.bottom-spacer { height: 60rpx; }
</style>
