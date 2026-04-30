<template>
  <view class="type-selector-popup" v-if="visible" @click="close">
    <view class="selector-content" @click.stop>
      <view class="selector-header">
        <text class="selector-title">选择训练类型</text>
        <text class="close-btn" @click="close">✕</text>
      </view>
      
      <view class="type-grid">
        <view 
          v-for="type in types" 
          :key="type.value"
          class="type-item"
          :class="{ active: selectedType === type.value }"
          @click="selectType(type.value)"
        >
          <text class="type-icon">{{ type.icon }}</text>
          <text class="type-name">{{ type.label }}</text>
        </view>
      </view>
      
      <!-- 详细配置表单 -->
      <view class="config-form" v-if="selectedType">
        <view class="form-group">
          <text class="form-label">训练名称</text>
          <input class="form-input" v-model="form.title" placeholder="例如：胸部训练" />
        </view>
        
        <!-- 跑步配置 -->
        <template v-if="selectedType === 'run'">
          <view class="form-group">
            <text class="form-label">子类型</text>
            <view class="sub-type-row">
              <text 
                v-for="sub in runSubTypes" 
                :key="sub.value"
                class="sub-type-tag"
                :class="{ active: form.runParams?.subType === sub.value }"
                @click="setRunSubType(sub.value)"
              >{{ sub.label }}</text>
            </view>
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
        </template>
        
        <!-- 力量配置 -->
        <template v-if="selectedType === 'strength'">
          <view class="form-group">
            <text class="form-label">训练部位</text>
            <view class="sub-type-row">
              <text 
                v-for="part in bodyParts" 
                :key="part"
                class="sub-type-tag"
                :class="{ active: form.bodyPart === part }"
                @click="form.bodyPart = part"
              >{{ part }}</text>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">动作列表</text>
            <view class="exercise-list-editor">
              <view class="exercise-row" v-for="(ex, idx) in form.exercises" :key="idx">
                <input class="ex-name-input" v-model="ex.name" placeholder="动作名称" />
                <input class="ex-num-input" type="number" v-model="ex.sets" placeholder="组" />
                <text class="ex-x">×</text>
                <input class="ex-num-input" v-model="ex.reps" placeholder="次" />
                <text class="ex-delete" @click="removeExercise(idx)">✕</text>
              </view>
              <view class="add-exercise" @click="addExercise">
                <text class="add-icon">+</text>
                <text class="add-text">添加动作</text>
              </view>
            </view>
          </view>
        </template>
        
        <!-- 瑜伽/休息/通用 -->
        <view class="form-group">
          <text class="form-label">预计时长 (分钟)</text>
          <input class="form-input" type="number" v-model="form.duration" placeholder="45" />
        </view>
        <view class="form-group">
          <text class="form-label">详细说明</text>
          <textarea class="form-textarea" v-model="form.description" placeholder="描述训练内容和注意事项..." />
        </view>
      </view>
      
      <view class="selector-footer">
        <button class="footer-btn cancel" @click="close">取消</button>
        <button class="footer-btn confirm" @click="confirm">确定</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'confirm'])

const types = [
  { value: 'run', label: '跑步', icon: '🏃' },
  { value: 'strength', label: '力量', icon: '🏋️' },
  { value: 'yoga', label: '瑜伽', icon: '🧘' },
  { value: 'rest', label: '休息', icon: '⛔' },
  { value: 'custom', label: '自定义', icon: '📋' }
]

const runSubTypes = [
  { value: 'interval', label: '间歇' },
  { value: 'long', label: '长距离' },
  { value: 'tempo', label: 'Tempo' },
  { value: 'easy', label: '轻松跑' },
  { value: 'recovery', label: '恢复跑' }
]

const bodyParts = ['胸', '背', '腿', '肩', '臂', '核心']

const selectedType = ref('')
const form = ref({
  title: '',
  duration: '',
  description: '',
  runParams: { subType: 'easy', distance: '', sets: '', pace: '' },
  bodyPart: '',
  exercises: []
})

watch(() => props.visible, (val) => {
  if (val) {
    if (props.initialData) {
      selectedType.value = props.initialData.type || ''
      form.value = {
        title: props.initialData.title || '',
        duration: props.initialData.details?.duration || '',
        description: props.initialData.details?.description || '',
        runParams: props.initialData.details?.runParams || { subType: 'easy', distance: '', sets: '', pace: '' },
        bodyPart: '',
        exercises: props.initialData.details?.exercises ? [...props.initialData.details.exercises] : []
      }
    } else {
      resetForm()
    }
  }
})

function resetForm() {
  selectedType.value = ''
  form.value = {
    title: '',
    duration: '',
    description: '',
    runParams: { subType: 'easy', distance: '', sets: '', pace: '' },
    bodyPart: '',
    exercises: []
  }
}

function selectType(type) {
  selectedType.value = type
}

function setRunSubType(val) {
  form.value.runParams.subType = val
}

function addExercise() {
  form.value.exercises.push({ name: '', sets: '', reps: '' })
}

function removeExercise(idx) {
  form.value.exercises.splice(idx, 1)
}

function close() {
  emit('close')
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
    result.details.exercises = form.value.exercises.filter(e => e.name.trim())
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.selector-content {
  width: 100%;
  max-height: 85vh;
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

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f1f5f9;
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
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16rpx;
  padding: 24rpx 30rpx;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;
  border-radius: 16rpx;
  background: #f8fafc;
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    
    .type-icon,
    .type-name {
      color: #fff;
    }
  }
}

.type-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.type-name {
  font-size: 22rpx;
  color: #64748b;
}

.config-form {
  flex: 1;
  overflow-y: auto;
  padding: 0 30rpx;
  max-height: 50vh;
}

.form-group {
  margin-bottom: 24rpx;
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

.sub-type-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.sub-type-tag {
  padding: 12rpx 24rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #64748b;
  transition: all 0.2s ease;
  
  &.active {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: #fff;
  }
}

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

.selector-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f1f5f9;
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
