<template>
  <view class="container">
    <!-- 顶部步骤条 -->
    <view class="steps-bar">
      <view class="step" v-for="(label, idx) in stepLabels" :key="idx" @click="currentStep = idx + 1"
        :class="{ active: currentStep >= idx + 1, current: currentStep === idx + 1 }">
        <view class="step-dot">
          <text v-if="currentStep > idx + 1">✓</text>
          <text v-else>{{ idx + 1 }}</text>
        </view>
        <text class="step-label">{{ label }}</text>
        <view class="step-line" v-if="idx < stepLabels.length - 1"
          :class="{ filled: currentStep > idx + 1 }"></view>
      </view>
    </view>

    <!-- Step 1: 基本信息 -->
    <scroll-view v-if="currentStep === 1" class="step-body" scroll-y>
      <view class="form-card">
        <view class="form-group">
          <text class="form-label">计划名称</text>
          <input class="form-input" v-model="planData.name" placeholder="给计划起个名字，如：马拉松备赛4周计划"
            placeholder-style="color:#cbd5e1" />
        </view>
        <view class="form-group">
          <text class="form-label">计划简介</text>
          <textarea class="form-textarea" v-model="planData.description"
            placeholder="简单描述一下这个计划的目标和适用人群..." placeholder-style="color:#cbd5e1" />
        </view>
        <view class="form-group">
          <text class="form-label">持续周数</text>
          <view class="week-picker">
            <view class="week-opt" v-for="w in [2, 4, 6, 8, 12]" :key="w"
              :class="{ selected: planData.totalWeeks === w }" @click="planData.totalWeeks = w">
              {{ w }}周
            </view>
          </view>
        </view>
        <view class="form-group">
          <text class="form-label">难度等级</text>
          <view class="level-picker">
            <view class="level-opt" v-for="(label, val) in levelMap" :key="val"
              :class="{ selected: planData.level === Number(val) }" @click="planData.level = Number(val)">
              {{ label }}
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Step 2: 每周安排 -->
    <view v-if="currentStep === 2" class="step-body-column">
      <!-- 周切换 -->
      <view class="week-switcher">
        <view class="ws-btn" @click="prevWeek" :class="{ disabled: currentWeek <= 1 }">◀</view>
        <text class="ws-title">第 {{ currentWeek }} 周</text>
        <view class="ws-btn" @click="nextWeek"
          :class="{ disabled: currentWeek >= planData.totalWeeks }">▶</view>
      </view>

      <scroll-view class="day-grid-scroll" scroll-y>
        <view class="day-grid">
          <view class="day-card" v-for="(label, idx) in dayLabels" :key="idx"
            :class="{ 'has-courses': getDayCourses(idx + 1).length > 0 }"
            @click="editDay(idx + 1)">
            <text class="day-label">{{ label }}</text>

            <view class="day-courses" v-if="getDayCourses(idx + 1).length > 0">
              <view class="day-course-tag" v-for="(course, ci) in getDayCourses(idx + 1)" :key="ci">
                <text class="dct-type">{{ getTypeEmoji(course.type) }}</text>
                <text class="dct-name">{{ course.name }}</text>
              </view>
            </view>

            <view class="day-empty" v-else>
              <text class="day-empty-text">休息日</text>
            </view>

            <view class="day-edit-hint">点击编辑</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 课程编辑弹窗（Step 2中使用） -->
    <view class="modal-mask" v-if="editingDay > 0" @click="closeDayEditor">
      <view class="modal-card" @click.stop>
        <view class="modal-head">
          <text class="modal-title">周{{ currentWeek }} · 周{{ dayLabels[editingDay - 1] }}</text>
          <text class="modal-close" @click="closeDayEditor">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <!-- 当前课程列表 -->
          <view class="section-label" v-if="getDayCourses(editingDay).length > 0">
            已添加 {{ getDayCourses(editingDay).length }} 项训练
          </view>
          <view class="course-item" v-for="(course, ci) in getDayCourses(editingDay)" :key="ci">
            <view class="ci-left">
              <text class="ci-emoji">{{ getTypeEmoji(course.type) }}</text>
              <view class="ci-info">
                <text class="ci-name">{{ course.name }}</text>
                <text class="ci-meta">
                  {{ course.duration ? course.duration + '分钟' : '' }}
                  {{ course.exercises && course.exercises.length > 0 ? '·' + course.exercises.length + '个步骤' : '' }}
                </text>
              </view>
            </view>
            <view class="ci-actions">
              <text class="ci-edit" @click="editCourse(ci)">✎</text>
              <text class="ci-del" @click="removeCourse(ci)">✕</text>
            </view>
          </view>

          <!-- 快速添加活动 -->
          <view class="section-label">快速添加活动</view>
          <view class="quick-add">
            <view class="qa-item" v-for="act in quickActivities" :key="act.type" @click="quickAddCourse(act)">
              <text class="qa-emoji">{{ act.emoji }}</text>
              <text class="qa-name">{{ act.name }}</text>
            </view>
          </view>

          <!-- 自定义添加 -->
          <view class="section-label">或自定义活动</view>
          <view class="custom-form">
            <input class="cf-input" v-model="customCourse.name" placeholder="活动名称" />
            <view class="cf-row">
              <picker class="cf-picker" :range="activityTypes" :value="customTypeIndex"
                @change="onCustomTypeChange">
                <text>{{ activityTypes[customTypeIndex] }}</text>
              </picker>
              <input class="cf-input sm" v-model.number="customCourse.duration" placeholder="时长(分)" type="number" />
            </view>
            <view class="cf-btn" @click="addCustomCourse">添加活动</view>
          </view>
        </scroll-view>

        <view class="modal-foot">
          <view class="mbtn secondary" @click="closeDayEditor">完成</view>
        </view>
      </view>
    </view>

    <!-- Step 3: 活动定制（全局预览） -->
    <scroll-view v-if="currentStep === 3" class="step-body" scroll-y>
      <view class="section-label">所有训练活动</view>
      <view class="activity-list" v-for="(course, idx) in allCourses" :key="idx">
        <view class="al-header" @click="editingCourseIdx = editingCourseIdx === idx ? -1 : idx">
          <text class="al-emoji">{{ getTypeEmoji(course.type) }}</text>
          <view class="al-info">
            <text class="al-name">第{{ course.week }}周 周{{ dayLabels[course.day - 1] }} · {{ course.name }}</text>
            <text class="al-meta">{{ course.duration || 0 }}分钟 · {{ course.exercises.length }}个步骤</text>
          </view>
          <text class="al-toggle">{{ editingCourseIdx === idx ? '▲' : '▼' }}</text>
        </view>

        <view class="al-detail" v-if="editingCourseIdx === idx">
          <!-- 步骤列表 -->
          <view class="ex-steps">
            <view class="ex-step" v-for="(ex, ei) in course.exercises" :key="ei">
              <view class="ex-drag">≡</view>
              <view class="ex-body">
                <text class="ex-name">{{ ex.name }}</text>
                <text class="ex-meta">
                  <text v-if="ex.duration">{{ ex.duration }}秒</text>
                  <text v-if="ex.sets && ex.reps">{{ ex.sets }}组×{{ ex.reps }}次</text>
                  <text v-if="ex.distance">{{ (ex.distance / 1000).toFixed(1) }}km</text>
                </text>
              </view>
              <text class="ex-del" @click="removeExercise(idx, ei)">✕</text>
            </view>
          </view>

          <!-- 添加步骤表单 -->
          <view class="add-step-form">
            <text class="asf-label">添加步骤</text>
            <view class="asf-row">
              <input class="asf-input" v-model="newExercise.name" placeholder="步骤名称" />
              <picker class="asf-picker" :range="exerciseTypes" :value="exTypeIndex"
                @change="onExTypeChange">
                <text>{{ exerciseTypes[exTypeIndex] }}</text>
              </picker>
            </view>
            <view class="asf-row" v-if="newExercise.type === 1">
              <input class="asf-input sm" v-model.number="newExercise.duration" placeholder="时长(秒)" type="number" />
              <input class="asf-input sm" v-model.number="newExercise.distance" placeholder="距离(米)" type="number" />
            </view>
            <view class="asf-row" v-if="newExercise.type === 2">
              <input class="asf-input sm" v-model.number="newExercise.sets" placeholder="组数" type="number" />
              <input class="asf-input sm" v-model.number="newExercise.reps" placeholder="次数" type="number" />
              <input class="asf-input sm" v-model.number="newExercise.restTime" placeholder="休息(秒)" type="number" />
            </view>
            <view class="asf-row">
              <view class="asf-btn add" @click="addExercise(idx)">添加步骤</view>
              <view class="asf-btn repeat" @click="addRepeatGroup(idx)">添加重复组</view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="allCourses.length === 0">
        <text class="empty-icon">📝</text>
        <text class="empty-title">还没有添加训练活动</text>
        <text class="empty-sub">请返回上一步，点击日期添加训练内容</text>
      </view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="step-footer">
      <view class="sf-btn secondary" v-if="currentStep > 1" @click="currentStep--">上一步</view>
      <view class="sf-btn primary" v-if="currentStep < 3" @click="currentStep++">下一步</view>
      <view class="sf-btn primary" v-if="currentStep === 3" @click="savePlan" :class="{ loading: saving }">
        {{ saving ? '保存中...' : '保存计划' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { createPlan, getPlanDetail, updatePlan } from '@/api/plan'

const currentStep = ref(1)
const saving = ref(false)
const currentWeek = ref(1)
const editingDay = ref(0)
const editingCourseIdx = ref(-1)
const customTypeIndex = ref(0)
const exTypeIndex = ref(0)

const stepLabels = ['基本信息', '每周安排', '活动定制']

const dayLabels = ['一', '二', '三', '四', '五', '六', '日']

const levelMap = { 1: '新手', 2: '进阶', 3: '高手', 4: '专家' }

const activityTypes = ['有氧', '力量', '拉伸', 'HIIT', '综合']
const exerciseTypes = ['有氧', '力量', '拉伸']

const quickActivities = [
  { type: 1, name: '热身跑', emoji: '🏃', duration: 10, warmUpDuration: 10 },
  { type: 2, name: '400米间歇跑', emoji: '🏃', duration: 45, exercises: [
    { name: '热身慢跑', type: 1, duration: 600, distance: 2000 },
    { name: '400米冲刺', type: 1, duration: 90, distance: 400, sets: 8, reps: 1, restTime: 90 },
    { name: '缓和慢跑', type: 3, duration: 600 }
  ]},
  { type: 2, name: '长距离慢跑', emoji: '🏃', duration: 60, exercises: [
    { name: '热身', type: 1, duration: 300 },
    { name: '慢跑', type: 1, duration: 3000, distance: 10000 },
    { name: '缓和', type: 3, duration: 300 }
  ]},
  { type: 2, name: '节奏跑', emoji: '🏃', duration: 50, exercises: [
    { name: '热身', type: 1, duration: 600, distance: 2000 },
    { name: '节奏跑', type: 1, duration: 1800, distance: 5000 },
    { name: '缓和', type: 3, duration: 600 }
  ]},
  { type: 2, name: '全身力量训练', emoji: '💪', duration: 45, exercises: [
    { name: '深蹲', type: 2, sets: 4, reps: 12, restTime: 60 },
    { name: '俯卧撑', type: 2, sets: 4, reps: 15, restTime: 60 },
    { name: '引体向上', type: 2, sets: 3, reps: 8, restTime: 90 }
  ]},
  { type: 1, name: '有氧操', emoji: '🤸', duration: 40 },
  { type: 3, name: '瑜伽拉伸', emoji: '🧘', duration: 30 },
  { type: 1, name: '骑行', emoji: '🚴', duration: 60 },
  { type: 1, name: '游泳', emoji: '🏊', duration: 45 },
]

const planData = reactive({
  id: null,
  name: '',
  description: '',
  totalWeeks: 4,
  level: 1,
  // courses: [{ week, day, name, type, duration, warmUpDuration, coolDownDuration, exercises: [{ name, type, duration, sets, reps, restTime, distance }] }]
  courses: []
})

const customCourse = reactive({
  name: '',
  type: 1,
  duration: 30
})

const newExercise = reactive({
  name: '',
  type: 1,
  duration: null,
  sets: null,
  reps: null,
  restTime: null,
  distance: null
})

const allCourses = computed(() => {
  return planData.courses.sort((a, b) => {
    if (a.week !== b.week) return a.week - b.week
    if (a.day !== b.day) return a.day - b.day
    return 0
  })
})

onMounted(() => {
  // 检查是否是编辑模式
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  if (page && page.options && page.options.id) {
    loadPlan(page.options.id)
  }
})

function loadPlan(id) {
  getPlanDetail(id).then(res => {
    if (res.code === 200 && res.data) {
      const detail = res.data
      planData.id = detail.id
      planData.name = detail.name
      planData.description = detail.description || ''
      planData.totalWeeks = detail.totalWeeks
      planData.level = detail.level
      // 转换课程
      if (detail.courses) {
        planData.courses = detail.courses.map(c => ({
          week: c.week,
          day: c.day,
          name: c.name,
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
            distance: e.distance,
            isRepeatGroup: e.isRepeatGroup,
            repeatCount: e.repeatCount
          }))
        }))
      }
    }
  }).catch(() => {})
}

