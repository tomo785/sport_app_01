<template>
  <view class="container">
    <TaskFlow 
      ref="taskFlowRef"
      :goal="currentGoal"
      :tasks="taskList"
      :dates="dateList"
      :workoutDates="workoutDates"
      @select="onTaskSelect"
      @start="onTaskStart"
      @continue="onTaskContinue"
      @reorder="onTaskReorder"
      @date-change="onDateChange"
    />

    <!-- 近期运动打卡 -->
    <view class="workout-section" v-if="recentWorkouts.length > 0">
      <view class="workout-header">
        <text class="workout-title">近期打卡</text>
        <text class="workout-more" @click="goToStats">查看更多 ›</text>
      </view>
      <view class="workout-list">
        <view 
          class="workout-item" 
          v-for="(item, index) in recentWorkouts" 
          :key="index"
          @click="goToWorkoutDetail(item.id)"
        >
          <text class="workout-icon">{{ getWorkoutTypeIcon(item.type) }}</text>
          <view class="workout-info">
            <text class="workout-name">{{ getWorkoutTypeName(item.type) }}</text>
            <text class="workout-time">{{ formatWorkoutTime(item.startTimeStr || item.startTime) }}</text>
          </view>
          <view class="workout-data">
            <text class="workout-duration">{{ item.duration ? Math.floor(item.duration / 60) + '分钟' : '-' }}</text>
            <text class="workout-calories" v-if="item.calories">{{ item.calories }}千卡</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import TaskFlow from '../../components/TaskFlow.vue'
import { getTaskFlow, getTaskFlowByDate, updateTaskOrder, startTask } from '../../api/task'
import { getWorkoutList } from '../../api/workout'

const taskFlowRef = ref(null)

// 当前目标
const currentGoal = ref({
  id: 1,
  title: '增肌塑形计划',
  description: '12周增肌训练，提升力量与体型',
  targetProgress: 100
})

// 任务列表
const taskList = ref([])

// 日期列表
const dateList = ref([])

// 当前选中的日期
const currentDate = ref('')

// 运动打卡日期列表
const workoutDates = ref([])

// 近期运动记录
const recentWorkouts = ref([])

onLoad((options) => {
  currentDate.value = options.date || getTodayDate()
  initDateList()
  loadTaskFlow()
  loadWorkoutRecords()
})

function getTodayDate() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function initDateList() {
  const dates = []
  const today = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  
  for (let i = -7; i <= 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    dates.push({
      week: i === 0 ? '今天' : '周' + weekDays[date.getDay()],
      day: date.getDate(),
      isToday: i === 0,
      hasTask: true,
      fullDate: dateStr
    })
  }
  dateList.value = dates
}

async function loadTaskFlow() {
  uni.showLoading({ title: '加载中', mask: true })
  try {
    const res = await getTaskFlow(currentDate.value)
    
    if (res.code === 200 && res.data) {
      if (res.data.goal) {
        currentGoal.value = res.data.goal
      }
      if (res.data.tasks) {
        taskList.value = res.data.tasks.sort((a, b) => a.order - b.order)
      }
    }
  } catch (e) {
    console.error('加载任务流程失败:', e)
    // 使用默认数据
    taskList.value = getDefaultTasks()
  } finally {
    uni.hideLoading()
  }
}

function getDefaultTasks() {
  return [
    {
      id: 1,
      name: '热身运动',
      type: 'warmup',
      priority: 'high',
      status: 2,
      duration: 10,
      sets: 1,
      calories: 50,
      order: 1
    },
    {
      id: 2,
      name: '杠铃深蹲',
      type: 'strength',
      priority: 'high',
      status: 1,
      duration: 15,
      sets: 4,
      calories: 120,
      order: 2
    },
    {
      id: 3,
      name: '哑铃卧推',
      type: 'strength',
      priority: 'medium',
      status: 0,
      duration: 12,
      sets: 3,
      calories: 80,
      order: 3
    },
    {
      id: 4,
      name: '平板支撑',
      type: 'core',
      priority: 'medium',
      status: 0,
      duration: 5,
      sets: 3,
      calories: 30,
      order: 4
    },
    {
      id: 5,
      name: '拉伸放松',
      type: 'stretch',
      priority: 'low',
      status: 0,
      duration: 8,
      sets: 1,
      calories: 20,
      order: 5
    }
  ]
}

