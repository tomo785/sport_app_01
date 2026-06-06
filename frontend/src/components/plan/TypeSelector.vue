<template>
  <view
    class="type-selector-popup"
    :class="{ show: visible, hide: isHiding }"
    v-show="realVisible"
    @click="onMaskClick"
    @touchmove.stop.prevent="noop"
  >
    <view
      class="selector-content"
      @click.stop
      @touchmove.stop
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <!-- 拖拽指示条 -->
      <view class="drag-handle-bar">
        <view class="drag-indicator"></view>
      </view>
      <view class="selector-header">
        <text class="selector-title">{{ editingDay ? weekLabels[editingDay.dayOfWeek - 1] + ' · 选择训练类型' : '选择训练类型' }}</text>
        <AppIcon class="close-btn" name="close" size="28" @click="close" />
      </view>
      <!-- 两列主体：左类型 + 右详情 -->
      <view class="selector-body">
        <!-- 左侧类型栏 -->
        <scroll-view class="type-sidebar" scroll-y :show-scrollbar="false" :enhanced="true">
          <view
            v-for="type in types"
            :key="type.value"
            class="sidebar-item"
            :class="{ active: selectedType === type.value }"
            @click="selectType(type.value)"
          >
            <image class="sidebar-icon-img" :src="type.icon" mode="aspectFit" />
            <text class="sidebar-name">{{ type.label }}</text>
          </view>
        </scroll-view>
        <!-- 右侧详情面板 -->
        <scroll-view class="detail-panel" scroll-y :show-scrollbar="false" :enhanced="true">
          <view class="detail-inner" v-if="selectedType">
            <!-- 跑步详情 -->
            <template v-if="selectedType === 'run'">
              <view class="detail-section">
                <text class="section-title">跑步形式</text>
                <view class="option-grid">
                  <view
                    v-for="sub in runSubTypes"
                    :key="sub.value"
                    class="option-card"
                    :class="{ active: form.runParams?.subType === sub.value }"
                    @click="setRunSubType(sub.value)"
                  >
                    <text class="option-label">{{ sub.label }}</text>
                    <text v-if="sub.desc" class="option-desc">{{ sub.desc }}</text>
                  </view>
                </view>
              </view>
              <view class="detail-section">
                <text class="section-title">详细配置</text>
                <view class="form-group">
                  <text class="form-label">训练名称</text>
                  <input class="form-input" v-model="form.title" placeholder="例如：400米间歇训练" />
                </view>
                <view class="form-group">
                  <text class="form-label">距离 (km)</text>
                  <input class="form-input" type="digit" v-model="form.runParams.distance" placeholder="5" />
                </view>
                <view class="form-group">
                  <text class="form-label">组数</text>
                  <input class="form-input" type="number" v-model="form.runParams.sets" placeholder="1" />
                </view>
                <view class="form-group">
                  <text class="form-label">配速要求</text>
                  <input class="form-input" v-model="form.runParams.pace" placeholder="例如：5:30/km" />
                </view>
              </view>
            </template>
            <!-- 力量详情 -->
            <template v-if="selectedType === 'strength'">
              <view class="detail-section">
                <text class="section-title">训练部位</text>
                <view class="option-grid">
                  <view
                    v-for="part in bodyParts"
                    :key="part"
                    class="option-card"
                    :class="{ active: form.bodyPart === part }"
                    @click="form.bodyPart = part"
                  >
                    <text class="option-label">{{ part }}</text>
                  </view>
                </view>
              </view>
              <view class="detail-section">
                <text class="section-title">详细配置</text>
                <view class="form-group">
                  <text class="form-label">训练名称</text>
                  <input class="form-input" v-model="form.title" placeholder="例如：胸部力量训练" />
                </view>
                <view class="form-group">
                  <text class="form-label">动作列表</text>
                  <view class="exercise-list-editor">
                    <view class="exercise-row" v-for="(ex, idx) in form.exercises" :key="idx">
                      <input class="ex-name-input" v-model="ex.name" placeholder="动作名称" />
                      <input class="ex-num-input" type="number" v-model="ex.sets" placeholder="组" />
                      <text class="ex-x">×</text>
                      <input class="ex-num-input" v-model="ex.reps" placeholder="次" />
                      <AppIcon class="ex-delete" name="close" size="22" @click="removeExercise(idx)" />
                    </view>
                    <view class="add-exercise" @click="addExercise">
                      <text class="add-icon">+</text>
                      <text class="add-text">添加动作</text>
                    </view>
                  </view>
                </view>
              </view>
            </template>
            <!-- 瑜伽详情 -->
            <template v-if="selectedType === 'yoga'">
              <view class="detail-section">
                <text class="section-title">瑜伽流派</text>
                <view class="option-grid">
                  <view
                    v-for="style in yogaStyles"
                    :key="style.value"
                    class="option-card"
                    :class="{ active: form.yogaStyle === style.value }"
                    @click="form.yogaStyle = style.value"
                  >
                    <text class="option-label">{{ style.label }}</text>
                  </view>
                </view>
              </view>
              <view class="detail-section">
                <text class="section-title">详细配置</text>
                <view class="form-group">
                  <text class="form-label">训练名称</text>
                  <input class="form-input" v-model="form.title" placeholder="例如：晨间流瑜伽" />
                </view>
              </view>
            </template>
            <!-- 休息详情 -->
            <template v-if="selectedType === 'rest'">
              <view class="detail-section">
                <text class="section-title">休息形式</text>
                <view class="option-grid">
                  <view
                    v-for="r in restTypes"
                    :key="r.value"
                    class="option-card"
                    :class="{ active: form.restType === r.value }"
                    @click="form.restType = r.value"
                  >
                    <text class="option-label">{{ r.label }}</text>
                  </view>
                </view>
              </view>
              <view class="detail-section">
                <text class="section-title">详细配置</text>
                <view class="form-group">
                  <text class="form-label">训练名称</text>
                  <input class="form-input" v-model="form.title" placeholder="例如：完全休息日" />
                </view>
              </view>
            </template>
            <!-- 自定义详情 -->
            <template v-if="selectedType === 'custom'">
              <view class="detail-section">
                <text class="section-title">详细配置</text>
                <view class="form-group">
                  <text class="form-label">训练名称</text>
                  <input class="form-input" v-model="form.title" placeholder="例如：交叉训练" />
                </view>
              </view>
            </template>
            <!-- 通用字段 -->
            <view class="detail-section">
              <view class="form-group">
                <text class="form-label">预计时长 (分钟)</text>
                <input class="form-input" type="number" v-model="form.duration" placeholder="45" />
              </view>
              <view class="form-group">
                <text class="form-label">详细说明</text>
                <textarea class="form-textarea" v-model="form.description" placeholder="描述训练内容和注意事项..." />
              </view>
            </view>
          </view>
          <!-- 未选择类型时的占位 -->
          <view class="detail-empty" v-else>
            <AppIcon class="empty-icon" name="arrow-left" size="64" />
            <text class="empty-text">请选择左侧训练类型</text>
          </view>
        </scroll-view>
      </view>
      <view class="selector-footer">
        <button class="footer-btn cancel" @click="close">取消</button>
        <button class="footer-btn confirm" @click="confirm">确定</button>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, watch, nextTick } from 'vue'
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: null
  },
  editingDay: {
    type: Object,
    default: null
  }
})
const emit = defineEmits(['close', 'confirm'])
const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const types = [
  { value: 'run', label: '跑步', icon: '/static/images/plan/run.png' },
  { value: 'strength', label: '力量', icon: '/static/images/plan/strength.png' },
  { value: 'yoga', label: '瑜伽', icon: '/static/images/plan/yoga.png' },
  { value: 'rest', label: '休息', icon: '/static/images/plan/rest.png' },
  { value: 'custom', label: '自定义', icon: '/static/images/plan/custom.png' }
]
const runSubTypes = [
  { value: 'interval', label: '间歇', desc: '400m/800m' },
  { value: 'long', label: '长距离', desc: 'LSD慢跑' },
  { value: 'tempo', label: 'Tempo', desc: '乳酸阈值跑' },
  { value: 'easy', label: '轻松跑', desc: '有氧恢复' },
  { value: 'recovery', label: '恢复跑', desc: '超慢放松' }
]
const bodyParts = ['胸', '背', '腿', '肩', '臂', '核心']
const yogaStyles = [
  { value: 'hatha', label: '哈他瑜伽' },
  { value: 'vinyasa', label: '流瑜伽' },
  { value: 'yin', label: '阴瑜伽' },
  { value: 'power', label: '力量瑜伽' },
  { value: 'restorative', label: '修复瑜伽' }
]
const restTypes = [
  { value: 'full', label: '完全休息' },
  { value: 'active', label: '主动恢复' },
  { value: 'stretch', label: '拉伸放松' },
  { value: 'massage', label: '按摩放松' }
]
const realVisible = ref(false)
const isHiding = ref(false)
const selectedType = ref('')
const form = ref({
  title: '',
  duration: '',
  description: '',
  runParams: { subType: 'easy', distance: '', sets: '', pace: '' },
  bodyPart: '',
  exercises: [],
  yogaStyle: '',
  restType: ''
})
// 拖拽关闭相关
const touchStartY = ref(0)
const touchCurrentY = ref(0)
const isDragging = ref(false)
const DRAG_THRESHOLD = 120
watch(() => props.visible, (val) => {
  if (val) {
    isHiding.value = false
    realVisible.value = true
    if (props.initialData) {
      selectedType.value = props.initialData.type || ''
      form.value = {
        title: props.initialData.title || '',
        duration: props.initialData.details?.duration || '',
        description: props.initialData.details?.description || '',
        runParams: props.initialData.details?.runParams || { subType: 'easy', distance: '', sets: '', pace: '' },
        bodyPart: props.initialData.details?.bodyPart || '',
        exercises: props.initialData.details?.exercises ? [...props.initialData.details.exercises] : [],
        yogaStyle: props.initialData.details?.yogaStyle || '',
        restType: props.initialData.details?.restType || ''
      }
    } else {
      resetForm()
    }
  } else {
    startHide()
  }
})
function startHide() {
  isHiding.value = true
  setTimeout(() => {
    realVisible.value = false
    isHiding.value = false
  }, 300)
}
function resetForm() {
  selectedType.value = ''
  form.value = {
    title: '',
    duration: '',
    description: '',
    runParams: { subType: 'easy', distance: '', sets: '', pace: '' },
    bodyPart: '',
    exercises: [],
    yogaStyle: '',
    restType: ''
  }
}
function selectType(type) {
  selectedType.value = type
  if (!form.value.title) {
    const t = types.find(item => item.value === type)
    if (t) form.value.title = t.label
  }
}
function setRunSubType(val) {
  form.value.runParams.subType = val
  const sub = runSubTypes.find(s => s.value === val)
  if (sub && (!form.value.title || types.some(t => t.label === form.value.title))) {
    form.value.title = sub.label
  }
}
function addExercise() {
  form.value.exercises.push({ name: '', sets: '', reps: '' })
}
function removeExercise(idx) {
  form.value.exercises.splice(idx, 1)
}
function noop() {}
function onMaskClick() {
  close()
}
function close() {
  emit('close')
}
function onTouchStart(e) {
  touchStartY.value = e.touches[0].clientY
  isDragging.value = true
}
function onTouchMove(e) {
  if (!isDragging.value) return
  touchCurrentY.value = e.touches[0].clientY
  const diff = touchCurrentY.value - touchStartY.value
  if (diff > 0) {
    // 向下拖拽时给内容添加位移
    const content = e.currentTarget
    if (content && content.style) {
      content.style.transform = `translateY(${diff * 0.5}px)`
    }
  }
}
function onTouchEnd(e) {
  if (!isDragging.value) return
  const diff = touchCurrentY.value - touchStartY.value
  isDragging.value = false
  // 重置 transform
  const content = e.currentTarget
  if (content && content.style) {
    content.style.transform = ''
  }
  if (diff > DRAG_THRESHOLD) {
    close()
  }
}
function confirm() {
  if (!selectedType.value) {
    uni.showToast({ title: '请选择训练类型', icon: 'none' })
    return
  }
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入训练名称', icon: 'none' })
    return
  }
  const result = {
    type: selectedType.value,
    title: form.value.title.trim(),
    details: {
      description: form.value.description,
      duration: parseInt(form.value.duration) || 0
    }
  }
  if (selectedType.value === 'run') {
    result.details.runParams = { ...form.value.runParams }
  }
  if (selectedType.value === 'strength') {
    result.details.bodyPart = form.value.bodyPart
    result.details.exercises = form.value.exercises.filter(e => e.name.trim())
  }
  if (selectedType.value === 'yoga') {
    result.details.yogaStyle = form.value.yogaStyle
  }
  if (selectedType.value === 'rest') {
    result.details.restType = form.value.restType
  }
  emit('confirm', result)
  resetForm()
}
</script>
<style lang="scss" scoped>
.type-selector-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
  pointer-events: none;
  transition: background 0.3s ease;
  &.show {
    pointer-events: auto;
    background: rgba(0, 0, 0, 0.5);
    .selector-content {
      transform: translateY(0);
      opacity: 1;
    }
  }
  &.hide {
    background: rgba(0, 0, 0, 0);
    .selector-content {
      transform: translateY(100%);
      opacity: 0.8;
    }
  }
}
.selector-content {
  width: 100%;
  max-height: 88vh;
  background: #fff;
  border-radius: 36rpx 36rpx 0 0;
  display: flex;
  flex-direction: column;
  transform: translateY(100%);
  opacity: 0;
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.3s ease;
  box-shadow: 0 -8rpx 40rpx rgba(0, 0, 0, 0.12);
}
/* 拖拽指示条 */
.drag-handle-bar {
  display: flex;
  justify-content: center;
  padding: 16rpx 0 8rpx;
  flex-shrink: 0;
}
.drag-indicator {
  width: 72rpx;
  height: 6rpx;
  background: #cbd5e1;
  border-radius: 6rpx;
}
.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 30rpx 20rpx;
  flex-shrink: 0;
}
.selector-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1c1c1e;
}
.close-btn {
  font-size: 32rpx;
  color: #94a3b8;
  padding: 10rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f1f5f9;
}
/* 两列主体布局 */
.selector-body {
  display: flex;
  flex-direction: row;
  flex: 1;
  min-height: 0;
  height: calc(88vh - 220rpx - env(safe-area-inset-bottom));
  max-height: 720rpx;
}
/* 左侧类型栏 */
.type-sidebar {
  width: 22%;
  background: #f8fafc;
  border-right: 1rpx solid #f1f5f9;
  height: 100%;
}
.sidebar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28rpx 8rpx;
  margin: 0 12rpx 12rpx;
  border-radius: 16rpx;
  transition: all 0.2s ease;
  &:first-child {
    margin-top: 12rpx;
  }
  &:active {
    transform: scale(0.95);
  }
  &.active {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    .sidebar-icon,
    .sidebar-name {
      color: #fff;
    }
  }
}
.sidebar-icon-img {
  width: 40rpx;
  height: 40rpx;
  margin-bottom: 8rpx;
}
.sidebar-name {
  font-size: 22rpx;
  color: #64748b;
  text-align: center;
}
/* 右侧详情面板 */
.detail-panel {
  width: 78%;
  background: #fff;
  height: 100%;
}
.detail-inner {
  padding: 24rpx;
}
.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 40rpx;
}
.empty-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}
.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
}
.detail-section {
  margin-bottom: 32rpx;
  &:last-child {
    margin-bottom: 0;
  }
}
.section-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1c1c1e;
  margin-bottom: 16rpx;
  display: block;
}
/* 选项卡片网格 */
.option-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}
.option-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx 16rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
  &:active {
    transform: scale(0.96);
  }
  &.active {
    background: #eff6ff;
    border-color: #3b82f6;
    .option-label {
      color: #3b82f6;
      font-weight: 700;
    }
    .option-desc {
      color: #60a5fa;
    }
  }
}
.option-label {
  font-size: 26rpx;
  color: #334155;
  text-align: center;
}
.option-desc {
  font-size: 20rpx;
  color: #94a3b8;
  margin-top: 4rpx;
  text-align: center;
}
/* 表单样式 */
.form-group {
  margin-bottom: 24rpx;
  &:last-child {
    margin-bottom: 0;
  }
}
.form-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #1c1c1e;
  margin-bottom: 12rpx;
  display: block;
}
.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #334155;
  box-sizing: border-box;
}
.form-textarea {
  width: 100%;
  height: 160rpx;
  padding: 16rpx 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #334155;
  box-sizing: border-box;
}
/* 力量动作列表 */
.exercise-list-editor {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.exercise-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.ex-name-input {
  flex: 1;
  height: 72rpx;
  padding: 0 16rpx;
  background: #f8fafc;
  border-radius: 10rpx;
  font-size: 26rpx;
}
.ex-num-input {
  width: 80rpx;
  height: 72rpx;
  padding: 0 8rpx;
  background: #f8fafc;
  border-radius: 10rpx;
  font-size: 26rpx;
  text-align: center;
}
.ex-x {
  font-size: 24rpx;
  color: #94a3b8;
}
.ex-delete {
  font-size: 24rpx;
  color: #ef4444;
  padding: 10rpx;
}
.add-exercise {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  border: 2rpx dashed #cbd5e1;
  .add-icon {
    font-size: 32rpx;
    color: #3b82f6;
  }
  .add-text {
    font-size: 26rpx;
    color: #3b82f6;
  }
}
/* 底部按钮 */
.selector-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f1f5f9;
  flex-shrink: 0;
  background: #fff;
}
.footer-btn {
  flex: 1;
  height: 84rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  &:active {
    opacity: 0.9;
  }
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