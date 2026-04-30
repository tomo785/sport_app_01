<template>
  <view
    class="plan-card-wrapper"
    :class="{
      dragging: isDragging,
      expanded: isExpanded
    }"
    :style="dragStyle"
    @click="handleCardClick"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
    @touchcancel="handleTouchEnd"
  >
    <view class="plan-card">
      <view class="type-bar" :class="'type-' + (dayPlan?.type || 'rest')"></view>

      <view class="card-content">
        <view class="card-main">
          <view class="type-chip">
            <text class="type-icon">{{ getTypeIcon(dayPlan?.type) }}</text>
          </view>

          <view class="card-info">
            <text class="plan-title">
              {{ dayPlan?.title || '今日计划' }}
            </text>
            <text class="plan-meta">{{ getMetaText(dayPlan) }}</text>
            <text class="plan-hint">单击卡片查看详情，点击右箭头开始</text>
          </view>

          <view class="card-actions">
            <view class="expand-btn" @click.stop="toggleExpand">{{ isExpanded ? '˄' : '˅' }}</view>
            <view class="start-btn" @click.stop="startTraining">→</view>
          </view>
        </view>

        <view class="detail-panel" v-if="isExpanded && dayPlan" @click.stop>
          <view class="detail-section">
            <text class="detail-label">训练说明</text>
            <text class="detail-desc">{{ dayPlan.details?.description || '暂无详细说明' }}</text>
          </view>

          <view class="detail-section" v-if="dayPlan.details?.runParams?.pace">
            <text class="detail-label">目标配速</text>
            <text class="detail-value highlight">{{ dayPlan.details.runParams.pace }}</text>
          </view>

          <view class="detail-section" v-if="dayPlan.details?.exercises?.length">
            <text class="detail-label">动作列表</text>
            <view class="exercise-list">
              <view class="exercise-item" v-for="(exercise, idx) in dayPlan.details.exercises" :key="idx">
                <text class="ex-name">{{ exercise.name }}</text>
                <text class="ex-params">{{ exercise.sets }}组 × {{ exercise.reps }}</text>
              </view>
            </view>
          </view>

          <view class="detail-section">
            <text class="detail-label">个人备注</text>
            <textarea
              class="note-input"
              v-model="localNote"
              placeholder="记录今天状态或调整要点..."
              maxlength="200"
              @blur="saveNote"
            />
          </view>

          <view class="detail-actions">
            <button class="action-btn go-btn" @click.stop="startTraining">开始训练</button>
          </view>
        </view>
      </view>
    </view>

    <view class="drag-hint" v-if="isDragging">拖动排序</view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  dayPlan: { type: Object, default: null },
  index: { type: Number, default: 0 },
  isDragging: { type: Boolean, default: false },
  dragOffset: { type: Number, default: 0 }
})

const emit = defineEmits(['expand', 'start', 'note-change', 'drag-start', 'drag-move', 'drag-end'])

const isExpanded = ref(false)
const localNote = ref('')
const longPressTimer = ref(null)
const isLongPress = ref(false)

watch(
  () => props.dayPlan?.personalNote,
  value => {
    localNote.value = value || ''
  },
  { immediate: true }
)

const dragStyle = computed(() => {
  if (!props.isDragging) return {}
  return {
    transform: `translateY(${props.dragOffset}px) scale(1.02)`,
    zIndex: 100,
    opacity: 0.96,
    boxShadow: '0 20rpx 56rpx rgba(15, 23, 42, 0.24)'
  }
})

function handleCardClick() {
  if (isLongPress.value) {
    isLongPress.value = false
    return
  }
  uni.showToast({
    title: '点右侧箭头开始训练',
    icon: 'none'
  })
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
  emit('expand', { index: props.index, expanded: isExpanded.value })
}

function handleTouchStart() {
  if (longPressTimer.value) clearTimeout(longPressTimer.value)
  isLongPress.value = false
  longPressTimer.value = setTimeout(() => {
    isLongPress.value = true
    uni.vibrateShort()
    emit('drag-start', props.index)
  }, 450)
}

