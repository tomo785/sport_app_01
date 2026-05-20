<template>
  <view class="editor-page">
    <!-- ===== 自定义导航栏 ===== -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-body">
        <text class="nav-cancel" @click="handleCancel">取消</text>
        <text class="nav-title">创建活动</text>
        <text class="nav-save" @click="handleSave">保存</text>
      </view>
    </view>

    <!-- ===== 活动标题与类型 ===== -->
    <view class="title-section">
      <view class="title-row">
        <view class="title-ring"></view>
        <input
          class="title-input"
          v-model="activityTitle"
          placeholder="输入活动名称"
          placeholder-style="color: #94a3b8"
          maxlength="30"
        />
        <view class="type-selector">
          <picker :range="typeOptions" :value="activityTypeIndex" @change="onTypeChange">
            <view class="ts-btn">
              <text class="ts-label">{{ typeOptions[activityTypeIndex].label }}</text>
              <text class="ts-arrow">▼</text>
            </view>
          </picker>
        </view>
      </view>
    </view>

    <!-- ===== 活动结构 ===== -->
    <scroll-view class="editor-body" scroll-y>
      <!-- 热身 -->
      <view class="phase-card">
        <view class="phase-head">
          <view class="phase-badge warm-badge">
            <text class="badge-icon"></text>
            <text class="badge-text">热身</text>
          </view>
          <view class="phase-add" @click="addPhaseItem('warmup')">
            <text class="phase-add-icon">＋</text>
          </view>
        </view>
        <view class="phase-body">
          <view v-if="warmupSteps.length === 0" class="phase-empty">
            <text class="phase-empty-text">添加热身步骤</text>
          </view>
          <view class="phase-item" v-for="(step, idx) in warmupSteps" :key="idx">
            <view class="pi-left">
              <view class="pi-drag">⠿</view>
              <input
                class="pi-name-input"
                v-model="step.name"
                placeholder="动作名称"
                placeholder-style="color: #94a3b8"
              />
            </view>
            <view class="pi-right">
              <view class="pi-duration">
                <input
                  class="pi-dur-input"
                  v-model.number="step.duration"
                  type="number"
                  placeholder="秒"
                  placeholder-style="color: #94a3b8"
                />
                <text class="pi-unit">s</text>
              </view>
              <view class="pi-del" @click="removePhaseItem('warmup', idx)">✕</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 主项 -->
      <view class="phase-card">
        <view class="phase-head">
          <view class="phase-badge main-badge">
            <text class="badge-icon"></text>
            <text class="badge-text">主项</text>
          </view>
          <view class="phase-add" @click="addPhaseItem('main')">
            <text class="phase-add-icon">＋</text>
          </view>
        </view>
        <view class="phase-body">
          <view v-if="mainSteps.length === 0" class="phase-empty">
            <text class="phase-empty-text">添加主项内容</text>
          </view>
          <!-- 主项步骤 -->
          <view class="phase-item" v-for="(step, idx) in mainSteps" :key="'m'+idx">
            <view class="pi-left">
              <view class="pi-drag">⠿</view>
              <input
                class="pi-name-input"
                v-model="step.name"
                placeholder="步骤名称"
                placeholder-style="color: #94a3b8"
              />
            </view>
            <view class="pi-right">
              <view class="pi-duration" v-if="step.type === 'duration'">
                <input class="pi-dur-input" v-model.number="step.duration" type="number" placeholder="秒" placeholder-style="color: #94a3b8" />
                <text class="pi-unit">s</text>
              </view>
              <view class="pi-sets" v-if="step.type === 'reps'">
                <input class="pi-dur-input sm" v-model.number="step.sets" type="number" placeholder="组" placeholder-style="color: #94a3b8" />
                <text class="pi-sep">×</text>
                <input class="pi-dur-input sm" v-model.number="step.reps" type="number" placeholder="次" placeholder-style="color: #94a3b8" />
              </view>
              <view class="pi-type-toggle" @click="toggleMainType(idx)">
                <text>{{ step.type === 'duration' ? '时' : '组' }}</text>
              </view>
              <view class="pi-del" @click="removePhaseItem('main', idx)">✕</view>
            </view>
          </view>
          <!-- 主项重复组 -->
          <view class="repeat-group" v-for="(group, gi) in repeatGroups" :key="'rg'+gi">
            <view class="rg-head">
              <text class="rg-label">重复组 {{ gi + 1 }}</text>
              <view class="rg-count">
                <input class="rg-count-input" v-model.number="group.repeat" type="number" placeholder="次数" placeholder-style="color: #94a3b8" />
                <text class="rg-unit">次</text>
              </view>
              <view class="rg-del" @click="removeRepeatGroup(gi)">✕</view>
            </view>
            <view class="phase-item sub-item" v-for="(step, si) in group.steps" :key="'gs'+si">
              <view class="pi-left">
                <text class="pi-index">{{ si + 1 }}</text>
                <input class="pi-name-input" v-model="step.name" placeholder="动作" placeholder-style="color: #94a3b8" />
              </view>
              <view class="pi-right">
                <view class="pi-sets">
                  <input class="pi-dur-input sm" v-model.number="step.sets" type="number" placeholder="组" placeholder-style="color: #94a3b8" />
                  <text class="pi-sep">×</text>
                  <input class="pi-dur-input sm" v-model.number="step.reps" type="number" placeholder="次" placeholder-style="color: #94a3b8" />
                </view>
                <view class="pi-del" @click="removeRepeatStep(gi, si)">✕</view>
              </view>
            </view>
            <view class="rg-add-btn" @click="addRepeatStep(gi)">
              <text>＋ 添加动作</text>
            </view>
          </view>
        </view>
      </view>

      <view class="bottom-filler"></view>
    </scroll-view>

    <!-- ===== 底部功能栏 ===== -->
    <view class="bottom-bar">
      <view class="bb-btn" @click="addPhaseItem(currentPhase)">
        <text class="bb-btn-icon">＋</text>
        <text class="bb-btn-text">添加步骤</text>
      </view>
      <view class="bb-btn" @click="addRepeatGroup">
        <text class="bb-btn-icon">↻</text>
        <text class="bb-btn-text">添加重复</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const statusBarHeight = ref(20)
