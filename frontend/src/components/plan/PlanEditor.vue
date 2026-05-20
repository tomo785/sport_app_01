<template>
  <view class="plan-editor-popup" v-if="visible" @click="close">
    <view class="editor-content" @click.stop>
      <view class="editor-header">
        <text class="editor-title">编辑周计划</text>
        <text class="close-btn" @click="close">✕</text>
      </view>
      
      <!-- 7天网格 -->
      <view class="week-grid">
        <view 
          v-for="day in weekDays" 
          :key="day.dayOfWeek"
          class="day-column"
          :class="{ hasPlan: day.plan }"
          @click="editDay(day)"
        >
          <text class="column-week">{{ day.label }}</text>
          <view class="day-plan-card" :class="'type-' + (day.plan?.type || 'empty')">
            <image class="day-plan-icon-img" :src="getTypeIcon(day.plan?.type)" mode="aspectFit" />
            <text class="day-plan-title">{{ day.plan?.title || '点击添加' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 批量操作 -->
      <view class="batch-actions">
        <view class="batch-btn" @click="showCopyModal = true">
          <text>📋 复制到</text>
        </view>
        <view class="batch-btn danger" @click="confirmClear">
          <text>🗑️ 清空本周</text>
        </view>
        <view class="batch-btn" @click="showSaveTemplate = true">
          <text>💾 保存模板</text>
        </view>
        <view class="batch-btn primary" @click="$emit('upload')">
          <text>📤 上传分享</text>
        </view>
      </view>
      
      <!-- 复制弹窗 -->
      <view class="modal-overlay" v-if="showCopyModal" @click="showCopyModal = false">
        <view class="modal-content" @click.stop>
          <text class="modal-title">复制计划</text>
          <text class="modal-sub">选择要复制到的日期</text>
          <view class="day-check-list">
            <view 
              v-for="day in weekDays" 
              :key="day.dayOfWeek"
              class="day-check-item"
              @click="toggleCopyDay(day.dayOfWeek)"
            >
              <text class="check-box" :class="{ checked: copyTargets.includes(day.dayOfWeek) }">
                {{ copyTargets.includes(day.dayOfWeek) ? '✓' : '' }}
              </text>
              <text>{{ day.label }}</text>
            </view>
          </view>
          <view class="modal-footer">
            <button class="modal-btn cancel" @click="showCopyModal = false">取消</button>
            <button class="modal-btn confirm" @click="confirmCopy">确定</button>
          </view>
        </view>
      </view>
      
      <!-- 保存模板弹窗 -->
      <view class="modal-overlay" v-if="showSaveTemplate" @click="showSaveTemplate = false">
        <view class="modal-content" @click.stop>
          <text class="modal-title">保存为模板</text>
          <input class="modal-input" v-model="templateName" placeholder="输入模板名称" />
          <view class="modal-footer">
            <button class="modal-btn cancel" @click="showSaveTemplate = false">取消</button>
            <button class="modal-btn confirm" @click="confirmSaveTemplate">保存</button>
          </view>
        </view>
      </view>
      
      <!-- 类型选择器 -->
      <TypeSelector 
        :visible="showTypeSelector"
        :initialData="editingDay?.plan"
        @close="showTypeSelector = false"
        @confirm="onTypeConfirm"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import TypeSelector from './TypeSelector.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  planDays: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'update-day', 'copy-day', 'clear', 'save-template', 'upload'])

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const showTypeSelector = ref(false)
const showCopyModal = ref(false)
const showSaveTemplate = ref(false)
const editingDay = ref(null)
const copyTargets = ref([])
const templateName = ref('')

const weekDays = computed(() => {
  return weekLabels.map((label, index) => {
    const dayOfWeek = index + 1
    const plan = props.planDays.find(d => d.dayOfWeek === dayOfWeek)
    return { label, dayOfWeek, plan }
  })
})

function editDay(day) {
  editingDay.value = day
  showTypeSelector.value = true
}

function onTypeConfirm(data) {
  if (editingDay.value) {
    emit('update-day', { dayOfWeek: editingDay.value.dayOfWeek, data })
  }
  showTypeSelector.value = false
  editingDay.value = null
}

function toggleCopyDay(dayOfWeek) {
  const idx = copyTargets.value.indexOf(dayOfWeek)
  if (idx > -1) {
    copyTargets.value.splice(idx, 1)
  } else {
    copyTargets.value.push(dayOfWeek)
  }
}

function confirmCopy() {
  if (copyTargets.value.length === 0) {
    uni.showToast({ title: '请选择目标日期', icon: 'none' })
    return
  }
  const sourceDay = weekDays.value.find(d => d.plan)
  if (!sourceDay) {
    uni.showToast({ title: '没有可复制的计划', icon: 'none' })
    return
  }
  emit('copy-day', { sourceDayOfWeek: sourceDay.dayOfWeek, targetDays: copyTargets.value })
  showCopyModal.value = false
  copyTargets.value = []
}

function confirmClear() {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空本周所有训练计划吗？',
    confirmColor: '#ef4444',
    success: (res) => {
      if (res.confirm) {
        emit('clear')
      }
    }
  })
}

