<template>
  <view class="task-flow-container">
    <!-- 顶部标题栏 - 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-status-bar"></view>
      <view class="nav-content">
        <view class="nav-left">
          <text class="nav-title">任务卡片</text>
        </view>
        <view class="nav-right">
          <view class="stat-badge">
            <text class="stat-num">{{ completedCount }}</text>
            <text class="stat-total">/{{ taskList.length }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 日历式日期选择器（默认只显示一周） -->
    <view class="calendar-header">
      <view class="calendar-title">
        <text class="current-month">{{ isCalendarExpanded ? currentYearMonth : currentWeekRange }}</text>
        <view class="expand-btn" @click="toggleCalendarExpand">
          <text class="expand-icon" :class="{ 'expanded': isCalendarExpanded }">▼</text>
        </view>
      </view>
      
      <!-- 星期标题 -->
      <view class="week-header">
        <text 
          class="week-day-title" 
          v-for="(day, index) in weekDays" 
          :key="index"
          :class="{ 'weekend': index === 0 || index === 6 }"
        >{{ day }}</text>
      </view>
      
      <!-- 默认只显示本周（7天） -->
      <view class="week-grid" v-if="!isCalendarExpanded">
        <view 
          class="week-day" 
          v-for="(day, index) in currentWeekDays" 
          :key="index"
          :class="{ 
            'active': isSelectedDay(day),
            'today': day.isToday,
            'has-task': day.hasTask,
            'weekend': day.weekDay === 0 || day.weekDay === 6
          }"
          @click="selectCalendarDay(day)"
        >
          <text class="day-number">{{ day.day }}</text>
          <view class="day-dot" v-if="day.hasTask"></view>
        </view>
      </view>
      
      <!-- 展开后显示完整月历 -->
      <view class="calendar-grid" v-else>
        <view 
          class="calendar-day" 
          v-for="(day, index) in calendarDays" 
          :key="index"
          :class="{ 
            'active': isSelectedDay(day),
            'today': day.isToday,
            'other-month': !day.isCurrentMonth,
            'has-task': day.hasTask,
            'weekend': day.weekDay === 0 || day.weekDay === 6
          }"
          @click="selectCalendarDay(day)"
        >
          <text class="day-number">{{ day.day }}</text>
          <view class="day-dot" v-if="day.hasTask"></view>
        </view>
      </view>
      
      <!-- 展开的月份选择器 -->
      <view class="month-picker" v-if="isCalendarExpanded">
        <view class="month-picker-header">
          <text class="year-nav" @click="changeYear(-1)">‹</text>
          <text class="picker-year">{{ pickerYear }}年</text>
          <text class="year-nav" @click="changeYear(1)">›</text>
        </view>
        <view class="month-grid">
          <view 
            class="month-item" 
            v-for="month in 12" 
            :key="month"
            :class="{ 
              'active': isPickerMonthActive(month),
              'current': isCurrentMonth(month)
            }"
            @click="selectMonth(month)"
          >
            <text class="month-name">{{ month }}月</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 流程图区域 -->
    <scroll-view 
      class="flow-content" 
      scroll-y 
      :scroll-top="scrollTop"
      @scroll="onScroll"
    >
      <view class="flow-list">
        <!-- 目标卡片 -->
        <view class="goal-node">
          <view class="goal-card">
            <view class="goal-icon">🎯</view>
            <view class="goal-info">
              <text class="goal-title">{{ currentGoal?.title || '目标' }}</text>
              <text class="goal-desc">{{ currentGoal?.description || '坚持训练，达成目标' }}</text>
            </view>
            <view class="goal-progress">
              <text class="progress-text">{{ totalProgress }}%</text>
              <view class="progress-mini">
                <view class="progress-mini-fill" :style="{ width: totalProgress + '%' }"></view>
              </view>
            </view>
          </view>
        </view>

        <!-- 任务链 -->
        <view class="task-chain">
          <view 
            class="task-node-wrapper"
            v-for="(task, index) in sortedTasks" 
            :key="task.id"
            :class="{ 
              'selected': selectedTaskId === task.id,
              'dragging': draggingIndex === index,
              'completed': task.status === 2
            }"
            :style="getDragStyle(index)"
            @click="selectTask(task)"
            @longpress="startDrag(index, $event)"
            @touchmove="onDragMove"
            @touchend="endDrag"
            @touchcancel="endDrag"
          >
            <!-- 连接线 -->
            <view class="connector" v-if="index > 0">
              <view class="connector-line" :class="{ 'active': task.status === 2 }"></view>
              <view class="connector-dot" :class="{ 'completed': task.status === 2 }"></view>
            </view>

            <!-- 任务卡片 -->
            <view class="task-node">
              <!-- 优先级标签 -->
              <view class="priority-tag" :class="'priority-' + task.priority">
                {{ getPriorityText(task.priority) }}
              </view>

              <!-- 拖拽手柄 -->
              <view class="drag-handle" @longpress.stop="startDrag(index, $event)">
                <view class="drag-lines">
                  <view class="drag-line"></view>
                  <view class="drag-line"></view>
                  <view class="drag-line"></view>
                </view>
              </view>

              <!-- 任务内容 -->
              <view class="task-content">
                <view class="task-main">
                  <view class="task-icon-wrapper" :class="'icon-bg-' + task.type">
                    <text class="task-icon">{{ getTaskIcon(task.type) }}</text>
                  </view>
                  <view class="task-info">
                    <text class="task-name">{{ task.name }}</text>
                    <text class="task-duration">{{ formatDuration(task.duration) }}</text>
                  </view>
                </view>

                <view class="task-detail">
                  <view class="detail-item" v-if="task.distance">
                    <text class="detail-label">距离</text>
                    <text class="detail-value">{{ task.distance }}</text>
                  </view>
                  <view class="detail-item" v-if="task.sets">
                    <text class="detail-label">{{ task.distance ? '组数' : '组数' }}</text>
                    <text class="detail-value">{{ task.sets }}组</text>
                  </view>
                  <view class="detail-item" v-if="task.calories">
                    <text class="detail-label">消耗</text>
                    <text class="detail-value">{{ task.calories }}卡</text>
                  </view>
                </view>

                <!-- 状态指示器 -->
                <view class="task-status" :class="'status-' + task.status">
                  <text class="status-icon">{{ getStatusIcon(task.status) }}</text>
                  <text class="status-text">{{ getStatusText(task.status) }}</text>
                </view>
              </view>

              <!-- 完成遮罩 -->
              <view class="completed-overlay" v-if="task.status === 2">
                <text class="completed-icon">✓</text>
              </view>
            </view>

            <!-- 顺序序号 -->
            <view class="order-badge">{{ index + 1 }}</view>
          </view>
        </view>

        <!-- 补水提醒卡片 -->
        <view class="water-card" v-if="currentPlanType === 'running5k' || currentPlanType === 'running10k'" @click="showWaterCard">
          <view class="water-icon">💧</view>
          <view class="water-info">
            <text class="water-title">补水提醒</text>
            <text class="water-subtitle">保持水分，提升运动表现</text>
          </view>
          <view class="water-progress">
            <view class="water-circle">
              <text class="water-amount">{{ waterIntake }}</text>
              <text class="water-unit">ml</text>
            </view>
          </view>
        </view>

        <!-- 补水详情弹窗 -->
        <view class="water-popup" v-if="showWaterReminder" @click="hideWaterCard">
          <view class="water-popup-content" @click.stop>
            <view class="water-popup-header">
              <text class="water-popup-title">💧 补水记录</text>
              <text class="water-popup-close" @click="hideWaterCard">✕</text>
            </view>
            <view class="water-popup-body">
              <view class="water-current">
                <text class="water-current-label">今日已饮水</text>
                <text class="water-current-value">{{ waterIntake }} ml</text>
              </view>
              <view class="water-target">
                <text class="water-target-label">目标: 800ml</text>
                <view class="water-progress-bar">
                  <view class="water-progress-fill" :style="{ width: Math.min(waterIntake / 800 * 100, 100) + '%' }"></view>
                </view>
              </view>
              <view class="water-buttons">
                <view class="water-btn" @click="addWater(150)">
                  <text class="water-btn-icon">🥤</text>
                  <text class="water-btn-text">+150ml</text>
                </view>
                <view class="water-btn" @click="addWater(250)">
                  <text class="water-btn-icon">🥛</text>
                  <text class="water-btn-text">+250ml</text>
                </view>
                <view class="water-btn" @click="addWater(500)">
                  <text class="water-btn-icon">🧴</text>
                  <text class="water-btn-text">+500ml</text>
                </view>
              </view>
            </view>
            <view class="water-popup-footer">
              <text class="water-reset" @click="resetWater">重置</text>
            </view>
          </view>
        </view>

      </view>

      <!-- 底部留白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 结束节点（悬浮于任务栏之上） -->
    <view class="end-node">
      <view class="end-card">
        <text class="end-text">今日训练完成</text>
        <view class="end-icon">🏆</view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="flow-footer" v-if="selectedTaskId">
      <view class="footer-info">
        <text class="selected-name">{{ selectedTask?.name }}</text>
        <text class="selected-status">{{ getStatusText(selectedTask?.status) }}</text>
      </view>
      <view class="footer-actions">
        <button 
          class="action-btn start-btn" 
          v-if="selectedTask?.status === 0"
          @click="startTask(selectedTask)"
        >开始</button>
        <button 
          class="action-btn continue-btn" 
          v-else-if="selectedTask?.status === 1"
          @click="continueTask(selectedTask)"
        >继续</button>
        <button 
          class="action-btn done-btn" 
          v-else
          @click="viewTaskDetail(selectedTask)"
        >查看</button>
      </view>
    </view>

    <!-- 拖拽提示 -->
    <view class="drag-hint" v-if="isDragging">
      <text class="hint-text">拖动调整顺序</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  goal: {
    type: Object,
    default: () => ({
      id: 1,
      title: '增肌塑形计划',
      description: '12周增肌训练，提升力量与体型',
      targetProgress: 100
    })
  },
  tasks: {
    type: Array,
    default: () => []
  },
  dates: {
    type: Array,
    default: () => []
  },
  workoutDates: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['select', 'start', 'continue', 'reorder', 'date-change'])