const activityTitle = ref('')
const currentPhase = ref('warmup')
const activityTypeIndex = ref(0)

const typeOptions = [
  { label: '跑步', catKey: 'run' },
  { label: '骑行', catKey: 'bike' },
  { label: '户外', catKey: 'outdoor' },
  { label: '水中', catKey: 'swim' },
]

const warmupSteps = ref([])
const mainSteps = ref([])
const repeatGroups = ref([])

function onTypeChange(e) {
  activityTypeIndex.value = e.detail.value
}

onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
})

function addPhaseItem(phase) {
  currentPhase.value = phase
  const newItem = { name: '', duration: 30, type: 'duration', sets: 3, reps: 12 }

  if (phase === 'warmup') {
    warmupSteps.value.push({ ...newItem })
  } else if (phase === 'main') {
    mainSteps.value.push({ ...newItem, type: 'duration' })
  }
}

function removePhaseItem(phase, idx) {
  if (phase === 'warmup') warmupSteps.value.splice(idx, 1)
  else if (phase === 'main') mainSteps.value.splice(idx, 1)
}

function toggleMainType(idx) {
  const step = mainSteps.value[idx]
  if (step) {
    step.type = step.type === 'duration' ? 'reps' : 'duration'
  }
}

function addRepeatGroup() {
  currentPhase.value = 'main'
  repeatGroups.value.push({
    repeat: 1,
    steps: [{ name: '', sets: 3, reps: 12 }]
  })
}

function removeRepeatGroup(gi) {
  repeatGroups.value.splice(gi, 1)
}

function addRepeatStep(gi) {
  repeatGroups.value[gi].steps.push({ name: '', sets: 3, reps: 12 })
}

function removeRepeatStep(gi, si) {
  repeatGroups.value[gi].steps.splice(si, 1)
}

function buildActivityData() {
  return {
    title: activityTitle.value,
    type: typeOptions[activityTypeIndex.value].catKey,
    warmup: warmupSteps.value.map(s => ({ name: s.name, duration: s.duration })),
    main: mainSteps.value.map(s => ({
      name: s.name,
      type: s.type,
      duration: s.duration || null,
      sets: s.sets || null,
      reps: s.reps || null
    })),
    repeats: repeatGroups.value.map(g => ({
      repeat: g.repeat,
      steps: g.steps.map(s => ({ name: s.name, sets: s.sets, reps: s.reps }))
    }))
  }
}

function handleCancel() {
  const hasContent = warmupSteps.value.length > 0 || mainSteps.value.length > 0
    || repeatGroups.value.length > 0 || activityTitle.value

  if (hasContent) {
    uni.showModal({
      title: '放弃编辑',
      content: '当前内容将不会保存，确定退出？',
      success: res => {
        if (res.confirm) uni.navigateBack()
      }
    })
  } else {
    uni.navigateBack()
  }
}

