<template>
  <view class="container">
    <!-- 顶部步骤条 -->
    <view class="steps-bar">
      <view class="step" v-for="(label, idx) in stepLabels" :key="idx" @click="currentStep = idx + 1"
        :class="{ active: currentStep >= idx + 1, current: currentStep === idx + 1 }">
        <view class="step-dot">
          <AppIcon v-if="currentStep > idx + 1" name="check" size="22" />
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
        <view class="ws-btn" @click="prevWeek" :class="{ disabled: currentWeek <= 1 }">
          <AppIcon name="arrowLeft" size="26" bold />
        </view>
        <text class="ws-title">第 {{ currentWeek }} 周</text>
        <view class="ws-btn" @click="nextWeek"
          :class="{ disabled: currentWeek >= planData.totalWeeks }">
          <AppIcon name="arrowRight" size="26" bold />
        </view>
      </view>
      <!-- 周操作栏 -->
      <view class="week-actions">
        <text class="week-status">已设置 {{ getWeekCourseCount(currentWeek) }} 天</text>
        <view class="week-copy-btns">
          <view class="wcp-btn" v-if="currentWeek < planData.totalWeeks" @click="copyWeekToNext">复制到下周</view>
          <view class="wcp-btn primary" @click="copyWeekToAll">应用到所有周</view>
        </view>
      </view>
      <scroll-view class="day-grid-scroll" scroll-y>
        <view class="day-grid">
          <view class="day-card" v-for="(label, idx) in dayLabels" :key="idx"
            :class="{ 'has-courses': getDayCourses(idx + 1).length > 0 }"
            @click="editDay(idx + 1)">
            <text class="day-label">{{ label }}</text>
            <view class="day-courses" v-if="getDayCourses(idx + 1).length > 0">
              <view class="day-course-tag" v-for="(course, ci) in getDayCourses(idx + 1)" :key="ci">
                <text class="dct-type"></text>
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
    <view class="modal-mask" v-if="editingDay > 0" @click="onMaskClick">
      <view class="modal-card" @click="onCardClick">
        <view class="modal-head">
          <text class="modal-title">周{{ currentWeek }} · 周{{ dayLabels[editingDay - 1] }}</text>
          <AppIcon class="modal-close" name="close" size="28" @click="closeDayEditor" />
        </view>
        <!-- 已选课程（可点击展开设计详情） -->
        <view class="selected-bar" v-if="getDayCourses(editingDay).length > 0">
          <scroll-view class="selected-scroll" scroll-x :show-scrollbar="false">
            <view class="selected-chip" v-for="(course, ci) in getDayCourses(editingDay)" :key="ci"
              :class="{ 'chip-active': selectedDesignIdx === ci }"
              @click="selectDesignCourse(ci)">
              <AppIcon class="sc-type-icon" :name="getTypeIcon(course.type)" size="24" />
              <text class="sc-name">{{ course.name }}</text>
              <AppIcon class="sc-del" name="close" size="22" @click.stop="removeCourse(ci)" />
            </view>
          </scroll-view>
        </view>
        <!-- 小项目设计器（选中某个活动时展开） -->
        <view class="designer-panel" v-if="selectedDesignCourse">
          <view class="dp-head">
            <text class="dp-title">小项目设计 · {{ selectedDesignCourse.name }}</text>
            <view class="dp-close" @click="selectedDesignIdx = -1">
              <text>收起</text>
              <AppIcon name="arrowUp" size="22" bold />
            </view>
          </view>
          <!-- 已有步骤列表 -->
          <view class="dp-steps" v-if="selectedDesignCourse.exercises && selectedDesignCourse.exercises.length > 0">
            <view class="dp-step" v-for="(ex, ei) in selectedDesignCourse.exercises" :key="ei">
              <AppIcon class="dps-icon" :name="ex.type === 2 ? 'strength' : ex.type === 3 ? 'stretch' : 'run'" size="30" />
              <view class="dps-info">
                <text class="dps-name">{{ ex.name }}</text>
                <text class="dps-meta" v-if="ex.duration">{{ Math.round(ex.duration/60) }}分</text>
                <text class="dps-meta" v-if="ex.sets">{{ ex.sets }}组×{{ ex.reps }}次</text>
              </view>
              <AppIcon class="dps-del" name="close" size="22" @click="removeDesignerStep(ei)" />
            </view>
          </view>
          <!-- 添加步骤 Tab: 默认项目 / 自定义项目 -->
          <view class="dp-tabs">
            <text class="dp-tab" :class="{ active: designerTab === 'preset' }" @click="designerTab = 'preset'">默认项目</text>
            <text class="dp-tab" :class="{ active: designerTab === 'custom' }" @click="designerTab = 'custom'">自定义项目</text>
          </view>
          <!-- 默认步骤列表 -->
          <view class="dp-preset-list" v-if="designerTab === 'preset'">
            <view class="dp-preset-item" v-for="(preset, pi) in presetExercises" :key="pi" @click="addPresetStep(preset)">
              <AppIcon class="dpp-icon" :name="preset.icon" size="28" />
              <text class="dpp-name">{{ preset.name }}</text>
              <text class="dpp-meta">{{ preset.meta }}</text>
            </view>
          </view>
          <!-- 自定义步骤表单 -->
          <view class="dp-custom-form" v-if="designerTab === 'custom'">
            <view class="dp-cf-row">
              <input class="dp-cf-input" v-model="designerStep.name" placeholder="步骤名称（如：深蹲）" />
              <picker class="dp-cf-picker" :range="['有氧', '力量', '拉伸']" :value="designerStepTypeIndex"
                @change="onDesignerTypeChange">
                <text>{{ ['有氧', '力量', '拉伸'][designerStepTypeIndex] }}</text>
              </picker>
            </view>
            <view class="dp-cf-row" v-if="designerStep.type === 1">
              <input class="dp-cf-input sm" v-model.number="designerStep.duration" placeholder="时长(秒)" type="number" />
              <input class="dp-cf-input sm" v-model.number="designerStep.distance" placeholder="距离(米)" type="number" />
            </view>
            <view class="dp-cf-row" v-if="designerStep.type === 2">
              <input class="dp-cf-input sm" v-model.number="designerStep.sets" placeholder="组数" type="number" />
              <input class="dp-cf-input sm" v-model.number="designerStep.reps" placeholder="次数" type="number" />
              <input class="dp-cf-input sm" v-model.number="designerStep.rest" placeholder="休息(秒)" type="number" />
            </view>
            <view class="dp-cf-btn" @click="addDesignerStep">+ 添加步骤</view>
          </view>
        </view>
        <!-- 左右分栏主体 -->
        <view class="modal-body">
          <!-- 左侧筛选栏 -->
          <view class="add-filter-side">
            <view class="add-filter-item" :class="{ active: addTab === 'quick' }" @click="addTab = 'quick'">
              <text class="afi-text">快速添加</text>
            </view>
            <view class="add-filter-item" :class="{ active: addTab === 'my' }" @click="addTab = 'my'">
              <text class="afi-text">我的活动</text>
            </view>
            <view class="add-filter-item" :class="{ active: addTab === 'custom' }" @click="addTab = 'custom'">
              <text class="afi-text">自定义</text>
            </view>
          </view>
          <!-- 右侧内容区 -->
          <scroll-view class="add-filter-content" scroll-y>
            <!-- 快速添加 -->
            <view v-if="addTab === 'quick'">
              <view class="content-title">快速添加活动</view>
              <view class="content-grid">
                <view class="content-chip" v-for="act in quickActivities" :key="act.type" @click="quickAddCourse(act)">
                  <text class="cc-name">{{ act.name }}</text>
                  <text class="cc-meta">{{ act.duration }}分钟</text>
                </view>
              </view>
            </view>
            <!-- 我的活动 -->
            <view v-if="addTab === 'my'">
              <view class="content-title">我的活动</view>
              <view class="content-grid" v-if="myActivities.length > 0">
                <view class="content-chip" v-for="act in myActivities" :key="act.id" @click="addMyActivity(act)">
                  <text class="cc-name">{{ act.title || act.name }}</text>
                  <text class="cc-meta">{{ (act.exercises || []).length }}个步骤</text>
                </view>
              </view>
              <view class="content-empty" v-else>
                <text>暂无自定义活动</text>
                <text class="ce-hint">去「定制」页面创建吧</text>
              </view>
            </view>
            <!-- 自定义添加 -->
            <view v-if="addTab === 'custom'">
              <view class="content-title">自定义活动</view>
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
            </view>
          </scroll-view>
        </view>
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
          <AppIcon class="al-icon" :name="getTypeIcon(course.type)" size="30" />
          <view class="al-info">
            <text class="al-name">第{{ course.week }}周 周{{ dayLabels[course.day - 1] }} · {{ course.name }}</text>
            <text class="al-meta">{{ course.duration || 0 }}分钟 · {{ course.exercises.length }}个步骤</text>
          </view>
          <AppIcon class="al-toggle" :name="editingCourseIdx === idx ? 'arrowUp' : 'arrowDown'" size="24" bold />
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
              <AppIcon class="ex-del" name="close" size="22" @click="removeExercise(idx, ei)" />
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
        <text class="empty-icon"></text>
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
  { type: 1, name: '热身跑', duration: 10, warmUpDuration: 10 },
  { type: 2, name: '400米间歇跑', duration: 45, exercises: [
    { name: '热身慢跑', type: 1, duration: 600, distance: 2000 },
    { name: '400米冲刺', type: 1, duration: 90, distance: 400, sets: 8, reps: 1, restTime: 90 },
    { name: '缓和慢跑', type: 3, duration: 600 }
  ]},
  { type: 2, name: '长距离慢跑', duration: 60, exercises: [
    { name: '热身', type: 1, duration: 300 },
    { name: '慢跑', type: 1, duration: 3000, distance: 10000 },
    { name: '缓和', type: 3, duration: 300 }
  ]},
  { type: 2, name: '节奏跑', duration: 50, exercises: [
    { name: '热身', type: 1, duration: 600, distance: 2000 },
    { name: '节奏跑', type: 1, duration: 1800, distance: 5000 },
    { name: '缓和', type: 3, duration: 600 }
  ]},
  { type: 2, name: '全身力量训练', duration: 45, exercises: [
    { name: '深蹲', type: 2, sets: 4, reps: 12, restTime: 60 },
    { name: '俯卧撑', type: 2, sets: 4, reps: 15, restTime: 60 },
    { name: '引体向上', type: 2, sets: 3, reps: 8, restTime: 90 }
  ]},
  { type: 1, name: '有氧操', duration: 40 },
  { type: 3, name: '瑜伽拉伸', duration: 30 },
  { type: 1, name: '骑行', duration: 60 },
  { type: 1, name: '游泳', duration: 45 },
]
const planData = reactive({
  id: null,
  name: '',
  description: '',
  totalWeeks: 4,
  level: 1,
  startDate: '',
  // courses: [{ week, day, name, type, duration, warmUpDuration, coolDownDuration, exercises: [{ name, type, duration, sets, reps, restTime, distance }] }]
  courses: []
})
const customCourse = reactive({
  name: '',
  type: 1,
  duration: 30
})
const addTab = ref('quick')
const myActivities = ref([])
const selectedDesignIdx = ref(-1)
const designerTab = ref('preset')
const designerStepTypeIndex = ref(0)
const designerStep = reactive({
  name: '', type: 1, duration: null, distance: null,
  sets: null, reps: null, rest: null
})
const presetExercises = [
  { icon: 'run', name: '慢跑热身', type: 1, meta: '5-10分 · 1-2km', duration: 600, distance: 2000 },
  { icon: 'run', name: '400米冲刺', type: 1, meta: '90秒 · 1组', duration: 90, distance: 400, sets: 1, reps: 1 },
  { icon: 'run', name: '800米间歇', type: 1, meta: '180秒 · 4组', duration: 180, distance: 800, sets: 4, reps: 1 },
  { icon: 'strength', name: '深蹲', type: 2, meta: '4组×12次', sets: 4, reps: 12 },
  { icon: 'strength', name: '俯卧撑', type: 2, meta: '4组×15次', sets: 4, reps: 15 },
  { icon: 'strength', name: '平板支撑', type: 2, meta: '3组×60秒', sets: 3, reps: 60 },
  { icon: 'strength', name: '卷腹', type: 2, meta: '3组×20次', sets: 3, reps: 20 },
  { icon: 'strength', name: '弓步蹲', type: 2, meta: '3组×12次/腿', sets: 3, reps: 12 },
  { icon: 'stretch', name: '全身拉伸', type: 3, meta: '10分钟', duration: 600 },
  { icon: 'stretch', name: '哈他瑜伽', type: 3, meta: '30分钟', duration: 1800 },
  { icon: 'stretch', name: '泡沫轴放松', type: 3, meta: '15分钟', duration: 900 },
  { icon: 'walk', name: '缓和散步', type: 1, meta: '5-10分 · 0.5km', duration: 300, distance: 500 },
]
const newExercise = reactive({
  name: '',
  type: 1,
  duration: null,
  sets: null,
  reps: null,
  restTime: null,
  distance: null
})
const selectedDesignCourse = computed(() => {
  if (selectedDesignIdx.value < 0) return null
  const courses = getDayCourses(editingDay.value)
  return courses[selectedDesignIdx.value] || null
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
  } else {
    planData.startDate = formatDate(new Date())
  }
  // 检查是否有 AI 生成的计划需要导入
  loadAiPlan()
})
function loadAiPlan() {
  const stored = uni.getStorageSync('ai_generated_plan')
  if (!stored) return
  // 读取后立即清除，防止重复导入
  uni.removeStorageSync('ai_generated_plan')
  // 结构化 JSON 计划数据
  if (stored.name && stored.courses) {
    planData.name = stored.name || ''
    planData.description = stored.description || ''
    planData.totalWeeks = stored.totalWeeks || 4
    planData.level = stored.level || 1
    planData.courses = (stored.courses || []).map(c => ({
      week: c.week,
      day: c.day,
      name: c.name,
      type: c.type,
      duration: c.duration,
      warmUpDuration: 0,
      coolDownDuration: 0,
      description: c.description || '',
      exercises: (c.exercises || []).map(e => ({
        name: e.name,
        type: e.type,
        duration: e.duration || 0,
        sets: e.sets || null,
        reps: e.reps || null,
        restTime: e.restTime || null,
        distance: e.distance || null,
        description: e.description || ''
      }))
    }))
    uni.showToast({ title: '已导入 AI 训练方案', icon: 'success' })
  } else if (stored.rawText) {
    // 纯文本降级：仅填充名称
    planData.name = 'AI 训练方案'
    planData.description = stored.rawText.substring(0, 200)
  }
}
function loadPlan(id) {
  getPlanDetail(id).then(res => {
    if (res.code === 200 && res.data) {
      const detail = res.data
      planData.id = detail.id
      planData.name = detail.name
      planData.description = detail.description || ''
      planData.totalWeeks = detail.totalWeeks
      planData.level = detail.level
      planData.startDate = detail.startDate || formatDate(new Date())
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
  }).catch(() => {
    // API失败时尝试从本地加载
    const local = uni.getStorageSync('myTrainingPlans')
    let plans = []
    try { plans = JSON.parse(local || '[]') } catch (e) { plans = [] }
    const plan = plans.find(p => String(p.id) === String(id))
    if (plan) {
      planData.id = plan.id
      planData.name = plan.name
      planData.description = plan.description || ''
      planData.totalWeeks = plan.totalWeeks || 4
      planData.level = plan.level || 1
      planData.startDate = plan.startDate || formatDate(new Date())
      planData.courses = (plan.courses || []).map(c => ({
        week: c.week,
        day: c.day,
        name: c.name,
        type: c.type,
        duration: c.duration,
        warmUpDuration: c.warmUpDuration || 0,
        coolDownDuration: c.coolDownDuration || 0,
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
  })
}
function getDayCourses(day) {
  return planData.courses.filter(c => c.week === currentWeek.value && c.day === day)
}
function getWeekCourseCount(week) {
  const days = new Set(planData.courses.filter(c => c.week === week).map(c => c.day))
  return days.size
}
function copyWeekToNext() {
  const sourceWeek = currentWeek.value
  const targetWeek = sourceWeek + 1
  if (targetWeek > planData.totalWeeks) {
    uni.showToast({ title: '已经是最后一周', icon: 'none' })
    return
  }
  doCopyWeek(sourceWeek, [targetWeek])
}
function copyWeekToAll() {
  const sourceWeek = currentWeek.value
  const targetWeeks = []
  for (let w = 1; w <= planData.totalWeeks; w++) {
    if (w !== sourceWeek) targetWeeks.push(w)
  }
  if (targetWeeks.length === 0) return
  uni.showModal({
    title: '应用到所有周',
    content: `将第${sourceWeek}周的安排复制到其余 ${targetWeeks.length} 周，会覆盖目标周已有内容。确定吗？`,
    confirmText: '确定复制',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) doCopyWeek(sourceWeek, targetWeeks)
    }
  })
}
function doCopyWeek(sourceWeek, targetWeeks) {
  const sourceCourses = planData.courses.filter(c => c.week === sourceWeek)
  if (sourceCourses.length === 0) {
    uni.showToast({ title: '当前周没有安排', icon: 'none' })
    return
  }
  targetWeeks.forEach(targetWeek => {
    // 删除目标周原有课程
    planData.courses = planData.courses.filter(c => c.week !== targetWeek)
    // 复制源周课程到目标周
    sourceCourses.forEach(course => {
      planData.courses.push({
        ...course,
        week: targetWeek
      })
    })
  })
  uni.showToast({ title: '复制成功', icon: 'success' })
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
  addTab.value = 'quick'
  loadMyActivities()
}
function closeDayEditor() {
  editingDay.value = 0
}
// 微信小程序不支持 @click.stop，改用方法判断
function onMaskClick() {
  closeDayEditor()
}
function onCardClick() {
  // 阻止冒泡 - 微信小程序中使用空方法阻止事件传播
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
function loadMyActivities() {
  try {
    const raw = uni.getStorageSync('userActivities')
    if (raw) {
      const list = typeof raw === 'string' ? JSON.parse(raw) : raw
      myActivities.value = (list || []).map(item => ({
        id: item.id || Date.now() + Math.random(),
        title: item.title || item.name || '未命名活动',
        name: item.title || item.name || '未命名活动',
        type: 1,
        duration: 30,
        warmUpDuration: 0,
        coolDownDuration: 0,
        exercises: [
          ...(item.warmup || []).map(s => ({ name: s.name, type: 1, duration: s.duration || 60 })),
          ...(item.main || []).map(s => ({
            name: s.name,
            type: s.type === 'reps' ? 2 : 1,
            duration: s.duration || null,
            sets: s.sets || null,
            reps: s.reps || null
          })),
          ...(item.cooldown || []).map(s => ({ name: s.name, type: 3, duration: s.duration || 60 }))
        ].filter(e => e.name)
      }))
    }
  } catch (e) {
    myActivities.value = []
  }
}
function addMyActivity(act) {
  const course = {
    week: currentWeek.value,
    day: editingDay.value,
    name: act.name,
    type: act.type,
    duration: act.duration,
    warmUpDuration: act.warmUpDuration || 0,
    coolDownDuration: act.coolDownDuration || 0,
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
function getTypeIcon(type) {
  return { 1: 'run', 2: 'strength', 3: 'stretch', 4: 'hiit', 5: 'hiit', 6: 'rest' }[type] || 'run'
}
function selectDesignCourse(idx) {
  selectedDesignIdx.value = selectedDesignIdx.value === idx ? -1 : idx
  designerTab.value = 'preset'
}
function removeDesignerStep(ei) {
  const course = selectedDesignCourse.value
  if (course && course.exercises) {
    course.exercises.splice(ei, 1)
  }
}
function addPresetStep(preset) {
  const course = selectedDesignCourse.value
  if (!course) return
  if (!course.exercises) course.exercises = []
  course.exercises.push({
    name: preset.name,
    type: preset.type,
    duration: preset.duration || null,
    distance: preset.distance || null,
    sets: preset.sets || null,
    reps: preset.reps || null,
    restTime: null
  })
}
function addDesignerStep() {
  if (!designerStep.name.trim()) { uni.showToast({ title: '请输入步骤名称', icon: 'none' }); return }
  const course = selectedDesignCourse.value
  if (!course) return
  if (!course.exercises) course.exercises = []
  course.exercises.push({
    name: designerStep.name.trim(),
    type: designerStep.type,
    duration: designerStep.duration || null,
    distance: designerStep.distance || null,
    sets: designerStep.sets || null,
    reps: designerStep.reps || null,
    restTime: designerStep.rest || null
  })
  designerStep.name = ''
  designerStep.duration = null
  designerStep.distance = null
  designerStep.sets = null
  designerStep.reps = null
  designerStep.rest = null
}
function onDesignerTypeChange(e) {
  designerStepTypeIndex.value = e.detail.value
  designerStep.type = e.detail.value + 1
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
    if (selectedDesignIdx.value === idx) selectedDesignIdx.value = -1
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
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
function getTypeIconName(type) {
  return ''
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
    startDate: planData.startDate || formatDate(new Date()),
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
        const savedId = planData.id || res.data?.id || Date.now()
        const localData = { ...data, id: savedId }
        updateLocalPlans(localData)
        uni.removeStorageSync('weeklyPlan_current')
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1200)
      } else {
        uni.showToast({ title: res.message || '保存失败', icon: 'none' })
      }
    })
    .catch(() => {
      saveLocalPlan(data)
    })
    .finally(() => {
      saving.value = false
    })
}
function updateLocalPlans(plan) {
  const local = uni.getStorageSync('myTrainingPlans') || '[]'
  let plans = []
  try { plans = JSON.parse(local) } catch (e) { plans = [] }
  const idx = plans.findIndex(p => String(p.id) === String(plan.id))
  if (idx > -1) {
    plans[idx] = { ...plan }
  } else {
    plans.unshift(plan)
  }
  uni.setStorageSync('myTrainingPlans', JSON.stringify(plans))
}
function saveLocalPlan(data) {
  const localPlans = uni.getStorageSync('myTrainingPlans') || '[]'
  let plans = []
  try { plans = JSON.parse(localPlans) } catch (e) { plans = [] }
  const planToSave = { ...data, startDate: planData.startDate || formatDate(new Date()) }
  if (planData.id) {
    const idx = plans.findIndex(p => String(p.id) === String(planData.id))
    if (idx > -1) {
      plans[idx] = { ...planToSave, id: planData.id }
    } else {
      plans.unshift({ ...planToSave, id: planData.id })
    }
  } else {
    planData.id = Date.now()
    plans.unshift({ ...planToSave, id: planData.id })
  }
  uni.setStorageSync('myTrainingPlans', JSON.stringify(plans))
  uni.showToast({ title: '已保存(本地)', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1200)
}
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  transition: background 0.3s;
}
// 步骤条
.steps-bar {
  display: flex;
  padding: 24rpx 32rpx;
  background: var(--bg-card);
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
  background: var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: var(--text-tertiary);
  font-weight: 600;
  flex-shrink: 0;
  margin-right: 8rpx;
}
.step.active .step-dot {
  background: var(--accent-green);
  color: #fff;
}
.step.current .step-label {
  color: #16a34a;
  font-weight: 700;
}
.step-label {
  font-size: 22rpx;
  color: var(--text-tertiary);
  white-space: nowrap;
}
.step-line {
  position: absolute;
  left: 42rpx;
  top: 50%;
  width: calc(100% - 52rpx);
  height: 3rpx;
  background: var(--border-color);
  transform: translateY(-50%);
  z-index: -1;
}
.step-line.filled {
  background: var(--accent-green);
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
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx;
}
.form-group {
  margin-bottom: 28rpx;
}
.form-label {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  display: block;
  margin-bottom: 14rpx;
}
.form-input {
  height: 80rpx;
  border-radius: 12rpx;
  padding: 0 20rpx;
  background: var(--bg-secondary);
  font-size: 28rpx;
  color: var(--text-primary);
  border: 2rpx solid var(--border-color);
}
.form-textarea {
  min-height: 140rpx;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  background: var(--bg-secondary);
  font-size: 28rpx;
  color: var(--text-primary);
  border: 2rpx solid var(--border-color);
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
  color: var(--text-tertiary);
  background: var(--bg-secondary);
  border: 2rpx solid var(--border-color);
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
  width: 62rpx;
  height: 62rpx;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-green);
}
.ws-btn.disabled {
  opacity: 0.3;
}
.ws-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
}
.week-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  margin-bottom: 16rpx;
}
.week-status {
  font-size: 24rpx;
  color: var(--text-tertiary);
}
.week-copy-btns {
  display: flex;
  gap: 12rpx;
}
.wcp-btn {
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #3b82f6;
  background: #eff6ff;
  border: 1rpx solid #bfdbfe;
  &.primary {
    background: #3b82f6;
    color: #fff;
    border-color: #3b82f6;
  }
  &:active {
    opacity: 0.85;
  }
}
// Day Grid
.day-grid-scroll {
  flex: 1;
  height: 0; /* 微信小程序需要显式设置高度 */
  padding: 0 24rpx;
}
.day-grid {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  padding-bottom: 20rpx;
}
.day-card {
  background: var(--bg-card);
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
  color: var(--text-primary);
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
  color: var(--text-tertiary);
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
  max-height: 85vh;
  background: var(--bg-card);
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
  color: var(--text-primary);
}
.modal-close {
  font-size: 32rpx;
  color: var(--text-tertiary);
  padding: 8rpx;
}
.modal-body {
  flex: 1;
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  padding: 0 28rpx;
  max-height: 68vh;
  min-height: 420rpx;
  overflow: hidden;
}
.modal-foot {
    padding: 20rpx 28rpx calc(20rpx + constant(safe-area-inset-bottom));
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
  background: var(--border-color);
  color: var(--text-tertiary);
}
.section-label {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-tertiary);
  padding: 16rpx 0 12rpx;
}
// Course Items in Modal
.course-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx;
  background: var(--bg-secondary);
  border-radius: 12rpx;
  margin-bottom: 10rpx;
}
.ci-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
  flex: 1;
}
.ci-icon {
  font-size: 32rpx;
}
.ci-info {
  flex: 1;
  min-width: 0;
}
.ci-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  display: block;
}
.ci-meta {
  font-size: 22rpx;
  color: var(--text-tertiary);
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
// Selected courses bar
.selected-bar {
  margin: 0 28rpx 16rpx;
}
.selected-scroll {
  white-space: nowrap;
}
.selected-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #f0fdf4;
  border: 2rpx solid #bbf7d0;
  border-radius: 24rpx;
  margin-right: 10rpx;
}
.sc-type-icon { font-size: 24rpx; }
.sc-name {
  font-size: 22rpx;
  color: var(--accent-green);
  font-weight: 500;
}
.sc-del {
  font-size: 20rpx;
  color: var(--accent-green);
  padding: 4rpx;
}
// Left filter sidebar
.add-filter-side {
  width: 136rpx;
  flex-shrink: 0;
  background: var(--bg-card);
  border-radius: 8px;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  max-height: 480rpx;
  overflow-y: auto;
  &::-webkit-scrollbar { display: none; }
}
.add-filter-item {
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
    background: var(--border-color);
  }
  &.active {
    background: #f0fdf4;
    .afi-text { color: #16a34a; font-weight: 600; }
  }
}
.afi-text {
  font-size: 22rpx;
  color: var(--text-tertiary);
  font-weight: 500;
}
// Right content area
.add-filter-content {
  flex: 1;
  background: var(--bg-card);
  border-radius: 8px;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.content-title {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 14rpx;
}
.content-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.content-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14rpx 18rpx;
  border-radius: 12rpx;
  background: var(--bg-secondary);
  border: 2rpx solid var(--border-color);
  min-width: 140rpx;
  transition: all 0.2s;
  &:active {
    background: #f0fdf4;
    border-color: #86efac;
  }
}
.cc-name {
  font-size: 24rpx;
  color: var(--text-secondary);
  font-weight: 500;
}
.cc-meta {
  font-size: 20rpx;
  color: var(--text-tertiary);
  margin-top: 4rpx;
}
.content-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 20rpx;
  color: var(--text-tertiary);
  font-size: 24rpx;
}
.ce-hint {
  font-size: 22rpx;
  color: #cbd5e1;
  margin-top: 8rpx;
}
// Custom Form
.custom-form {
  background: var(--bg-secondary);
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
  color: var(--text-primary);
  border: 2rpx solid var(--border-color);
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
  border: 2rpx solid var(--border-color);
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #475569;
}
.cf-btn {
  height: 64rpx;
  border-radius: 10rpx;
  background: var(--accent-green);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}