function getDayCourses(day) {
  return planData.courses.filter(c => c.week === currentWeek.value && c.day === day)
}

function prevWeek() {
  if (currentWeek.value > 1) currentWeek.value--
}

function nextWeek() {
  if (currentWeek.value < planData.totalWeeks) currentWeek.value++
}

function editDay(day) {
  editingDay.value = day
  customCourse.name = ''
  customCourse.type = 1
  customCourse.duration = 30
}

function closeDayEditor() {
  editingDay.value = 0
}

function quickAddCourse(act) {
  const course = {
    week: currentWeek.value,
    day: editingDay.value,
    name: act.name,
    type: act.type,
    duration: act.duration,
    warmUpDuration: act.warmUpDuration || 0,
    coolDownDuration: 0,
    exercises: (act.exercises || []).map(e => ({ ...e }))
  }
  planData.courses.push(course)
}

function addCustomCourse() {
  if (!customCourse.name.trim()) {
    uni.showToast({ title: '请输入活动名称', icon: 'none' })
    return
  }
  const course = {
    week: currentWeek.value,
    day: editingDay.value,
    name: customCourse.name.trim(),
    type: customCourse.type,
    duration: customCourse.duration,
    warmUpDuration: 0,
    coolDownDuration: 0,
    exercises: [{ name: customCourse.name.trim(), type: customCourse.type, duration: customCourse.duration * 60 }]
  }
  planData.courses.push(course)
  customCourse.name = ''
}

