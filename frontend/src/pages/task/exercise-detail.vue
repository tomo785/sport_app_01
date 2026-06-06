<template>
  <view class="container">
    <!-- 头部 -->
    <view class="header">
      <text class="title">动作详情</text>
    </view>
    <!-- 动作基本信息 -->
    <view class="exercise-card" v-if="exercise.id">
      <view class="exercise-header">
        <view class="exercise-name">{{ exercise.exerciseName }}</view>
        <view class="exercise-type">{{ exercise.typeDesc || '力量训练' }}</view>
      </view>
      <view class="exercise-target" v-if="exercise.targetMuscles">
        <text class="label">目标肌群：</text>
        <text class="value">{{ exercise.targetMuscles }}</text>
      </view>
      <!-- 计划信息 -->
      <view class="plan-info">
        <view class="info-title">计划安排</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-value">{{ exercise.setsPlanned || 0 }}</text>
            <text class="info-label">组数</text>
          </view>
          <view class="info-item">
            <text class="info-value">{{ exercise.repsPlanned || exercise.durationPlanned || 0 }}</text>
            <text class="info-label">{{ exercise.repsPlanned ? '次数/组' : '秒/组' }}</text>
          </view>
          <view class="info-item">
            <text class="info-value">{{ exercise.restTime || 60 }}s</text>
            <text class="info-label">组间休息</text>
          </view>
          <view class="info-item">
            <text class="info-value">{{ exercise.weightUsed || '-' }}</text>
            <text class="info-label">重量(kg)</text>
          </view>
        </view>
      </view>
      <!-- 完成情况 -->
      <view class="completion-info" v-if="exercise.status !== 0">
        <view class="info-title">完成情况</view>
        <view class="completion-stats">
          <view class="completion-item">
            <text class="label">已完成组数</text>
            <text class="value">{{ exercise.setsCompleted }}/{{ exercise.setsPlanned }}</text>
          </view>
          <view class="completion-item" v-if="exercise.caloriesBurned">
            <text class="label">消耗卡路里</text>
            <text class="value">{{ exercise.caloriesBurned }} 大卡</text>
          </view>
          <view class="completion-item" v-if="exercise.heartRateAvg">
            <text class="label">平均心率</text>
            <text class="value">{{ exercise.heartRateAvg }} 次/分</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 组数记录 -->
    <view class="sets-section" v-if="exercise.setsPlanned > 1">
      <view class="section-title">组数记录</view>
      <view class="sets-list">
        <view
          class="set-item"
          v-for="setIndex in exercise.setsPlanned"
          :key="setIndex"
          :class="{
            'completed': setIndex <= exercise.setsCompleted,
            'current': setIndex === exercise.setsCompleted + 1 && exercise.status === 1
          }"
        >
          <text class="set-number">第{{ setIndex }}组</text>
          <view class="set-status">
            <text v-if="setIndex <= exercise.setsCompleted">已完成</text>
            <text v-else-if="setIndex === exercise.setsCompleted + 1 && exercise.status === 1" class="current-text">进行中</text>
            <text v-else>待完成</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 底部操作 -->
    <view class="footer-actions">
      <button
        class="action-btn rest"
        v-if="exercise.status === 1"
        @click="startRest"
      >
        休息倒计时 {{ formatCountdown }}
      </button>
      <button
        class="action-btn primary"
        v-if="exercise.status !== 2"
        @click="completeSet"
      >
        {{ exercise.setsCompleted >= exercise.setsPlanned - 1 ? '完成动作' : '完成本组' }}
      </button>
      <button
        class="action-btn success"
        v-else
        @click="goBack"
      >返回任务</button>
    </view>
  </view>