function confirmSaveTemplate() {
  if (!templateName.value.trim()) {
    uni.showToast({ title: '请输入模板名称', icon: 'none' })
    return
  }
  emit('save-template', templateName.value.trim())
  showSaveTemplate.value = false
  templateName.value = ''
}

function close() {
  emit('close')
}

function getTypeIcon(type) {
  const map = {
    run: '/static/images/plan/run.png',
    strength: '/static/images/plan/strength.png',
    yoga: '/static/images/plan/yoga.png',
    rest: '/static/images/plan/rest.png',
    custom: '/static/images/plan/custom.png',
    empty: '/static/images/plan/add.png'
  }
  return map[type] || '/static/images/plan/add.png'
}
</script>

<style lang="scss" scoped>
.plan-editor-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.editor-content {
  width: 100%;
  max-height: 90vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.editor-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1c1c1e;
}

.close-btn {
  font-size: 32rpx;
  color: #94a3b8;
  padding: 10rpx;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12rpx;
  padding: 24rpx 20rpx;
}

.day-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.column-week {
  font-size: 22rpx;
  color: #64748b;
}

.day-plan-card {
  width: 88rpx;
  height: 120rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: #f8fafc;
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.95);
  }
  
  &.type-run { background: rgba(59, 130, 246, 0.1); }
  &.type-strength { background: rgba(249, 115, 22, 0.1); }
  &.type-yoga { background: rgba(16, 185, 129, 0.1); }
  &.type-rest { background: rgba(148, 163, 184, 0.1); }
  &.type-custom { background: rgba(139, 92, 246, 0.1); }
}

.day-plan-icon-img {
  width: 40rpx;
  height: 40rpx;
}

.day-plan-title {
  font-size: 18rpx;
  color: #64748b;
  text-align: center;
  padding: 0 4rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80rpx;
}

.batch-actions {
  display: flex;
  gap: 16rpx;
  padding: 0 30rpx 24rpx;
}

.batch-btn {
  flex: 1;
  height: 72rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #334155;
  
  &:active {
    background: #e2e8f0;
  }
  
  &.danger {
    color: #ef4444;
  }
  
  &.primary {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: #fff;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.modal-content {
  width: 80%;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1c1c1e;
  display: block;
  margin-bottom: 8rpx;
}

.modal-sub {
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 20rpx;
  display: block;
}

.day-check-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.day-check-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #334155;
}

.check-box {
  width: 40rpx;
  height: 40rpx;
  border-radius: 10rpx;
  border: 2rpx solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #fff;
  
  &.checked {
    background: #3b82f6;
    border-color: #3b82f6;
  }
}

.modal-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
}

.modal-btn {
  flex: 1;
  height: 76rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  
  &.cancel {
    background: #f1f5f9;
    color: #64748b;
  }
  
  &.confirm {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: #fff;
  }
}
</style>