function editCourse(idx) {
  // 跳转到step 3并展开对应课程
  editingDay.value = 0
  currentStep.value = 3
  setTimeout(() => {
    editingCourseIdx.value = idx
  }, 100)
}

function removeCourse(idx) {
  const dayCourses = getDayCourses(editingDay.value)
  if (dayCourses[idx]) {
    planData.courses = planData.courses.filter(c => c !== dayCourses[idx])
  }
}

function onCustomTypeChange(e) {
  customTypeIndex.value = e.detail.value
  customCourse.type = e.detail.value + 1
}

function onExTypeChange(e) {
  exTypeIndex.value = e.detail.value
  newExercise.type = e.detail.value + 1
}

function addExercise(courseIdx) {
  if (!newExercise.name.trim()) {
    uni.showToast({ title: '请输入步骤名称', icon: 'none' })
    return
  }
  const course = allCourses.value[courseIdx]
  if (!course.exercises) course.exercises = []
  course.exercises.push({
    name: newExercise.name.trim(),
    type: newExercise.type,
    duration: newExercise.duration,
    sets: newExercise.sets,
    reps: newExercise.reps,
    restTime: newExercise.restTime,
    distance: newExercise.distance
  })
  // 重置
  newExercise.name = ''
  newExercise.duration = null
  newExercise.sets = null
  newExercise.reps = null
  newExercise.restTime = null
  newExercise.distance = null
}