function handleSave() {
  if (!activityTitle.value.trim()) {
    uni.showToast({ title: '请输入活动名称', icon: 'none' })
    return
  }

  const data = buildActivityData()
  const events = uni.getStorageSync('userActivities') || []

  try {
    const list = typeof events === 'string' ? JSON.parse(events) : events
    list.push({
      id: Date.now(),
      ...data,
      createdAt: new Date().toISOString()
    })
    uni.setStorageSync('userActivities', JSON.stringify(list))
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.editor-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

// ===== 导航栏 =====
.nav-bar {
  background: #fff;
  border-bottom: 1rpx solid #e2e8f0;
  padding-bottom: 12rpx;
}

.nav-body {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.nav-cancel {
  font-size: 30rpx;
  color: #64748b;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 1rpx;
}

.nav-save {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  padding: 10rpx 28rpx;
  background: #22c55e;
  border-radius: 20rpx;
}

// ===== 标题区 =====
.title-section {
  padding: 28rpx 32rpx 20rpx;
  background: #fff;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.title-ring {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #22c55e;
  flex-shrink: 0;
}

.title-input {
  flex: 1;
  height: 72rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: -1rpx;

  &::placeholder {
    font-weight: 400;
    color: #94a3b8;
  }
}

// ===== 类型选择器 =====
.type-selector {
  flex-shrink: 0;
}

.ts-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 12rpx 18rpx;
  background: #f0fdf4;
  border-radius: 16rpx;
  border: 1rpx solid #bbf7d0;
}

.ts-label {
  font-size: 30rpx;
  font-weight: 600;
  color: #16a34a;
}

.ts-arrow {
  font-size: 18rpx;
  color: #16a34a;
  margin-top: 2rpx;
}

// ===== 编辑主体 =====
.editor-body {
  flex: 1;
  height: 0;
  padding: 0 24rpx;
  padding-top: 24rpx;
}

// ===== 阶段卡片 =====
.phase-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 14rpx rgba(0, 0, 0, 0.04);
}

.phase-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.phase-badge {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
}

.badge-icon {
  font-size: 22rpx;
}

.badge-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #1e293b;
}

.warm-badge {
  background: #fffbeb;
  border: 1rpx solid #fde68a;
}

.main-badge {
  background: #f0fdf4;
  border: 1rpx solid #bbf7d0;
}

.phase-add {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: #f1f5f9;
  border: 1rpx solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phase-add:active {
  background: #e2e8f0;
}

.phase-add-icon {
  font-size: 28rpx;
  color: #64748b;
}

.phase-body {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.phase-empty {
  padding: 32rpx 0;
  text-align: center;
}

.phase-empty-text {
  font-size: 26rpx;
  color: #94a3b8;
}

// 阶段步骤行
.phase-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 20rpx;
  background: #f8fafc;
  border-radius: 14rpx;
  border: 1rpx solid #e2e8f0;
  gap: 12rpx;
}

.pi-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.pi-drag {
  font-size: 28rpx;
  color: #94a3b8;
  flex-shrink: 0;
}

.pi-index {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pi-name-input {
  flex: 1;
  height: 52rpx;
  font-size: 28rpx;
  color: #1e293b;
  min-width: 0;
}

.pi-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.pi-duration,
.pi-sets {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 16rpx;
  background: #fff;
  border-radius: 10rpx;
  border: 1rpx solid #e2e8f0;
}

.pi-dur-input {
  width: 68rpx;
  height: 44rpx;
  font-size: 26rpx;
  color: #1e293b;
  text-align: center;
}

.pi-dur-input.sm {
  width: 48rpx;
}

.pi-unit {
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 500;
}

.pi-sep {
  font-size: 22rpx;
  color: #94a3b8;
}

.pi-type-toggle {
  width: 48rpx;
  height: 48rpx;
  border-radius: 10rpx;
  background: #f0fdf4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

.pi-del {
  font-size: 24rpx;
  color: #94a3b8;
  padding: 8rpx;
}

.pi-del:active {
  color: #ef4444;
}

// 重复组
.repeat-group {
  margin-top: 8rpx;
  padding: 18rpx;
  background: #f0fdf4;
  border-radius: 16rpx;
  border: 1rpx solid #bbf7d0;
}

.rg-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14rpx;
  padding: 0 4rpx;
}

.rg-label {
  font-size: 24rpx;
  color: #16a34a;
  font-weight: 600;
}

.rg-count {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.rg-count-input {
  width: 60rpx;
  height: 48rpx;
  background: #fff;
  border-radius: 8rpx;
  border: 1rpx solid #e2e8f0;
  font-size: 24rpx;
  color: #1e293b;
  text-align: center;
}

.rg-unit {
  font-size: 22rpx;
  color: #94a3b8;
}

.rg-del {
  font-size: 24rpx;
  color: #94a3b8;
  padding: 8rpx;
}

.rg-del:active {
  color: #ef4444;
}

.sub-item {
  margin-bottom: 8rpx;
  background: #f8fafc;
}

.rg-add-btn {
  text-align: center;
  padding: 16rpx 0 4rpx;
  font-size: 24rpx;
  color: #16a34a;
  font-weight: 500;
}

.rg-add-btn:active {
  opacity: 0.7;
}

// ===== 底部操作栏 =====
.bottom-bar {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + constant(safe-area-inset-bottom));
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #e2e8f0;
}

.bb-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  background: #f1f5f9;
  border: 1rpx solid #e2e8f0;
}

.bb-btn:active {
  background: #e2e8f0;
}

.bb-btn-icon {
  font-size: 28rpx;
  color: #22c55e;
  font-weight: 700;
}

.bb-btn-text {
  font-size: 28rpx;
  color: #1e293b;
  font-weight: 600;
}

// ===== 底部留白 =====
.bottom-filler {
  height: 40rpx;
}
</style>