// ==================== 状态管理 ====================
const selectedDate = ref(new Date())
const selectedTaskId = ref(null)
const isDragging = ref(false)
const draggingIndex = ref(-1)
const dragStartY = ref(0)
const dragCurrentY = ref(0)
const scrollTop = ref(0)

// 训练计划类型：'strength' | 'running5k' | 'hiit' | 'yoga' | 'core' | 'running10k'
const currentPlanType = ref('strength')

// 日历相关状态
const currentCalendarDate = ref(new Date())
const isCalendarExpanded = ref(false)
const pickerYear = ref(new Date().getFullYear())
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 补水提醒状态
const showWaterReminder = ref(false)
const waterIntake = ref(0) // 已饮水量(ml)

// ==================== 数据计算 ====================
const currentGoal = computed(() => {
  const goals = {
    strength: { title: '增肌塑形计划', description: '12周增肌训练，提升力量与体型', targetProgress: 100 },
    running5k: { title: '5公里破速计划', description: '提升心肺耐力，挑战个人PB', targetProgress: 100 },
    hiit: { title: '高效燃脂计划', description: '20分钟快速燃烧脂肪，提升代谢', targetProgress: 100 },
    yoga: { title: '柔韧平衡计划', description: '舒展身心，改善体态与睡眠质量', targetProgress: 100 },
    core: { title: '核心力量计划', description: '打造坚实腰腹，提升运动稳定性', targetProgress: 100 },
    running10k: { title: '10公里耐力计划', description: '建立有氧基础，完成人生首个10公里', targetProgress: 100 }
  }
  return goals[currentPlanType.value] || props.goal
})