function addRepeatGroup(courseIdx) {
  const course = allCourses.value[courseIdx]
  if (!course.exercises || course.exercises.length === 0) {
    uni.showToast({ title: '请先添加至少一个步骤', icon: 'none' })
    return
  }
  // 简化：将当前所有步骤标记为重复组
  const lastEx = course.exercises[course.exercises.length - 1]
  lastEx.isRepeatGroup = true
  lastEx.repeatCount = lastEx.repeatCount || 3
  uni.showToast({ title: '已标记为重复组(3次)', icon: 'success' })
}

function removeExercise(courseIdx, exIdx) {
  const course = allCourses.value[courseIdx]
  if (course && course.exercises) {
    course.exercises.splice(exIdx, 1)
  }
}

function getTypeEmoji(type) {
  const emojis = { 1: '🏃', 2: '💪', 3: '🧘', 4: '⚡', 5: '🔀', 6: '🧊', 7: '🔀' }
  return emojis[type] || '📌'
}

function savePlan() {
  if (!planData.name.trim()) {
    uni.showToast({ title: '请输入计划名称', icon: 'none' })
    currentStep.value = 1
    return
  }

  saving.value = true
  const data = {
    name: planData.name.trim(),
    description: planData.description.trim(),
    totalWeeks: planData.totalWeeks,
    level: planData.level,
    courses: planData.courses.map(c => ({
      name: c.name,
      description: '',
      week: c.week,
      day: c.day,
      type: c.type,
      level: planData.level,
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
        distance: e.distance,
        isRepeatGroup: e.isRepeatGroup || false,
        repeatCount: e.repeatCount || 1
      }))
    }))
  }

  const apiCall = planData.id ? updatePlan(planData.id, data) : createPlan(data)

  apiCall
    .then(res => {
      if (res.code === 200) {
        uni.showToast({ title: '保存成功', icon: 'success' })
        // 清除本地缓存
        uni.removeStorageSync('weeklyPlan_current')
        setTimeout(() => uni.navigateBack(), 1200)
      } else {
        uni.showToast({ title: res.message || '保存失败', icon: 'none' })
      }
    })
    .catch(() => {
      // 降级到本地存储
      saveLocalPlan(data)
    })
    .finally(() => {
      saving.value = false
    })
}