// Step 3 Activity List
.activity-list {
  background: var(--bg-card);
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
.al-icon {
  font-size: 36rpx;
}
.al-info {
  flex: 1;
  min-width: 0;
}
.al-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  display: block;
}
.al-meta {
  font-size: 22rpx;
  color: var(--text-tertiary);
}
.al-toggle {
  width: 46rpx;
  height: 46rpx;
  color: var(--text-secondary);
  flex-shrink: 0;
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
  color: var(--text-secondary);
  display: block;
}
.ex-meta {
  font-size: 22rpx;
  color: var(--text-tertiary);
}
.ex-del {
  font-size: 24rpx;
  color: #ef4444;
  padding: 8rpx;
}
.add-step-form {
  background: var(--bg-secondary);
  border-radius: 12rpx;
  padding: 16rpx;
}
.asf-label {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-tertiary);
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
  color: var(--text-primary);
  border: 2rpx solid var(--border-color);
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
  border: 2rpx solid var(--border-color);
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
  background: var(--accent-green);
  color: #fff;
}
.asf-btn.repeat {
  background: #dbeafe;
  color: #2563eb;
}
// ===== 小项目设计器 =====
.designer-panel {
  margin: 0 28rpx 16rpx;
  background: var(--bg-secondary);
  border-radius: 14rpx;
  padding: 20rpx;
  max-height: 420rpx;
  overflow-y: auto;
}
.dp-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14rpx;
}
.dp-title { font-size: 24rpx; font-weight: 700; color: var(--text-primary); }
.dp-close {
  display: flex;
  align-items: center;
  gap: 6rpx;
  height: 44rpx;
  padding: 0;
  background: transparent;
  color: var(--text-secondary);
  font-size: 22rpx;
  font-weight: 700;
}
.dp-steps {
  display: flex; flex-direction: column; gap: 8rpx;
  margin-bottom: 16rpx;
  padding: 12rpx;
  background: var(--bg-card);
  border-radius: 10rpx;
}
.dp-step {
  display: flex; align-items: center; gap: 10rpx;
  padding: 10rpx 12rpx;
  border-bottom: 1rpx solid var(--border-color);
  &:last-child { border-bottom: none; }
}
.dps-icon { font-size: 28rpx; width: 36rpx; text-align: center; }
.dps-info { flex: 1; }
.dps-name { font-size: 26rpx; font-weight: 600; color: var(--text-primary); display: block; }
.dps-meta { font-size: 20rpx; color: var(--text-tertiary); margin-right: 10rpx; }
.dps-del { font-size: 22rpx; color: #ef4444; padding: 6rpx; }
.dp-tabs {
  display: flex; gap: 10rpx; margin-bottom: 12rpx;
}
.dp-tab {
  flex: 1; text-align: center; padding: 12rpx; border-radius: 8rpx;
  font-size: 24rpx; font-weight: 500;
  color: var(--text-tertiary); background: var(--bg-card);
  border: 1rpx solid var(--border-color);
  &.active { color: var(--accent-green); background: var(--bg-secondary); border-color: var(--accent-green); }
}
.dp-preset-list {
  display: flex; flex-wrap: wrap; gap: 8rpx;
}
.dp-preset-item {
  display: flex; align-items: center; gap: 6rpx;
  padding: 10rpx 16rpx; border-radius: 8rpx;
  background: var(--bg-card); border: 1rpx solid var(--border-color);
  &:active { background: var(--bg-secondary); }
}
.dpp-icon { font-size: 24rpx; }
.dpp-name { font-size: 24rpx; color: var(--text-primary); font-weight: 500; }
.dpp-meta { font-size: 18rpx; color: var(--text-tertiary); }
.dp-custom-form {
  background: var(--bg-card); border-radius: 10rpx; padding: 16rpx;
}
.dp-cf-row {
  display: flex; gap: 10rpx; margin-bottom: 10rpx;
}
.dp-cf-input {
  flex: 1; height: 56rpx; border-radius: 8rpx; padding: 0 12rpx;
  background: var(--bg-secondary); font-size: 24rpx; color: var(--text-primary);
  border: 1rpx solid var(--border-color);
  &.sm { flex: 1; }
}
.dp-cf-picker {
  flex: 1; height: 56rpx; border-radius: 8rpx; padding: 0 12rpx;
  background: var(--bg-secondary); border: 1rpx solid var(--border-color);
  display: flex; align-items: center; font-size: 24rpx; color: var(--text-primary);
}
.dp-cf-btn {
  height: 56rpx; border-radius: 8rpx; background: var(--accent-green); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 24rpx; font-weight: 600;
  &:active { opacity: 0.85; }
}
.chip-active {
  background: var(--accent-green) !important;
  border-color: var(--accent-green) !important;
  .sc-name { color: #fff !important; }
  .sc-del { color: #fff !important; }
}
// Footer
.step-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 24rpx calc(20rpx + constant(safe-area-inset-bottom));
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
  background: var(--border-color);
  color: var(--text-tertiary);
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
  color: var(--text-secondary);
  margin-top: 16rpx;
}
.empty-sub {
  font-size: 26rpx;
  color: var(--text-tertiary);
  margin-top: 8rpx;
}
</style>
