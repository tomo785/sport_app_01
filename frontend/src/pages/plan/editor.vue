<template>
  <view class="editor-page">
    <view class="hero">
      <text class="hero-title">创建本周计划</text>
      <text class="hero-sub">点击对应日期卡片，配置每天训练内容</text>
    </view>

    <view class="week-grid">
      <view
        v-for="day in weekDays"
        :key="day.dayOfWeek"
        class="day-card"
        :class="`type-${day.plan?.type || 'empty'}`"
        @click="openDayEditor(day)"
      >
        <text class="day-label">{{ day.label }}</text>
        <image class="day-icon-img" :src="getTypeIcon(day.plan?.type)" mode="aspectFit" />
        <text class="day-title">{{ day.plan?.title || '点击添加' }}</text>
        <text class="day-meta">{{ getMeta(day.plan) }}</text>
      </view>
    </view>

    <view class="list-card" v-if="weeklyPlan.days.length > 0">
      <text class="list-title">本周已创建</text>
      <view class="plan-row" v-for="item in sortedPlans" :key="item.id">
        <text class="row-day">{{ weekLabels[item.dayOfWeek - 1] }}</text>
        <text class="row-name">{{ item.title }}</text>
        <text class="row-remove" @click="removePlan(item.dayOfWeek)">删除</text>
      </view>
    </view>

    <view class="publish-card">
      <text class="publish-title">共享到发现计划</text>
      <input class="publish-input" v-model="publishForm.name" placeholder="计划名称（必填）" />
      <textarea class="publish-textarea" v-model="publishForm.description" placeholder="计划说明（选填）" />
      <button class="publish-btn" @click="publishPlan">上传共享</button>
    </view>

    <view class="footer-actions">
      <button class="footer-btn clear" @click="clearWeek">清空本周</button>
      <button class="footer-btn save" @click="saveAndBack">保存并返回</button>
    </view>

    <TypeSelector
      :visible="showTypeSelector"
      :initialData="editingDay?.plan || null"
      :editingDay="editingDay"
      @close="showTypeSelector = false"
      @confirm="onTypeConfirm"
    />
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import TypeSelector from '../../components/plan/TypeSelector.vue'

const STORAGE_KEY = 'weeklyPlan_current'
const COMMUNITY_KEY = 'communityTemplates'

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const showTypeSelector = ref(false)
const editingDay = ref(null)

const weeklyPlan = ref({
  id: '',
  name: '本周训练计划',
  weekStartDate: '',
  days: [],
  isFromTemplate: false
})

const publishForm = ref({
  name: '',
  description: ''
})

const weekDays = computed(() => {
  return weekLabels.map((label, index) => {
    const dayOfWeek = index + 1
    const plan = weeklyPlan.value.days
      .filter(item => item.dayOfWeek === dayOfWeek)
      .sort((a, b) => (a.order || 0) - (b.order || 0))[0]
    return { label, dayOfWeek, plan }
  })
})

const sortedPlans = computed(() => {
  return [...weeklyPlan.value.days].sort((a, b) => a.dayOfWeek - b.dayOfWeek)
})

onMounted(() => {
  loadPlan()
})

function loadPlan() {
  const saved = uni.getStorageSync(STORAGE_KEY)
  if (!saved) {
    initDefaultPlan()
    return
  }
  try {
    weeklyPlan.value = JSON.parse(saved)
  } catch (error) {
    initDefaultPlan()
  }
}

function initDefaultPlan() {
  weeklyPlan.value = {
    id: `wp_${Date.now()}`,
    name: '本周训练计划',
    weekStartDate: getWeekStart(new Date()),
    days: [],
    isFromTemplate: false
  }
}

function getWeekStart(date) {
  const temp = new Date(date)
  const day = temp.getDay() || 7
  temp.setDate(temp.getDate() - day + 1)
  return formatDate(temp)
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function openDayEditor(day) {
  editingDay.value = day
  showTypeSelector.value = true
}

function onTypeConfirm(data) {
  if (!editingDay.value) return

  const dayOfWeek = editingDay.value.dayOfWeek
  weeklyPlan.value.days = weeklyPlan.value.days.filter(item => item.dayOfWeek !== dayOfWeek)
  weeklyPlan.value.days.push({
    id: `dp_${Date.now()}_${dayOfWeek}`,
    dayOfWeek,
    order: 1,
    isCompleted: false,
    personalNote: '',
    ...data
  })

  showTypeSelector.value = false
  editingDay.value = null
}

function removePlan(dayOfWeek) {
  weeklyPlan.value.days = weeklyPlan.value.days.filter(item => item.dayOfWeek !== dayOfWeek)
}

function clearWeek() {
  uni.showModal({
    title: '清空本周',
    content: '确定清空全部训练安排吗？',
    success: res => {
      if (!res.confirm) return
      weeklyPlan.value.days = []
      uni.showToast({ title: '已清空', icon: 'none' })
    }
  })
}

function saveAndBack() {
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(weeklyPlan.value))
  uni.showToast({
    title: '计划已保存',
    icon: 'success'
  })
  setTimeout(() => {
    uni.navigateBack()
  }, 300)
}