function saveLocalPlan(data) {
  const localPlans = uni.getStorageSync('myTrainingPlans') || '[]'
  let plans
  try { plans = JSON.parse(localPlans) } catch (e) { plans = [] }

  if (planData.id) {
    const idx = plans.findIndex(p => p.id === planData.id)
    if (idx > -1) plans[idx] = { ...data, id: planData.id }
  } else {
    planData.id = Date.now()
    plans.push({ ...data, id: planData.id })
  }

  uni.setStorageSync('myTrainingPlans', JSON.stringify(plans))
  uni.showToast({ title: '已保存(本地)', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1200)
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

// 步骤条
.steps-bar {
  display: flex;
  padding: 24rpx 32rpx;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.step {
  flex: 1;
  display: flex;
  align-items: center;
  position: relative;
}

.step-dot {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 600;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.step.active .step-dot {
  background: #22c55e;
  color: #fff;
}

.step.current .step-label {
  color: #16a34a;
  font-weight: 700;
}

.step-label {
  font-size: 22rpx;
  color: #94a3b8;
  white-space: nowrap;
}

.step-line {
  position: absolute;
  left: 42rpx;
  top: 50%;
  width: calc(100% - 52rpx);
  height: 3rpx;
  background: #f1f5f9;
  transform: translateY(-50%);
  z-index: -1;
}

.step-line.filled {
  background: #22c55e;
}

// Step Body
.step-body {
  flex: 1;
  padding: 20rpx 24rpx;
}

.step-body-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

// Form
.form-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  display: block;
  margin-bottom: 14rpx;
}

.form-input {
  height: 80rpx;
  border-radius: 12rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  font-size: 28rpx;
  color: #1e293b;
  border: 2rpx solid #e2e8f0;
}

.form-textarea {
  min-height: 140rpx;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  background: #f8fafc;
  font-size: 28rpx;
  color: #1e293b;
  border: 2rpx solid #e2e8f0;
  width: 100%;
  box-sizing: border-box;
}

.week-picker, .level-picker {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.week-opt, .level-opt {
  padding: 16rpx 32rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #64748b;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  transition: all 0.2s;
}

.week-opt.selected, .level-opt.selected {
  background: #f0fdf4;
  color: #16a34a;
  border-color: #22c55e;
}

// Week Switcher
.week-switcher {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  background: #fff;
  gap: 24rpx;
}

.ws-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #64748b;
}

.ws-btn.disabled {
  opacity: 0.3;
}

.ws-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1e293b;
}

// Day Grid
.day-grid-scroll {
  flex: 1;
  padding: 0 24rpx;
}

.day-grid {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  padding-bottom: 20rpx;
}

.day-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
  position: relative;
}

.day-card.has-courses {
  border-color: #bbf7d0;
}

.day-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
}

.day-courses {
  margin-top: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.day-course-tag {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: #f0fdf4;
  padding: 10rpx 16rpx;
  border-radius: 8rpx;
}

.dct-type {
  font-size: 28rpx;
}

.dct-name {
  font-size: 26rpx;
  color: #166534;
  font-weight: 500;
}

.day-empty {
  margin-top: 12rpx;
}

.day-empty-text {
  font-size: 24rpx;
  color: #cbd5e1;
}

.day-edit-hint {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 22rpx;
  color: #94a3b8;
}

// Modal
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.modal-card {
  width: 100%;
  max-height: 75vh;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  display: flex;
  flex-direction: column;
}

.modal-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 28rpx 16rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1e293b;
}

.modal-close {
  font-size: 32rpx;
  color: #94a3b8;
  padding: 8rpx;
}

.modal-body {
  flex: 1;
  padding: 0 28rpx;
  max-height: 50vh;
  min-height: 300rpx;
}

