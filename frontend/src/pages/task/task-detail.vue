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
          :class="{ 'completed': item.status === 2, 'in-progress': item.status === 1, 'selected': String(item.id) === String(selectedExerciseId) }"
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
            <view class="exercise-tags" v-if="item.tags && item.tags.length > 0">
              <view
                class="exercise-tag"
                v-for="(tag, tIdx) in item.tags"
                :key="tIdx"
                :style="{ background: tag.bg, color: tag.color }"
              >
                {{ tag.text }}
              </view>
            </view>
            <view class="exercise-tags" v-else>
              <view class="exercise-tag" v-if="item.setsPlanned && item.repsPlanned" style="background:#fff7ed;color:#f59e0b">
                {{ item.setsPlanned }}×{{ item.repsPlanned }}
              </view>
              <view class="exercise-tag" v-else-if="item.setsPlanned" style="background:#fff7ed;color:#f59e0b">
                {{ item.setsPlanned }}组
              </view>
              <view class="exercise-tag" v-if="item.durationPlanned" style="background:#eff6ff;color:#3b82f6">
                {{ item.durationPlanned }}秒
              </view>
              <view class="exercise-tag" v-if="item.restTime" style="background:#fff7ed;color:#f97316">
                休息{{ item.restTime }}秒
              </view>
            </view>
          </view>
          <view class="exercise-status">
            <AppIcon class="status-icon" v-if="item.status === 2" name="check" size="24" />
            <AppIcon class="status-icon" v-else-if="item.status === 1" name="play-right-fill" size="24" />
            <text class="status-text" v-else>未开始</text>
          </view>
          <view class="exercise-actions" @click.stop>
            <view class="mini-btn" @click="goToExerciseDetail(item)">详情</view>
            <view class="mini-btn primary" v-if="item.status !== 2" @click="completePlanExercise(item)">完成</view>
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
      <button
        class="action-btn settle"
        v-if="task.status !== 2 && exercises.length > 0"
        @click="settleTask"
      >结算训练</button>
    </view>
  </view>