</template>
<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { completeExercise } from '../../api/task'
const EXECUTION_KEY = 'todayPlanExecution'
const exercise = ref({})
const countdown = ref(60)
let countdownTimer = null
const formatCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})
onLoad((options) => {
  loadExerciseData(options.id)
})
function loadExerciseData(id) {
  // 优先从本地存储读取 task-detail 传递过来的数据
  try {
    const raw = uni.getStorageSync('currentExercise')
    if (raw) {
      const data = typeof raw === 'string' ? JSON.parse(raw) : raw
      if (data && String(data.id) === String(id)) {
        exercise.value = { setsCompleted: 0, setsPlanned: 1, status: 0, ...data }
        countdown.value = exercise.value.restTime || 60
        return
      }
    }
  } catch (e) {
    console.error('读取动作数据失败:', e)
  }
  // 降级：显示空状态
  exercise.value = { id }
  uni.showToast({ title: '加载动作数据失败', icon: 'none' })
}
function syncExerciseToExecution() {
  try {
    const raw = uni.getStorageSync(EXECUTION_KEY)
    const ctx = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : null
    if (!ctx?.localPlan) return
    const target = (ctx.activities || []).find(item => String(item.id) === String(exercise.value.id))
    if (!target) return
    Object.assign(target, {
      status: exercise.value.status,
      setsCompleted: exercise.value.setsCompleted,
      caloriesBurned: exercise.value.caloriesBurned || target.caloriesBurned || 0,
      completedAt: exercise.value.status === 2 ? new Date().toISOString() : target.completedAt
    })
    if ((ctx.activities || []).every(item => item.status === 2)) {
      ctx.status = 2
      ctx.completedAt = new Date().toISOString()
    } else {
      ctx.status = 1
      ctx.startedAt = ctx.startedAt || new Date().toISOString()
    }
    uni.setStorageSync(EXECUTION_KEY, JSON.stringify(ctx))
  } catch (e) {}
}
function startRest() {
  if (countdownTimer) return
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(countdownTimer)
      countdownTimer = null
      countdown.value = exercise.value.restTime || 60
      uni.showToast({ title: '休息结束', icon: 'success' })
    }
  }, 1000)
}
async function completeSet() {
  try {
    // 更新完成组数
    exercise.value.setsCompleted++
    // 同步更新本地缓存
    try {
      uni.setStorageSync('currentExercise', JSON.stringify(exercise.value))
    } catch (e) {}
    // 如果全部完成
    if (exercise.value.setsCompleted >= exercise.value.setsPlanned) {
      exercise.value.status = 2
      if (!String(exercise.value.id).includes('-')) {
        await completeExercise(exercise.value.id, {
          actualReps: exercise.value.repsPlanned,
          actualDuration: exercise.value.durationPlanned
        })
      }
      syncExerciseToExecution()
      uni.showToast({ title: '动作完成！', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 800)
    } else {
      exercise.value.status = 1
      syncExerciseToExecution()
      uni.showToast({ title: `第${exercise.value.setsCompleted}组完成`, icon: 'success' })
      // 自动开始休息
      startRest()
    }
  } catch (e) {
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}
function goBack() {
  uni.navigateBack()
}
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4f8 0%, #f8fafc 100%);
  padding: 20rpx 30rpx 200rpx;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  .title {
    font-size: 36rpx;
    font-weight: 700;
    color: #1c1c1e;
  }
}
.exercise-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
  .exercise-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    .exercise-name {
      font-size: 40rpx;
      font-weight: 700;
      color: #1c1c1e;
    }
    .exercise-type {
      font-size: 24rpx;
      color: #3b82f6;
      background: rgba(59, 130, 246, 0.1);
      padding: 8rpx 20rpx;
      border-radius: 12rpx;
    }
  }
  .exercise-target {
    margin-bottom: 30rpx;
    .label {
      font-size: 26rpx;
      color: #64748b;
    }
    .value {
      font-size: 26rpx;
      color: #1c1c1e;
      font-weight: 500;
    }
  }
  .info-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #1c1c1e;
    margin-bottom: 20rpx;
  }
  .info-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    padding: 20rpx;
    background: #f8fafc;
    border-radius: 16rpx;
    .info-item {
      text-align: center;
      .info-value {
        font-size: 36rpx;
        font-weight: 700;
        color: #3b82f6;
        display: block;
      }
      .info-label {
        font-size: 22rpx;
        color: #94a3b8;
        margin-top: 8rpx;
      }
    }
  }
  .completion-info {
    margin-top: 30rpx;
    padding-top: 30rpx;
    border-top: 1rpx solid #f1f5f9;
    .completion-stats {
      .completion-item {
        display: flex;
        justify-content: space-between;
        padding: 16rpx 0;
        .label {
          font-size: 28rpx;
          color: #64748b;
        }
        .value {
          font-size: 28rpx;
          color: #1c1c1e;
          font-weight: 600;
        }
      }
    }
  }
}
.sets-section {
  margin-bottom: 30rpx;
  .section-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #1c1c1e;
    margin-bottom: 20rpx;
  }
  .sets-list {
    .set-item {
      background: rgba(255, 255, 255, 0.9);
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 16rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      &.completed {
        background: rgba(16, 185, 129, 0.1);
        border: 1rpx solid rgba(16, 185, 129, 0.3);
      }
      &.current {
        background: rgba(59, 130, 246, 0.1);
        border: 1rpx solid rgba(59, 130, 246, 0.3);
      }
      .set-number {
        font-size: 30rpx;
        font-weight: 600;
        color: #1c1c1e;
      }
      .set-status {
        font-size: 26rpx;
        color: #94a3b8;
        .current-text {
          color: #3b82f6;
          font-weight: 500;
        }
      }
    }
  }
}
.footer-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: white;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
  .action-btn {
    flex: 1;
    height: 96rpx;
    border-radius: 24rpx;
    font-size: 32rpx;
    font-weight: 600;
    border: none;
    &.primary {
      background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
      color: white;
    }
    &.success {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      color: white;
    }
    &.rest {
      background: #f1f5f9;
      color: #64748b;
    }
    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }
  }
}
</style>