const taskList = computed(() => props.tasks.length > 0 ? props.tasks : getDefaultTasks())

const sortedTasks = computed(() => taskList.value)

const completedCount = computed(() => {
  return taskList.value.filter(t => t.status === 2).length
})

const totalProgress = computed(() => {
  if (taskList.value.length === 0) return 0
  return Math.round((completedCount.value / taskList.value.length) * 100)
})

const selectedTask = computed(() => {
  return taskList.value.find(t => t.id === selectedTaskId.value)
})

// 日历计算属性
const currentYearMonth = computed(() => {
  const year = currentCalendarDate.value.getFullYear()
  const month = currentCalendarDate.value.getMonth() + 1
  return `${year}年${month}月`
})

// 本周范围文字（如：4月6日-4月12日）
const currentWeekRange = computed(() => {
  const weekDays = getWeekDays(new Date())
  if (weekDays.length === 0) return ''
  const firstDay = weekDays[0].fullDate
  const lastDay = weekDays[6].fullDate
  
  const formatDay = (date) => `${date.getMonth() + 1}月${date.getDate()}日`
  return `${formatDay(firstDay)}-${formatDay(lastDay)}`
})

// 本周7天
const currentWeekDays = computed(() => {
  return getWeekDays(new Date())
})

const calendarDays = computed(() => {
  return generateCalendarDays(currentCalendarDate.value)
})

// ==================== 日历方法 ====================
function getWeekDays(baseDate) {
  const days = []
  const today = new Date()
  
  // 获取本周第一天（周日）
  const dayOfWeek = baseDate.getDay()
  const firstDayOfWeek = new Date(baseDate)
  firstDayOfWeek.setDate(baseDate.getDate() - dayOfWeek)
  
  for (let i = 0; i < 7; i++) {
    const currentDate = new Date(firstDayOfWeek)
    currentDate.setDate(firstDayOfWeek.getDate() + i)
    
    const isToday = today.getDate() === currentDate.getDate() && 
                    today.getMonth() === currentDate.getMonth() && 
                    today.getFullYear() === currentDate.getFullYear()
    
    const dateStr = formatDate(currentDate)
    days.push({
      day: currentDate.getDate(),
      weekDay: i, // 0=周日, 1=周一...
      isToday,
      hasTask: props.workoutDates.includes(dateStr),
      fullDate: currentDate
    })
  }
  
  return days
}

function generateCalendarDays(date) {
  const year = date.getFullYear()
  const month = date.getMonth()
  
  // 当月第一天
  const firstDay = new Date(year, month, 1)
  // 当月最后一天
  const lastDay = new Date(year, month + 1, 0)
  
  // 获取第一天是星期几（0=周日，1=周一...）
  const firstDayWeek = firstDay.getDay()
  
  const days = []
  const today = new Date()
  
  // 上个月的日期（补全第一行）
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const prevDateStr = formatDate(new Date(year, month - 1, day))
    days.push({
      day,
      weekDay: (firstDayWeek - i - 1 + 7) % 7,
      isCurrentMonth: false,
      isToday: false,
      hasTask: props.workoutDates.includes(prevDateStr),
      fullDate: new Date(year, month - 1, day)
    })
  }
  
  // 当月的日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const currentDate = new Date(year, month, i)
    const isToday = today.getDate() === i && 
                    today.getMonth() === month && 
                    today.getFullYear() === year
    
    const currentDateStr = formatDate(currentDate)
    days.push({
      day: i,
      weekDay: (firstDayWeek + i - 1) % 7,
      isCurrentMonth: true,
      isToday,
      hasTask: props.workoutDates.includes(currentDateStr),
      fullDate: currentDate
    })
  }
  
  // 下个月的日期（补全最后一行，凑成完整的6行或至少显示完整）
  const remainingDays = (7 - (days.length % 7)) % 7
  for (let i = 1; i <= remainingDays; i++) {
    const nextDateStr = formatDate(new Date(year, month + 1, i))
    days.push({
      day: i,
      weekDay: (firstDayWeek + lastDay.getDate() + i - 1) % 7,
      isCurrentMonth: false,
      isToday: false,
      hasTask: props.workoutDates.includes(nextDateStr),
      fullDate: new Date(year, month + 1, i)
    })
  }
  
  return days
}

function isSelectedDay(day) {
  return selectedDate.value && 
         selectedDate.value.getDate() === day.day &&
         selectedDate.value.getMonth() === day.fullDate.getMonth() &&
         selectedDate.value.getFullYear() === day.fullDate.getFullYear()
}

function selectCalendarDay(day) {
  selectedDate.value = day.fullDate
  // 如果点击的是其他月份的日期，切换日历显示
  if (!day.isCurrentMonth) {
    currentCalendarDate.value = new Date(day.fullDate)
  }
  emit('date-change', {
    fullDate: formatDate(day.fullDate),
    date: day.fullDate
  })
}

function toggleCalendarExpand() {
  isCalendarExpanded.value = !isCalendarExpanded.value
  if (isCalendarExpanded.value) {
    pickerYear.value = currentCalendarDate.value.getFullYear()
  }
}

function changeYear(delta) {
  pickerYear.value += delta
}