function publishPlan() {
  if (!publishForm.value.name.trim()) {
    uni.showToast({ title: '请输入计划名称', icon: 'none' })
    return
  }
  if (weeklyPlan.value.days.length < 2) {
    uni.showToast({ title: '至少创建2天训练内容', icon: 'none' })
    return
  }

  const list = JSON.parse(uni.getStorageSync(COMMUNITY_KEY) || '[]')
  list.push({
    id: `ct_${Date.now()}`,
    name: publishForm.value.name.trim(),
    authorName: '我',
    tags: ['自定义'],
    targetAudience: '通用',
    description: publishForm.value.description || '用户上传的周计划',
    weeklyStructure: [...weeklyPlan.value.days],
    usageCount: 0,
    rating: 5,
    createdAt: new Date().toISOString()
  })
  uni.setStorageSync(COMMUNITY_KEY, JSON.stringify(list))
  uni.showToast({ title: '已上传到发现计划', icon: 'success' })
  publishForm.value = { name: '', description: '' }
}

function getTypeIcon(type) {
  const map = {
    run: '/static/images/plan/run.png',
    strength: '/static/images/plan/strength.png',
    yoga: '/static/images/plan/yoga.png',
    rest: '/static/images/plan/rest.png',
    custom: '/static/images/plan/custom.png'
  }
  return map[type] || '/static/images/plan/add.png'
}

function getMeta(plan) {
  if (!plan) return '未安排'
  if (plan.type === 'rest') return '休息/恢复'
  if (plan.details?.duration) return `${plan.details.duration} 分钟`
  return '已安排'
}
</script>

<style lang="scss" scoped>
.editor-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef2ff 0%, #f8fafc 42%, #f8fafc 100%);
  padding: 24rpx;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.hero {
  border-radius: 26rpx;
  padding: 30rpx;
  background: linear-gradient(140deg, #0f172a 0%, #1e3a8a 52%, #312e81 100%);
  box-shadow: 0 16rpx 34rpx rgba(15, 23, 42, 0.28);
  margin-bottom: 20rpx;
}

.hero-title {
  display: block;
  color: #ffffff;
  font-size: 38rpx;
  font-weight: 800;
}

.hero-sub {
  margin-top: 8rpx;
  display: block;
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
}

.week-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-bottom: 20rpx;
}

.day-card {
  border-radius: 18rpx;
  padding: 18rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.08);
}

.day-card.type-run { border-left: 8rpx solid #2563eb; }
.day-card.type-strength { border-left: 8rpx solid #ea580c; }
.day-card.type-yoga { border-left: 8rpx solid #0ea5a4; }
.day-card.type-rest { border-left: 8rpx solid #64748b; }
.day-card.type-custom { border-left: 8rpx solid #7c3aed; }
.day-card.type-empty { border-left: 8rpx solid #cbd5e1; }

.day-label {
  font-size: 22rpx;
  color: #64748b;
}

.day-icon-img {
  margin-top: 8rpx;
  display: block;
  width: 48rpx;
  height: 48rpx;
}

.day-title {
  margin-top: 10rpx;
  display: block;
  color: #0f172a;
  font-size: 28rpx;
  font-weight: 700;
}

.day-meta {
  margin-top: 6rpx;
  display: block;
  color: #64748b;
  font-size: 22rpx;
}

.list-card,
.publish-card {
  background: #ffffff;
  border-radius: 18rpx;
  box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.08);
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.list-title,
.publish-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 12rpx;
}

.plan-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}

.plan-row:last-child {
  border-bottom: none;
}

.row-day {
  width: 70rpx;
  color: #334155;
  font-size: 24rpx;
}

.row-name {
  flex: 1;
  color: #0f172a;
  font-size: 25rpx;
}

.row-remove {
  color: #dc2626;
  font-size: 23rpx;
}

.publish-input,
.publish-textarea {
  width: 100%;
  box-sizing: border-box;
  border-radius: 12rpx;
  background: #f8fafc;
  font-size: 24rpx;
  color: #0f172a;
}

.publish-input {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 14rpx;
  margin-bottom: 10rpx;
}

.publish-textarea {
  height: 110rpx;
  padding: 14rpx;
}

.publish-btn {
  margin-top: 12rpx;
  width: 100%;
  height: 72rpx;
  border: none;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  color: #ffffff;
  font-size: 25rpx;
  line-height: 72rpx;
}

.footer-actions {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 12rpx;
}

.footer-btn {
  flex: 1;
  height: 78rpx;
  border: none;
  border-radius: 14rpx;
  font-size: 26rpx;
  line-height: 78rpx;
  font-weight: 700;
}

.footer-btn.clear {
  background: #e2e8f0;
  color: #334155;
}

.footer-btn.save {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #ffffff;
}
</style>
