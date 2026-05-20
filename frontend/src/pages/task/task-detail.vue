<template>
  <view class="container">
    <!-- 头部信息 -->
    <view class="header">
      <text class="title">任务详情</text>
      <text class="date">{{ taskDate }}</text>
    </view>

    <!-- 任务概览 -->
    <view class="task-overview">
      <view class="overview-card">
        <view class="task-title">{{ task.title || '今日训练' }}</view>
        <view class="task-subtitle">{{ task.subtitle || '' }}</view>
        <view class="task-stats">
          <view class="stat-item">
            <text class="stat-value">{{ task.completedExercises || 0 }}/{{ task.totalExercises || 0 }}</text>
            <text class="stat-label">动作完成</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ task.progress || 0 }}%</text>
            <text class="stat-label">完成进度</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ task.caloriesBurned || 0 }}</text>
            <text class="stat-label">消耗大卡</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 动作列表 -->
    <view class="exercise-section">
      <view class="section-title">训练动作</view>
      <view class="exercise-list">
        <view 
          class="exercise-card" 
          v-for="(item, index) in exercises" 
          :key="index"
          :class="{ 'completed': item.status === 2, 'in-progress': item.status === 1 }"
          @click="goToExerciseDetail(item)"
        >
          <view class="exercise-info">
            <view class="exercise-name">{{ item.exerciseName }}</view>
            <view class="exercise-detail">
              <text v-if="item.setsPlanned">{{ item.setsPlanned }}组</text>
              <text v-if="item.repsPlanned">×{{ item.repsPlanned }}次</text>
              <text v-if="item.durationPlanned">{{ item.durationPlanned }}秒</text>
              <text class="rest-time" v-if="item.restTime">休息{{ item.restTime }}秒</text>
            </view>
          </view>
          <view class="exercise-status">
            <text class="status-icon" v-if="item.status === 2">✓</text>
            <text class="status-icon" v-else-if="item.status === 1">▶</text>
            <text class="status-text" v-else>未开始</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer-actions">
      <button 
        class="action-btn primary" 
        v-if="task.status === 0"
        @click="startTask"
      >开始训练</button>
      <button 
        class="action-btn primary" 
        v-else-if="task.status === 1"
        @click="continueTask"
      >继续训练</button>
      <button 
        class="action-btn success" 
        v-else-if="task.status === 2"
        @click="viewSummary"
      >查看总结</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getTodayTask, startTodayTask } from '../../api/task'

const task = ref({})
const exercises = ref([])
const taskDate = ref('')

onLoad((options) => {
  taskDate.value = options.date || getTodayDate()
  loadTaskDetail()
})

onShow(() => {
  loadTaskDetail()
})

function getTodayDate() {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
}

async function loadTaskDetail() {
  try {
    const res = await getTodayTask()
    if (res.code === 200 && res.data) {
      task.value = res.data
      exercises.value = res.data.exercises || []
    }
  } catch (e) {
    console.error('加载任务详情失败:', e)
  }
}

function goToExerciseDetail(exItem) {
  uni.setStorageSync('currentExercise', JSON.stringify(exItem))
  uni.navigateTo({
    url: `/pages/task/exercise-detail?id=${exItem.id}`
  })
}

async function startTask() {
  try {
    await startTodayTask()
    uni.showToast({ title: '开始训练', icon: 'success' })
    loadTaskDetail()
  } catch (e) {
    uni.showToast({ title: '开始失败', icon: 'none' })
  }
}

function continueTask() {
  const nextExercise = exercises.value.find(e => e.status === 0 || e.status === 1)
  if (nextExercise) {
    goToExerciseDetail(nextExercise)
  }
}

function viewSummary() {
  uni.showToast({ title: '训练已完成！', icon: 'success' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4f8 0%, #f8fafc 100%);
  padding: 20rpx 30rpx 140rpx;
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

  .date {
    font-size: 28rpx;
    color: #64748b;
  }
}

.task-overview {
  margin-bottom: 30rpx;

  .overview-card {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 24rpx;
    padding: 40rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);

    .task-title {
      font-size: 36rpx;
      font-weight: 700;
      color: #1c1c1e;
      margin-bottom: 8rpx;
    }

    .task-subtitle {
      font-size: 28rpx;
      color: #64748b;
      margin-bottom: 30rpx;
    }

    .task-stats {
      display: flex;
      justify-content: space-around;
      padding-top: 30rpx;
      border-top: 1rpx solid #f1f5f9;

      .stat-item {
        text-align: center;

        .stat-value {
          font-size: 40rpx;
          font-weight: 700;
          color: #3b82f6;
          display: block;
        }

        .stat-label {
          font-size: 24rpx;
          color: #94a3b8;
          margin-top: 8rpx;
        }
      }
    }
  }
}

.exercise-section {
  .section-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #1c1c1e;
    margin-bottom: 20rpx;
  }

  .exercise-list {
    .exercise-card {
      background: rgba(255, 255, 255, 0.9);
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);

      &.completed {
        background: rgba(16, 185, 129, 0.1);
        border: 1rpx solid rgba(16, 185, 129, 0.3);
      }

      &.in-progress {
        background: rgba(59, 130, 246, 0.1);
        border: 1rpx solid rgba(59, 130, 246, 0.3);
      }

      .exercise-info {
        .exercise-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #1c1c1e;
          margin-bottom: 10rpx;
        }

        .exercise-detail {
          font-size: 26rpx;
          color: #64748b;
          display: flex;
          align-items: center;
          gap: 10rpx;

          .rest-time {
            color: #f97316;
            background: rgba(249, 115, 22, 0.1);
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
            font-size: 22rpx;
          }
        }
      }

      .exercise-status {
        .status-icon {
          width: 60rpx;
          height: 60rpx;
          border-radius: 50%;
          background: #10b981;
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32rpx;
        }

        .status-text {
          font-size: 24rpx;
          color: #94a3b8;
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

  .action-btn {
    width: 100%;
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

    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }
  }
}
</style>