function handleTouchMove(event) {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
  if (!props.isDragging) return
  const touch = event.touches?.[0]
  if (!touch) return
  emit('drag-move', { index: props.index, clientY: touch.clientY })
}

function handleTouchEnd() {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
  if (props.isDragging) {
    emit('drag-end', props.index)
  }
}

function startTraining() {
  emit('start', props.dayPlan)
}

function saveNote() {
  emit('note-change', { id: props.dayPlan?.id, note: localNote.value })
}

function getTypeIcon(type) {
  const map = {
    run: '🏃',
    strength: '🏋',
    yoga: '🧘',
    rest: '🛌',
    custom: '📝'
  }
  return map[type] || '📝'
}

function getMetaText(plan) {
  if (!plan) return ''
  if (plan.type === 'rest') return '恢复/休息日'
  if (plan.details?.duration) return `${plan.details.duration} 分钟`
  if (plan.details?.exercises?.length) return `${plan.details.exercises.length} 个动作`
  return '点击右侧箭头开始'
}
</script>

<style lang="scss" scoped>
.plan-card-wrapper {
  margin-bottom: 22rpx;
  transition: all 0.24s ease;
  position: relative;
}

.plan-card {
  display: flex;
  border-radius: 24rpx;
  overflow: hidden;
  background: linear-gradient(145deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10rpx 30rpx rgba(15, 23, 42, 0.08);
}

.type-bar {
  width: 10rpx;
  flex-shrink: 0;
}

.type-bar.type-run { background: linear-gradient(180deg, #2563eb 0%, #38bdf8 100%); }
.type-bar.type-strength { background: linear-gradient(180deg, #ea580c 0%, #f59e0b 100%); }
.type-bar.type-yoga { background: linear-gradient(180deg, #0ea5a4 0%, #14b8a6 100%); }
.type-bar.type-rest { background: linear-gradient(180deg, #64748b 0%, #94a3b8 100%); }
.type-bar.type-custom { background: linear-gradient(180deg, #4f46e5 0%, #7c3aed 100%); }

.card-content {
  flex: 1;
  padding: 26rpx;
}

.card-main {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.type-chip {
  width: 66rpx;
  height: 66rpx;
  border-radius: 18rpx;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.type-icon {
  font-size: 36rpx;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.plan-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.plan-meta {
  font-size: 23rpx;
  color: #64748b;
}

.plan-hint {
  font-size: 21rpx;
  color: #94a3b8;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: center;
}

.expand-btn,
.start-btn {
  width: 54rpx;
  height: 54rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 700;
}

.expand-btn {
  background: #f1f5f9;
  color: #475569;
}

.start-btn {
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  color: #ffffff;
  box-shadow: 0 8rpx 18rpx rgba(37, 99, 235, 0.35);
}

.detail-panel {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #e2e8f0;
}

.detail-section {
  margin-bottom: 18rpx;
}

.detail-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin-bottom: 6rpx;
}

.detail-desc {
  font-size: 25rpx;
  line-height: 1.55;
  color: #334155;
}

.detail-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}

.detail-value.highlight {
  color: #2563eb;
}

.exercise-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.exercise-item {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}

.ex-name {
  font-size: 24rpx;
  color: #0f172a;
}

.ex-params {
  font-size: 22rpx;
  color: #64748b;
}

.note-input {
  width: 100%;
  height: 112rpx;
  padding: 14rpx;
  box-sizing: border-box;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #334155;
}

.detail-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 74rpx;
  border-radius: 14rpx;
  border: none;
  font-size: 24rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.go-btn {
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  color: #ffffff;
}

.drag-hint {
  position: absolute;
  top: -34rpx;
  left: 50%;
  transform: translateX(-50%);
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.85);
  color: #ffffff;
  font-size: 20rpx;
}
</style>