</template>
<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getTodayTask, startTodayTask, completeExercise as completeExerciseApi } from '../../api/task'
const EXECUTION_KEY = 'todayPlanExecution'
const LOCAL_RECORDS_KEY = 'local_workout_records'
const task = ref({})
const exercises = ref([])
const taskDate = ref('')
const source = ref('')
const selectedExerciseId = ref('')
onLoad((options) => {
  taskDate.value = options.date || getTodayDate()
  source.value = options.source || ''
  selectedExerciseId.value = ''
  loadTaskDetail(options)
})
onShow(() => {
  loadTaskDetail()
})
function getTodayDate() {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
}
function readExecutionContext() {
  try {
    const raw = uni.getStorageSync(EXECUTION_KEY)
    return raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : null
  } catch (e) {
    return null
  }
}
function writeExecutionContext(ctx) {
  uni.setStorageSync(EXECUTION_KEY, JSON.stringify(ctx))
}
function buildTaskFromExecution(ctx) {
  const list = (ctx.activities || []).map((item, idx) => ({
    status: 0,
    setsCompleted: item.status === 2 ? (item.setsPlanned || 1) : 0,
    ...item,
    id: item.id || `${ctx.id}-${idx}`,
    exerciseName: item.exerciseName || item.name || `小项目${idx + 1}`,
    setsPlanned: Math.max(1, Number(item.setsPlanned || 1)),
    repsPlanned: Number(item.repsPlanned || 0),
    durationPlanned: Number(item.durationPlanned || 0),
    restTime: Number(item.restTime || 60)
  }))
  const completed = list.filter(i => i.status === 2).length
  const progress = list.length ? Math.round((completed / list.length) * 100) : 0
  exercises.value = list
  task.value = {
    id: ctx.id,
    localPlan: true,
    status: progress >= 100 ? 2 : (ctx.status || (completed > 0 ? 1 : 0)),
    title: ctx.title || '今日训练',
    subtitle: ctx.subtitle || `${ctx.duration || 0}分钟 · ${list.length}项训练`,
    completedExercises: completed,
    totalExercises: list.length,
    progress,
    caloriesBurned: list.reduce((sum, item) => sum + (item.caloriesBurned || 0), 0)
  }
}
async function loadTaskDetail(options = {}) {
  const ctx = readExecutionContext()
  if (source.value === 'todayPlan' && ctx) {
    buildTaskFromExecution(ctx)
    const idx = Number(options.index || 0)
    if (!selectedExerciseId.value && exercises.value[idx]) {
      selectedExerciseId.value = exercises.value[idx].id
    }
    return
  }
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
  selectedExerciseId.value = exItem.id
  uni.setStorageSync('currentExercise', JSON.stringify(exItem))
  uni.navigateTo({
    url: `/pages/task/exercise-detail?id=${exItem.id}`
  })
}
async function startTask() {
  const ctx = readExecutionContext()
  if (ctx?.localPlan) {
    ctx.status = 1
    ctx.startedAt = ctx.startedAt || new Date().toISOString()
    const first = (ctx.activities || []).find(item => item.status !== 2)
    if (first) first.status = 1
    writeExecutionContext(ctx)
    buildTaskFromExecution(ctx)
    uni.showToast({ title: '开始训练', icon: 'success' })
    return
  }
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
function estimateCalories(item) {
  const minutes = Math.max(1, Math.round((item.durationPlanned || 0) / 60) || 8)
  if ((item.typeDesc || '').includes('有氧') || item.type === 'run') return minutes * 7
  if (item.type === 'strength') return minutes * 5
  return minutes * 4
}
function completePlanExercise(item) {
  const ctx = readExecutionContext()
  if (!ctx?.localPlan) {
    completeExerciseApi(item.id, {
      actualReps: item.repsPlanned,
      actualDuration: item.durationPlanned
    }).then(loadTaskDetail).catch(() => uni.showToast({ title: '提交失败', icon: 'none' }))
    return
  }
  ctx.status = 1
  ctx.startedAt = ctx.startedAt || new Date().toISOString()
  const target = (ctx.activities || []).find(ex => String(ex.id) === String(item.id))
  if (target) {
    target.status = 2
    target.setsCompleted = target.setsPlanned || 1
    target.caloriesBurned = estimateCalories(target)
    target.completedAt = new Date().toISOString()
  }
  const next = (ctx.activities || []).find(ex => ex.status !== 2)
  if (next) next.status = 1
  const done = (ctx.activities || []).every(ex => ex.status === 2)
  if (done) {
    ctx.status = 2
    ctx.completedAt = new Date().toISOString()
  }
  writeExecutionContext(ctx)
  buildTaskFromExecution(ctx)
  uni.showToast({ title: done ? '全部完成' : '小项目完成', icon: 'success' })
}
function saveLocalPlanRecord(ctx) {
  if (ctx.recordId) return { id: ctx.recordId }
  const now = new Date()
  const started = ctx.startedAt || new Date(now.getTime() - Math.max(1, ctx.duration || 1) * 60 * 1000).toISOString()
  const duration = Math.max(60, (ctx.activities || []).reduce((sum, item) => sum + (item.durationPlanned || 0), 0))
  const calories = (ctx.activities || []).reduce((sum, item) => sum + (item.caloriesBurned || estimateCalories(item)), 0)
  const record = {
    id: `local-plan-${Date.now()}`,
    localOnly: true,
    type: 1,
    typeName: ctx.title || '计划训练',
    status: 1,
    statusDesc: '已完成',
    startTime: started,
    endTime: now.toISOString(),
    startTimeStr: started.replace('T', ' ').substring(0, 19),
    endTimeStr: now.toISOString().replace('T', ' ').substring(0, 19),
    duration,
    distance: 0,
    calories,
    steps: 0,
    avgPace: 0,
    avgSpeed: 0,
    goalType: 'plan',
    goalValue: ctx.title || '今日计划',
    goalAchieved: true,
    planActivities: ctx.activities || [],
    trajectory: []
  }
  try {
    const raw = uni.getStorageSync(LOCAL_RECORDS_KEY)
    const list = raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
    uni.setStorageSync(LOCAL_RECORDS_KEY, JSON.stringify([record, ...(Array.isArray(list) ? list : [])].slice(0, 100)))
  } catch (e) {}
  ctx.recordId = record.id
  writeExecutionContext(ctx)
  return record
}
function settleTask() {
  const ctx = readExecutionContext()
  if (!ctx?.localPlan) {
    viewSummary()
    return
  }
  uni.showModal({
    title: '结算训练',
    content: '将未完成的小项目也按当前计划结算，并生成今日训练记录。',
    confirmText: '结算',
    cancelText: '继续训练',
    success: (res) => {
      if (!res.confirm) return
      ctx.status = 2
      ctx.startedAt = ctx.startedAt || new Date().toISOString()
      ctx.completedAt = new Date().toISOString()
      ctx.activities = (ctx.activities || []).map(item => ({
        ...item,
        status: 2,
        setsCompleted: item.setsPlanned || 1,
        caloriesBurned: item.caloriesBurned || estimateCalories(item)
      }))
      const record = saveLocalPlanRecord(ctx)
      writeExecutionContext(ctx)
      buildTaskFromExecution(ctx)
      uni.showToast({ title: '训练已结算', icon: 'success' })
      setTimeout(() => {
        uni.navigateTo({ url: `/pages/workout/detail?id=${record.id}` })
      }, 500)
    }
  })
}
function viewSummary() {
  const ctx = readExecutionContext()
  if (ctx?.localPlan) {
    const record = saveLocalPlanRecord(ctx)
    uni.navigateTo({ url: `/pages/workout/detail?id=${record.id}` })
    return
  }
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
      &.selected {
        border: 2rpx solid #22c55e;
      }
      .exercise-info {
        flex: 1;
        min-width: 0;
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
        .exercise-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 8rpx;
          margin-top: 12rpx;
          .exercise-tag {
            font-size: 20rpx;
            font-weight: 500;
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
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
      .exercise-actions {
        display: flex;
        flex-direction: column;
        gap: 12rpx;
        margin-left: 16rpx;
        .mini-btn {
          min-width: 104rpx;
          height: 52rpx;
          border-radius: 14rpx;
          background: #f1f5f9;
          color: #334155;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24rpx;
          font-weight: 700;
          &.primary {
            background: #22c55e;
            color: white;
          }
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
  gap: 16rpx;
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
    &.settle {
      background: #0f172a;
      color: white;
    }
    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }
  }
}
</style>