// 事件处理
function onTaskSelect(task) {
  console.log('选中任务:', task)
}

async function onTaskStart(task) {
  try {
    await startTask(task.id)
    uni.showToast({ title: '开始训练', icon: 'success' })
    
    // 更新本地状态
    const index = taskList.value.findIndex(t => t.id === task.id)
    if (index !== -1) {
      taskList.value[index].status = 1
    }
    
    // 跳转到动作详情
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/task/exercise-detail?id=${task.id}`
      })
    }, 500)
  } catch (e) {
    uni.showToast({ title: '开始失败', icon: 'none' })
  }
}

function onTaskContinue(task) {
  uni.navigateTo({
    url: `/pages/task/exercise-detail?id=${task.id}`
  })
}

async function onTaskReorder(tasks) {
  taskList.value = tasks
  
  try {
    // 调用API更新顺序
    const orderData = tasks.map((task, index) => ({
      taskId: task.id,
      order: index + 1
    }))
    await updateTaskOrder(orderData)
  } catch (e) {
    console.error('更新任务顺序失败:', e)
  }
}

async function onDateChange(date) {
  currentDate.value = date.fullDate
  await loadTaskFlow()
}

// 加载运动打卡记录
async function loadWorkoutRecords() {
  try {
    // 查询最近30天的运动记录
    const endDate = getTodayDate()
    const startDate = getDateDaysAgo(30)
    const res = await getWorkoutList({ page: 1, size: 100, startDate, endDate })
    
    if (res.code === 200 && res.data) {
      const list = res.data.list || []
      recentWorkouts.value = list.slice(0, 5)
      
      // 提取有运动的日期
      const dates = new Set()
      list.forEach(item => {
        if (item.startTimeStr) {
          const dateStr = item.startTimeStr.split(' ')[0]
          dates.add(dateStr)
        } else if (item.startTime) {
          const dateStr = item.startTime.substring(0, 10)
          dates.add(dateStr)
        }
      })
      workoutDates.value = Array.from(dates)
    }
  } catch (e) {
    console.error('加载运动记录失败:', e)
  }
}

function getDateDaysAgo(days) {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function formatWorkoutTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.substring(0, 16).replace('T', ' ')
}

function getWorkoutTypeName(type) {
  const map = { 1: '跑步', 2: '健走', 3: '骑行' }
  return map[type] || '运动'
}

function getWorkoutTypeIcon(type) {
  const map = { 1: '🏃', 2: '🚶', 3: '🚴' }
  return map[type] || '🏃'
}

function goToStats() {
  uni.switchTab({ url: '/pages/stats/stats' })
}

function goToWorkoutDetail(id) {
  uni.navigateTo({ url: `/pages/workout/detail?id=${id}` })
}

// 页面显示时刷新
function refresh() {
  loadTaskFlow()
}

defineExpose({
  refresh
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
  padding-bottom: 40rpx;
}

/* 让 TaskFlow 组件占据剩余空间 */
:deep(.task-flow-container) {
  flex: 1;
  min-height: 0;
  height: auto !important;
}

.workout-section {
  margin: 20rpx 30rpx 0;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.workout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.workout-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1c1c1e;
}

.workout-more {
  font-size: 24rpx;
  color: #3b82f6;
}

.workout-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.workout-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 16rpx;

  &:active {
    background: #f1f5f9;
  }
}

.workout-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  margin-right: 20rpx;
}

.workout-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.workout-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1c1c1e;
}

.workout-time {
  font-size: 22rpx;
  color: #64748b;
}

.workout-data {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.workout-duration {
  font-size: 28rpx;
  font-weight: 600;
  color: #3b82f6;
}

.workout-calories {
  font-size: 22rpx;
  color: #64748b;
}

.navigate-btn {
  margin: 20rpx 30rpx 0;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  color: #3b82f6;
  font-size: 28rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  &:active {
    background: #f8fafc;
  }
}
</style>