function isPickerMonthActive(month) {
  return pickerYear.value === currentCalendarDate.value.getFullYear() &&
         month - 1 === currentCalendarDate.value.getMonth()
}

function isCurrentMonth(month) {
  const now = new Date()
  return pickerYear.value === now.getFullYear() && month - 1 === now.getMonth()
}

function selectMonth(month) {
  const newDate = new Date(pickerYear.value, month - 1, 1)
  currentCalendarDate.value = newDate
  // 默认选中该月第一天
  selectedDate.value = newDate
  isCalendarExpanded.value = false
  emit('date-change', {
    fullDate: formatDate(newDate),
    date: newDate
  })
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// ==================== 原有方法 ====================
function getDefaultTasks() {
  const map = {
    running5k: getRunning5KPlan,
    hiit: getHIITPlan,
    yoga: getYogaPlan,
    core: getCorePlan,
    running10k: getRunning10KPlan
  }
  return (map[currentPlanType.value] || getStrengthPlan)()
}

// 力量训练计划
function getStrengthPlan() {
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

// 5km跑步训练计划
function getRunning5KPlan() {
  return [
    {
      id: 101,
      name: '热身慢跑',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 8,
      distance: '1km',
      sets: 1,
      calories: 60,
      order: 1,
      description: '轻松慢跑，逐渐提升心率'
    },
    {
      id: 102,
      name: '动态拉伸',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 20,
      order: 2,
      description: '高抬腿、踢臀跑、开合跳'
    },
    {
      id: 103,
      name: '800米间歇跑',
      type: 'cardio',
      priority: 'high',
      status: 0,
      duration: 25,
      distance: '3.2km',
      sets: 4,
      calories: 280,
      order: 3,
      description: '快跑800m + 慢跑400m恢复，重复4组'
    },
    {
      id: 104,
      name: '冷身慢跑',
      type: 'stretch',
      priority: 'medium',
      status: 0,
      duration: 6,
      distance: '0.8km',
      sets: 1,
      calories: 40,
      order: 4,
      description: '慢速恢复跑，让心率逐渐下降'
    },
    {
      id: 105,
      name: '结束拉伸',
      type: 'stretch',
      priority: 'medium',
      status: 0,
      duration: 6,
      sets: 1,
      calories: 15,
      order: 5,
      description: '腿部拉伸、髋部放松'
    }
  ]
}

// 燃脂HIIT训练计划
function getHIITPlan() {
  return [
    {
      id: 201,
      name: '关节激活',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 25,
      order: 1,
      description: '活动手腕、脚踝、髋关节，预防运动损伤'
    },
    {
      id: 202,
      name: '开合跳热身',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 3,
      sets: 1,
      calories: 30,
      order: 2,
      description: '快速激活全身肌群，提升心率'
    },
    {
      id: 203,
      name: '波比跳',
      type: 'cardio',
      priority: 'high',
      status: 0,
      duration: 4,
      sets: 4,
      reps: 15,
      calories: 120,
      order: 3,
      description: '全身爆发力训练，高效燃脂王牌动作'
    },
    {
      id: 204,
      name: '高抬腿冲刺',
      type: 'cardio',
      priority: 'high',
      status: 0,
      duration: 4,
      sets: 4,
      reps: 45,
      calories: 100,
      order: 4,
      description: '45秒全力高抬腿 + 15秒休息，重复4组'
    },
    {
      id: 205,
      name: '登山者',
      type: 'core',
      priority: 'medium',
      status: 0,
      duration: 3,
      sets: 4,
      reps: 30,
      calories: 80,
      order: 5,
      description: '模拟登山动作，燃脂同时雕刻腹肌线条'
    },
    {
      id: 206,
      name: '深蹲跳',
      type: 'strength',
      priority: 'medium',
      status: 0,
      duration: 3,
      sets: 3,
      reps: 20,
      calories: 90,
      order: 6,
      description: '下肢爆发力训练，提升心率至最高点'
    },
    {
      id: 207,
      name: '拉伸放松',
      type: 'stretch',
      priority: 'low',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 20,
      order: 7,
      description: '全身静态拉伸，让心跳逐渐平复'
    }
  ]
}

// 瑜伽拉伸训练计划
function getYogaPlan() {
  return [
    {
      id: 301,
      name: '冥想调息',
      type: 'rest',
      priority: 'high',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 10,
      order: 1,
      description: '盘腿静坐，专注呼吸，放松身心'
    },
    {
      id: 302,
      name: '颈部肩部拉伸',
      type: 'stretch',
      priority: 'high',
      status: 0,
      duration: 8,
      sets: 1,
      calories: 15,
      order: 2,
      description: '缓解久坐肩颈僵硬，改善血液循环'
    },
    {
      id: 303,
      name: '脊柱扭转',
      type: 'stretch',
      priority: 'medium',
      status: 0,
      duration: 10,
      sets: 2,
      calories: 20,
      order: 3,
      description: '坐姿与仰卧扭转，灵活脊柱各节段'
    },
    {
      id: 304,
      name: '下犬式流',
      type: 'stretch',
      priority: 'medium',
      status: 0,
      duration: 8,
      sets: 3,
      calories: 30,
      order: 4,
      description: '串联下犬式与上犬式，舒展背部与腿后侧'
    },
    {
      id: 305,
      name: '鸽子式开髋',
      type: 'stretch',
      priority: 'medium',
      status: 0,
      duration: 8,
      sets: 2,
      calories: 20,
      order: 5,
      description: '深度髋外旋拉伸，释放髋部紧张'
    },
    {
      id: 306,
      name: '婴儿式放松',
      type: 'rest',
      priority: 'low',
      status: 0,
      duration: 6,
      sets: 1,
      calories: 10,
      order: 6,
      description: '前额贴地，双臂前伸，完全放松全身'
    }
  ]
}

// 核心雕刻训练计划
function getCorePlan() {
  return [
    {
      id: 401,
      name: '腹肌激活',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 20,
      order: 1,
      description: '轻微卷腹与骨盆倾斜，唤醒核心肌群'
    },
    {
      id: 402,
      name: '卷腹',
      type: 'core',
      priority: 'high',
      status: 0,
      duration: 4,
      sets: 4,
      reps: 20,
      calories: 60,
      order: 2,
      description: '上腹经典动作，控制节奏避免颈部发力'
    },
    {
      id: 403,
      name: '俄罗斯转体',
      type: 'core',
      priority: 'medium',
      status: 0,
      duration: 4,
      sets: 4,
      reps: 30,
      calories: 70,
      order: 3,
      description: 'V字坐姿左右转体，雕刻腹外斜肌'
    },
    {
      id: 404,
      name: '仰卧抬腿',
      type: 'core',
      priority: 'medium',
      status: 0,
      duration: 3,
      sets: 3,
      reps: 15,
      calories: 50,
      order: 4,
      description: '强化下腹力量，保持腰部贴紧地面'
    },
    {
      id: 405,
      name: '侧平板支撑',
      type: 'core',
      priority: 'medium',
      status: 0,
      duration: 3,
      sets: 3,
      reps: 45,
      calories: 45,
      order: 5,
      description: '每侧45秒，强化腹斜肌与肩部稳定性'
    },
    {
      id: 406,
      name: '死虫式',
      type: 'core',
      priority: 'medium',
      status: 0,
      duration: 3,
      sets: 3,
      reps: 12,
      calories: 40,
      order: 6,
      description: '对侧手脚伸展，提升核心抗伸展能力'
    },
    {
      id: 407,
      name: '腹部拉伸',
      type: 'stretch',
      priority: 'low',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 15,
      order: 7,
      description: '眼镜蛇式拉伸，放松紧张的腹肌'
    }
  ]
}

// 10km耐力跑训练计划
function getRunning10KPlan() {
  return [
    {
      id: 501,
      name: '慢跑热身',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 10,
      distance: '1.5km',
      sets: 1,
      calories: 90,
      order: 1,
      description: '配速6:30-7:00，让身体进入跑步状态'
    },
    {
      id: 502,
      name: '动态拉伸',
      type: 'warmup',
      priority: 'high',
      status: 0,
      duration: 5,
      sets: 1,
      calories: 20,
      order: 2,
      description: '弓步走、腿部摆动，激活髋膝踝'
    },
    {
      id: 503,
      name: '节奏跑',
      type: 'cardio',
      priority: 'high',
      status: 0,
      duration: 35,
      distance: '6km',
      sets: 1,
      calories: 420,
      order: 3,
      description: '保持有氧阈值配速，建立耐力基础'
    },
    {
      id: 504,
      name: '补给与恢复跑',
      type: 'cardio',
      priority: 'medium',
      status: 0,
      duration: 15,
      distance: '2km',
      sets: 1,
      calories: 120,
      order: 4,
      description: '放慢配速，补充水分，调整呼吸'
    },
    {
      id: 505,
      name: '冷身走',
      type: 'stretch',
      priority: 'low',
      status: 0,
      duration: 5,
      distance: '0.5km',
      sets: 1,
      calories: 30,
      order: 5,
      description: '步行降低心率，避免突然停下'
    },
    {
      id: 506,
      name: '腿部拉伸',
      type: 'stretch',
      priority: 'medium',
      status: 0,
      duration: 8,
      sets: 1,
      calories: 20,
      order: 6,
      description: '重点拉伸大腿前后侧与小腿肌群'
    }
  ]
}

// 切换训练计划
function switchPlanType(type) {
  currentPlanType.value = type
  // 重置选中状态
  selectedTaskId.value = null
}

// 补水相关方法
function showWaterCard() {
  showWaterReminder.value = true
}

function hideWaterCard() {
  showWaterReminder.value = false
}

function addWater(amount) {
  waterIntake.value += amount
}

function resetWater() {
  waterIntake.value = 0
}

// 已弃用，改为日历式展示
function generateWeekDates() {
  return []
}

function getPriorityText(priority) {
  const map = { high: '高', medium: '中', low: '低' }
  return map[priority] || '中'
}

function getTaskIcon(type) {
  const map = {
    warmup: '🔥',
    strength: '💪',
    cardio: '❤️',
    core: '🎯',
    stretch: '🧘',
    rest: '😴'
  }
  return map[type] || '📋'
}

function getStatusIcon(status) {
  const map = { 0: '○', 1: '▶', 2: '✓' }
  return map[status] || '○'
}

function getStatusText(status) {
  const map = { 0: '未开始', 1: '进行中', 2: '已完成' }
  return map[status] || '未开始'
}

function formatDuration(minutes) {
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分` : `${hours}小时`
}

function getDragStyle(index) {
  if (draggingIndex.value !== index) return {}
  const offset = dragCurrentY.value - dragStartY.value
  return {
    transform: `translateY(${offset}px)`,
    zIndex: 100,
    opacity: 0.9
  }
}

// ==================== 事件处理 ====================

function selectTask(task) {
  selectedTaskId.value = task.id
  emit('select', task)
}

function startTask(task) {
  emit('start', task)
}

function continueTask(task) {
  emit('continue', task)
}

function viewTaskDetail(task) {
  uni.navigateTo({
    url: `/pages/task/exercise-detail?id=${task.id}`
  })
}

// ==================== 拖拽排序 ====================
function startDrag(index, event) {
  draggingIndex.value = index
  isDragging.value = true
  dragStartY.value = event.touches ? event.touches[0].clientY : event.clientY
  dragCurrentY.value = dragStartY.value
  
  // 震动反馈
  uni.vibrateShort()
}

function onDragMove(event) {
  if (!isDragging.value || draggingIndex.value === -1) return
  
  const clientY = event.touches ? event.touches[0].clientY : event.clientY
  dragCurrentY.value = clientY
  
  // 计算需要交换的位置
  const itemHeight = 100 // 卡片高度估算
  const offset = clientY - dragStartY.value
  const moveIndex = Math.round(offset / itemHeight)
  
  if (moveIndex !== 0 && draggingIndex.value + moveIndex >= 0 && draggingIndex.value + moveIndex < sortedTasks.value.length) {
    const newIndex = draggingIndex.value + moveIndex
    // 交换位置
    const tasks = [...sortedTasks.value]
    const temp = tasks[draggingIndex.value]
    tasks[draggingIndex.value] = tasks[newIndex]
    tasks[newIndex] = temp
    
    // 更新order并触发重排
    tasks.forEach((task, i) => {
      task.order = i + 1
    })
    
    emit('reorder', tasks)
    draggingIndex.value = newIndex
    dragStartY.value = clientY
  }
}

function endDrag() {
  if (isDragging.value) {
    isDragging.value = false
    draggingIndex.value = -1
    dragStartY.value = 0
    dragCurrentY.value = 0
  }
}

function onScroll(e) {
  scrollTop.value = e.detail.scrollTop
}

// ==================== 暴露方法 ====================
function refresh() {
  selectedTaskId.value = null
}

defineExpose({
  refresh
})
</script>

<style lang="scss" scoped>
.task-flow-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

// ==================== 自定义导航栏 ====================
.custom-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);

  // 状态栏占位（适配刘海屏）
  .nav-status-bar {
    height: var(--status-bar-height, 44rpx);
  }

  .nav-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 30rpx;
    height: 88rpx;

    .nav-left {
      .nav-title {
        font-size: 36rpx;
        font-weight: 700;
        color: #1c1c1e;
      }
    }

    .nav-right {
      .stat-badge {
        background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
        padding: 12rpx 24rpx;
        border-radius: 24rpx;
        display: flex;
        align-items: baseline;

        .stat-num {
          font-size: 32rpx;
          font-weight: 700;
          color: white;
        }

        .stat-total {
          font-size: 20rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }
}

// ==================== 训练计划切换 ====================
// ==================== 日历式日期选择器 ====================
.calendar-header {
  background: white;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f1f5f9;

  .calendar-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .current-month {
      font-size: 32rpx;
      font-weight: 700;
      color: #1c1c1e;
    }

    .expand-btn {
      width: 56rpx;
      height: 56rpx;
      background: #f8fafc;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;

      &:active {
        background: #e2e8f0;
      }

      .expand-icon {
        font-size: 24rpx;
        color: #64748b;
        transition: transform 0.3s ease;

        &.expanded {
          transform: rotate(180deg);
        }
      }
    }
  }

  // 星期标题
  .week-header {
    display: flex;
    justify-content: space-around;
    margin-bottom: 16rpx;

    .week-day-title {
      width: 80rpx;
      text-align: center;
      font-size: 24rpx;
      color: #64748b;
      font-weight: 500;

      &.weekend {
        color: #f97316;
      }
    }
  }

  // 本周7天网格（默认显示）
  .week-grid {
    display: flex;
    justify-content: space-around;

    .week-day {
      width: 80rpx;
      height: 88rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: 20rpx;
      position: relative;
      transition: all 0.2s ease;

      &:active {
        transform: scale(0.95);
      }

      &.weekend {
        .day-number {
          color: #f97316;
        }
      }

      &.today {
        background: rgba(59, 130, 246, 0.1);

        .day-number {
          color: #3b82f6;
          font-weight: 700;
        }
      }

      &.active {
        background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);

        .day-number {
          color: white;
          font-weight: 700;
        }

        .day-dot {
          background: white;
        }
      }

      &.has-task:not(.active) {
        .day-dot {
          background: #3b82f6;
        }
      }

      .day-number {
        font-size: 32rpx;
        color: #1c1c1e;
        font-weight: 600;
      }

      .day-dot {
        position: absolute;
        bottom: 12rpx;
        width: 6rpx;
        height: 6rpx;
        background: transparent;
        border-radius: 50%;
      }
    }
  }

  // 日历网格（展开后显示）
  .calendar-grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-around;

    .calendar-day {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: 20rpx;
      margin-bottom: 12rpx;
      position: relative;
      transition: all 0.2s ease;

      &:active {
        transform: scale(0.95);
      }

      &.other-month {
        opacity: 0.4;
      }

      &.weekend {
        .day-number {
          color: #f97316;
        }
      }

      &.today {
        background: rgba(59, 130, 246, 0.1);

        .day-number {
          color: #3b82f6;
          font-weight: 700;
        }
      }

      &.active {
        background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);

        .day-number {
          color: white;
          font-weight: 700;
        }

        .day-dot {
          background: white;
        }
      }

      &.has-task:not(.active) {
        .day-dot {
          background: #3b82f6;
        }
      }

      .day-number {
        font-size: 28rpx;
        color: #1c1c1e;
        font-weight: 500;
      }

      .day-dot {
        position: absolute;
        bottom: 10rpx;
        width: 6rpx;
        height: 6rpx;
        background: transparent;
        border-radius: 50%;
      }
    }
  }

  // 月份选择器（展开状态）
  .month-picker {
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f1f5f9;
    animation: slideDown 0.3s ease;

    @keyframes slideDown {
      from {
        opacity: 0;
        transform: translateY(-20rpx);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .month-picker-header {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 40rpx;
      margin-bottom: 20rpx;

      .year-nav {
        font-size: 40rpx;
        color: #64748b;
        padding: 10rpx 20rpx;
        font-weight: 300;

        &:active {
          color: #3b82f6;
        }
      }

      .picker-year {
        font-size: 32rpx;
        font-weight: 600;
        color: #1c1c1e;
      }
    }

    .month-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16rpx;

      .month-item {
        height: 80rpx;
        background: #f8fafc;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;

        &:active {
          transform: scale(0.95);
        }

        &.active {
          background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);

          .month-name {
            color: white;
            font-weight: 600;
          }
        }

        &.current:not(.active) {
          background: rgba(59, 130, 246, 0.1);

          .month-name {
            color: #3b82f6;
          }
        }

        .month-name {
          font-size: 26rpx;
          color: #64748b;
        }
      }
    }
  }
}

// ==================== 流程图内容 ====================
.flow-content {
  flex: 1;
  padding: 30rpx;

  .flow-list {
    position: relative;
  }

  .bottom-spacer {
    height: calc(220rpx + env(safe-area-inset-bottom));
  }
}

// ==================== 目标节点 ====================
.goal-node {
  margin-bottom: 40rpx;

  .goal-card {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    border-radius: 24rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    gap: 20rpx;
    box-shadow: 0 8rpx 30rpx rgba(59, 130, 246, 0.3);

    .goal-icon {
      font-size: 48rpx;
    }

    .goal-info {
      flex: 1;

      .goal-title {
        font-size: 32rpx;
        font-weight: 700;
        color: white;
        display: block;
      }

      .goal-desc {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
        margin-top: 4rpx;
      }
    }

    .goal-progress {
      text-align: center;

      .progress-text {
        font-size: 36rpx;
        font-weight: 700;
        color: white;
        display: block;
      }

      .progress-mini {
        width: 80rpx;
        height: 8rpx;
        background: rgba(255, 255, 255, 0.3);
        border-radius: 4rpx;
        margin-top: 8rpx;
        overflow: hidden;

        .progress-mini-fill {
          height: 100%;
          background: white;
          border-radius: 4rpx;
          transition: width 0.3s ease;
        }
      }
    }
  }
}

// ==================== 任务链 ====================
.task-chain {
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 50rpx;
    top: 0;
    bottom: 0;
    width: 4rpx;
    background: linear-gradient(180deg, #e2e8f0 0%, #cbd5e1 100%);
    z-index: 0;
  }
}

.task-node-wrapper {
  position: relative;
  margin-bottom: 30rpx;
  transition: all 0.3s ease;

  &.selected {
    .task-node {
      border-color: #3b82f6;
      box-shadow: 0 8rpx 30rpx rgba(59, 130, 246, 0.2);
      transform: scale(1.02);
    }
  }

  &.dragging {
    .task-node {
      box-shadow: 0 20rpx 50rpx rgba(0, 0, 0, 0.15);
    }
  }

  &.completed {
    .task-node {
      opacity: 0.8;
    }
  }
}

// 连接线
.connector {
  position: absolute;
  left: 50rpx;
  top: -30rpx;
  width: 4rpx;
  height: 30rpx;
  z-index: 1;

  .connector-line {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #e2e8f0;
    transition: all 0.3s ease;

    &.active {
      background: linear-gradient(180deg, #10b981 0%, #059669 100%);
    }
  }

  .connector-dot {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 16rpx;
    height: 16rpx;
    background: white;
    border: 4rpx solid #e2e8f0;
    border-radius: 50%;
    transition: all 0.3s ease;

    &.completed {
      background: #10b981;
      border-color: #10b981;
    }
  }
}

// 任务卡片
.task-node {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-left: 90rpx;
  border: 2rpx solid transparent;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
  position: relative;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
  }

  // 优先级标签
  .priority-tag {
    position: absolute;
    top: 16rpx;
    right: 16rpx;
    padding: 6rpx 16rpx;
    border-radius: 12rpx;
    font-size: 20rpx;
    font-weight: 600;

    &.priority-high {
      background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
      color: #dc2626;
    }

    &.priority-medium {
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      color: #d97706;
    }

    &.priority-low {
      background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
      color: #059669;
    }
  }

  // 拖拽手柄
  .drag-handle {
    position: absolute;
    left: -60rpx;
    top: 50%;
    transform: translateY(-50%);
    width: 50rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0.5;

    .drag-lines {
      display: flex;
      flex-direction: column;
      gap: 6rpx;

      .drag-line {
        width: 24rpx;
        height: 4rpx;
        background: #94a3b8;
        border-radius: 2rpx;
      }
    }
  }

  // 任务内容
  .task-content {
    .task-main {
      display: flex;
      align-items: center;
      gap: 20rpx;
      margin-bottom: 16rpx;

      .task-icon-wrapper {
        width: 72rpx;
        height: 72rpx;
        border-radius: 18rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        &.icon-bg-warmup {
          background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        }

        &.icon-bg-strength {
          background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
        }

        &.icon-bg-cardio {
          background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
        }

        &.icon-bg-core {
          background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
        }

        &.icon-bg-stretch {
          background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
        }

        &.icon-bg-rest {
          background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
        }

        .task-icon {
          font-size: 36rpx;
        }
      }

      .task-info {
        flex: 1;

        .task-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #1c1c1e;
          display: block;
        }

        .task-duration {
          font-size: 24rpx;
          color: #64748b;
          margin-top: 4rpx;
        }
      }
    }

    .task-detail {
      display: flex;
      gap: 30rpx;
      padding: 16rpx;
      background: #f8fafc;
      border-radius: 12rpx;
      margin-bottom: 16rpx;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .detail-label {
          font-size: 22rpx;
          color: #94a3b8;
        }

        .detail-value {
          font-size: 24rpx;
          font-weight: 600;
          color: #1c1c1e;
        }
      }
    }

    .task-status {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding-top: 16rpx;
      border-top: 1rpx solid #f1f5f9;

      &.status-0 {
        .status-icon,
        .status-text {
          color: #94a3b8;
        }
      }

      &.status-1 {
        .status-icon,
        .status-text {
          color: #3b82f6;
        }
      }

      &.status-2 {
        .status-icon,
        .status-text {
          color: #10b981;
        }
      }

      .status-icon {
        font-size: 24rpx;
      }

      .status-text {
        font-size: 24rpx;
        font-weight: 500;
      }
    }
  }

  // 完成遮罩
  .completed-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .completed-icon {
      width: 64rpx;
      height: 64rpx;
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 32rpx;
      font-weight: 700;
    }
  }
}

// 顺序序号
.order-badge {
  position: absolute;
  left: 34rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 40rpx;
  height: 40rpx;
  background: white;
  border: 4rpx solid #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 600;
  color: #64748b;
  z-index: 2;
}

// ==================== 补水卡片 ====================
.water-card {
  margin: 20rpx 0;
  margin-left: 90rpx;
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 8rpx 30rpx rgba(14, 165, 233, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
  }

  .water-icon {
    font-size: 48rpx;
  }

  .water-info {
    flex: 1;

    .water-title {
      font-size: 32rpx;
      font-weight: 700;
      color: white;
      display: block;
    }

    .water-subtitle {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 4rpx;
    }
  }

  .water-progress {
    .water-circle {
      width: 80rpx;
      height: 80rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: 4rpx solid rgba(255, 255, 255, 0.4);

      .water-amount {
        font-size: 28rpx;
        font-weight: 700;
        color: white;
      }

      .water-unit {
        font-size: 18rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
}

// 补水弹窗
.water-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;

  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }

  .water-popup-content {
    width: 80%;
    background: white;
    border-radius: 32rpx;
    padding: 40rpx;
    animation: slideUp 0.3s ease;

    @keyframes slideUp {
      from { 
        opacity: 0;
        transform: translateY(50rpx);
      }
      to { 
        opacity: 1;
        transform: translateY(0);
      }
    }

    .water-popup-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;

      .water-popup-title {
        font-size: 36rpx;
        font-weight: 700;
        color: #1c1c1e;
      }

      .water-popup-close {
        font-size: 40rpx;
        color: #94a3b8;
        padding: 10rpx;
      }
    }

    .water-popup-body {
      .water-current {
        text-align: center;
        margin-bottom: 30rpx;

        .water-current-label {
          font-size: 26rpx;
          color: #64748b;
          display: block;
          margin-bottom: 10rpx;
        }

        .water-current-value {
          font-size: 56rpx;
          font-weight: 700;
          color: #0ea5e9;
        }
      }

      .water-target {
        margin-bottom: 40rpx;

        .water-target-label {
          font-size: 24rpx;
          color: #64748b;
          display: block;
          margin-bottom: 12rpx;
        }

        .water-progress-bar {
          height: 16rpx;
          background: #e2e8f0;
          border-radius: 8rpx;
          overflow: hidden;

          .water-progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #0ea5e9 0%, #06b6d4 100%);
            border-radius: 8rpx;
            transition: width 0.3s ease;
          }
        }
      }

      .water-buttons {
        display: flex;
        justify-content: space-around;
        gap: 20rpx;

        .water-btn {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 12rpx;
          padding: 24rpx;
          background: #f8fafc;
          border-radius: 20rpx;
          transition: all 0.2s ease;

          &:active {
            background: #e2e8f0;
            transform: scale(0.95);
          }

          .water-btn-icon {
            font-size: 48rpx;
          }

          .water-btn-text {
            font-size: 24rpx;
            font-weight: 600;
            color: #1c1c1e;
          }
        }
      }
    }

    .water-popup-footer {
      margin-top: 30rpx;
      text-align: center;

      .water-reset {
        font-size: 26rpx;
        color: #94a3b8;
        padding: 10rpx 30rpx;
      }
    }
  }
}

// ==================== 结束节点 ====================
.end-node {
  position: fixed;
  left: 90rpx;
  right: 30rpx;
  bottom: calc(120rpx + env(safe-area-inset-bottom));
  z-index: 100;

  .end-card {
    background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
    border-radius: 24rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    box-shadow: 0 8rpx 30rpx rgba(245, 158, 11, 0.3);

    .end-text {
      font-size: 28rpx;
      font-weight: 600;
      color: white;
    }

    .end-icon {
      font-size: 40rpx;
    }
  }
}

// ==================== 底部操作栏 ====================
.flow-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 20rpx 30rpx calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4rpx 30rpx rgba(0, 0, 0, 0.08);
  z-index: 100;

  .footer-info {
    .selected-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #1c1c1e;
      display: block;
    }

    .selected-status {
      font-size: 22rpx;
      color: #64748b;
      margin-top: 4rpx;
    }
  }

  .footer-actions {
    .action-btn {
      min-width: 160rpx;
      height: 72rpx;
      border-radius: 36rpx;
      font-size: 28rpx;
      font-weight: 600;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;

      &.start-btn {
        background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
        color: white;
      }

      &.continue-btn {
        background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
        color: white;
      }

      &.done-btn {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        color: white;
      }

      &:active {
        opacity: 0.9;
        transform: scale(0.95);
      }
    }
  }
}

// ==================== 拖拽提示 ====================
.drag-hint {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  padding: 20rpx 40rpx;
  border-radius: 40rpx;
  z-index: 1000;

  .hint-text {
    font-size: 28rpx;
    color: white;
  }
}
</style>