.modal-foot {
  padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom));
}

.mbtn {
  height: 80rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
}

.mbtn.secondary {
  background: #f1f5f9;
  color: #64748b;
}

.section-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #64748b;
  padding: 16rpx 0 12rpx;
}

// Course Items in Modal
.course-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  margin-bottom: 10rpx;
}

.ci-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
  flex: 1;
}

.ci-emoji {
  font-size: 32rpx;
}

.ci-info {
  flex: 1;
  min-width: 0;
}

.ci-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  display: block;
}

.ci-meta {
  font-size: 22rpx;
  color: #94a3b8;
}

.ci-actions {
  display: flex;
  gap: 20rpx;
}

.ci-edit {
  font-size: 28rpx;
  color: #22c55e;
}

.ci-del {
  font-size: 28rpx;
  color: #ef4444;
}

// Quick Add
.quick-add {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.qa-item {
  padding: 14rpx 22rpx;
  border-radius: 10rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.qa-emoji {
  font-size: 24rpx;
}

.qa-name {
  font-size: 24rpx;
  color: #475569;
}

// Custom Form
.custom-form {
  background: #f8fafc;
  border-radius: 14rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.cf-input {
  height: 64rpx;
  border-radius: 10rpx;
  padding: 0 16rpx;
  background: #fff;
  font-size: 26rpx;
  color: #1e293b;
  border: 2rpx solid #e2e8f0;
  margin-bottom: 12rpx;
}

.cf-input.sm {
  flex: 1;
  margin-bottom: 0;
}

.cf-row {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.cf-picker {
  flex: 1;
  height: 64rpx;
  border-radius: 10rpx;
  padding: 0 16rpx;
  background: #fff;
  border: 2rpx solid #e2e8f0;
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #475569;
}

.cf-btn {
  height: 64rpx;
  border-radius: 10rpx;
  background: #22c55e;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}

// Step 3 Activity List
.activity-list {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 14rpx;
  overflow: hidden;
}

.al-header {
  display: flex;
  align-items: center;
  padding: 20rpx;
  gap: 14rpx;
}

.al-emoji {
  font-size: 36rpx;
}

.al-info {
  flex: 1;
  min-width: 0;
}

.al-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  display: block;
}

.al-meta {
  font-size: 22rpx;
  color: #94a3b8;
}

.al-toggle {
  font-size: 24rpx;
  color: #94a3b8;
}

.al-detail {
  border-top: 1rpx solid #f1f5f9;
  padding: 16rpx 20rpx 20rpx;
}

.ex-steps {
  margin-bottom: 16rpx;
}

.ex-step {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f8fafc;
}

.ex-drag {
  font-size: 28rpx;
  color: #cbd5e1;
}

.ex-body {
  flex: 1;
  min-width: 0;
}

.ex-name {
  font-size: 26rpx;
  font-weight: 500;
  color: #334155;
  display: block;
}

.ex-meta {
  font-size: 22rpx;
  color: #94a3b8;
}

.ex-del {
  font-size: 24rpx;
  color: #ef4444;
  padding: 8rpx;
}

.add-step-form {
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}

.asf-label {
  font-size: 24rpx;
  font-weight: 600;
  color: #64748b;
  display: block;
  margin-bottom: 10rpx;
}

.asf-row {
  display: flex;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.asf-input {
  flex: 1;
  height: 56rpx;
  border-radius: 8rpx;
  padding: 0 12rpx;
  background: #fff;
  font-size: 24rpx;
  color: #1e293b;
  border: 2rpx solid #e2e8f0;
}

.asf-input.sm {
  flex: 1;
}

.asf-picker {
  flex: 1;
  height: 56rpx;
  border-radius: 8rpx;
  padding: 0 12rpx;
  background: #fff;
  border: 2rpx solid #e2e8f0;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #475569;
}

.asf-btn {
  flex: 1;
  height: 56rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
}

.asf-btn.add {
  background: #22c55e;
  color: #fff;
}

.asf-btn.repeat {
  background: #dbeafe;
  color: #2563eb;
}

// Footer
.step-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f1f5f9;
}

.sf-btn {
  flex: 1;
  height: 84rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
}

.sf-btn.primary {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
}

.sf-btn.primary.loading {
  opacity: 0.7;
}

.sf-btn.secondary {
  background: #f1f5f9;
  color: #64748b;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
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

.empty-sub {
  font-size: 26rpx;
  color: #94a3b8;
  margin-top: 8rpx;
}
</style>
