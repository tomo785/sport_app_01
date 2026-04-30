<template>
  <view class="container">
    <!-- 右上角模式切换 -->
    <view class="mode-bar">
      <view 
        class="mode-btn" 
        :class="{ active: mode === 'fixed' }"
        @click="setMode('fixed')"
      >固定项目</view>
      <view 
        class="mode-btn" 
        :class="{ active: mode === 'random' }"
        @click="setMode('random')"
      >随机项目</view>
    </view>

    <!-- 任务计划小卡片 -->
    <view class="task-card" @click="goToTaskFlow">
      <view class="task-header">
        <text class="task-title">今日运动打卡</text>
        <text class="task-status">{{ statusText }}</text>
      </view>
      <view class="task-progress">
        <view class="progress-ring">
          <text class="progress-num">{{ completedCount }}</text>
          <text class="progress-total">/{{ dailyTarget }}</text>
        </view>
        <text class="progress-label">{{ completedCount >= dailyTarget ? '今日目标已达成 🎉' : '完成小目标，点亮今日' }}</text>
      </view>
    </view>

    <!-- 小目标卡片区域 -->
    <view class="goals-area">
      <view class="goals-header">
        <text class="goals-title">今日小目标</text>
        <text class="goals-sub" v-if="mode === 'fixed'">固定挑战，每日一致</text>
        <text class="goals-sub" v-else>随机挑战，每次不同</text>
      </view>

      <view class="goals-grid" v-if="displayGoals.length > 0">
        <view 
          class="goal-card" 
          v-for="(item, index) in displayGoals" 
          :key="index"
          @click="completeGoal(index)"
        >
          <text class="goal-icon">{{ item.icon }}</text>
          <text class="goal-name">{{ item.name }}</text>
          <text class="goal-amount">{{ item.amount }}</text>
          <text class="goal-tip">点击完成</text>
        </view>
      </view>

      <view class="empty-state" v-else>
        <text class="empty-icon">🎉</text>
        <text class="empty-text">今日小目标全部完成！</text>
        <text class="empty-sub">太棒了，继续保持～</text>
      </view>
    </view>

    <!-- 随机模式刷新按钮 -->
    <view class="refresh-btn" v-if="mode === 'random' && completedCount < dailyTarget" @click="refreshRandomGoals">
      <text class="refresh-icon">↻</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'

// 每日目标数
const dailyTarget = ref(5)

// 已完成数量
const completedCount = ref(0)

// 模式：fixed 固定项目 | random 随机项目
const mode = ref('fixed')

// 卡片配置（从目标管理页同步）
const goalCards = ref([])
const fixedIds = ref([])
const randomIds = ref([])

// 固定项目：每日固定的一组目标（按日期固定）
const fixedGoals = ref([])

// 随机项目：当前随机展示的目标
const randomGoals = ref([])

const displayGoals = computed(() => {
  return mode.value === 'fixed' ? fixedGoals.value : randomGoals.value
})

const statusText = computed(() => {
  if (completedCount.value >= dailyTarget.value) return '已完成'
  return `还差 ${dailyTarget.value - completedCount.value} 个`
})

const GOAL_CONFIG_KEY = 'goalCardConfig'

// 生成本地存储 key（按日期）
function getTodayKey() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function loadGoalConfig() {
  const saved = uni.getStorageSync(GOAL_CONFIG_KEY)
  if (saved) {
    try {
      const data = JSON.parse(saved)
      goalCards.value = data.goalCards || []
      fixedIds.value = data.fixedIds || []
      randomIds.value = data.randomIds || []
    } catch (e) {
      initDefaultConfig()
    }
  } else {
    initDefaultConfig()
  }
}

function initDefaultConfig() {
  const defaultCards = [
    { id: 'g1', icon: '🦵', name: '深蹲', amount: '20次 × 2组' },
    { id: 'g2', icon: '💪', name: '俯卧撑', amount: '15次 × 2组' },
    { id: 'g3', icon: '🏋️', name: '哑铃弯举', amount: '12次 × 3组' },
    { id: 'g4', icon: '🧘', name: '平板支撑', amount: '45秒 × 2组' },
    { id: 'g5', icon: '⭐', name: '开合跳', amount: '30次 × 2组' },
    { id: 'g6', icon: '🔥', name: '高抬腿', amount: '40次 × 2组' },
    { id: 'g7', icon: '🏃', name: '原地跑', amount: '60秒 × 2组' },
    { id: 'g8', icon: '🎯', name: '卷腹', amount: '20次 × 2组' },
    { id: 'g9', icon: '⛰️', name: '登山者', amount: '20次 × 2组' },
    { id: 'g10', icon: '🦵', name: '弓步蹲', amount: '12次 × 2组' },
    { id: 'g11', icon: '💪', name: '臂屈伸', amount: '12次 × 2组' },
    { id: 'g12', icon: '🧘', name: '侧平板', amount: '30秒 × 2组' },
    { id: 'g13', icon: '🔥', name: '波比跳', amount: '10次 × 2组' },
    { id: 'g14', icon: '🏋️', name: '肩推', amount: '12次 × 2组' },
    { id: 'g15', icon: '⭐', name: '空中自行车', amount: '20次 × 2组' }
  ]
  goalCards.value = defaultCards
  fixedIds.value = defaultCards.map(i => i.id)
  randomIds.value = defaultCards.map(i => i.id)
  uni.setStorageSync(GOAL_CONFIG_KEY, JSON.stringify({
    goalCards: defaultCards,
    fixedIds: fixedIds.value,
    randomIds: randomIds.value
  }))
}

function loadState() {
  loadGoalConfig()
  const today = getTodayKey()
  const saved = uni.getStorageSync('dailyGoals_' + today)
  if (saved) {
    try {
      const data = JSON.parse(saved)
      completedCount.value = data.completedCount || 0
      mode.value = data.mode || 'fixed'
      fixedGoals.value = data.fixedGoals || []
      randomGoals.value = data.randomGoals || []
    } catch (e) {
      initDefault()
    }
  } else {
    initDefault()
  }
}

function saveState() {
  const today = getTodayKey()
  const data = {
    completedCount: completedCount.value,
    mode: mode.value,
    fixedGoals: fixedGoals.value,
    randomGoals: randomGoals.value
  }
  uni.setStorageSync('dailyGoals_' + today, JSON.stringify(data))
}

// 按日期种子生成固定目标
function generateFixedGoals() {
  const pool = goalCards.value.filter(c => fixedIds.value.includes(c.id))
  if (pool.length === 0) return []
  const today = getTodayKey()
  let seed = 0
  for (let i = 0; i < today.length; i++) seed += today.charCodeAt(i)
  const shuffled = shuffleWithSeed([...pool], seed)
  return shuffled.slice(0, Math.min(dailyTarget.value, pool.length))
}

function generateRandomGoals() {
  const pool = goalCards.value.filter(c => randomIds.value.includes(c.id))
  if (pool.length === 0) return []
  const shuffled = [...pool].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, Math.min(dailyTarget.value, pool.length))
}

function shuffleWithSeed(array, seed) {
  let m = array.length
  while (m) {
    const i = Math.floor(randomFromSeed(seed) * m--)
    seed = nextSeed(seed)
    ;[array[m], array[i]] = [array[i], array[m]]
  }
  return array
}

function randomFromSeed(seed) {
  const x = Math.sin(seed) * 10000
  return x - Math.floor(x)
}

function nextSeed(seed) {
  return seed + 1
}

function initDefault() {
  completedCount.value = 0
  mode.value = 'fixed'
  fixedGoals.value = generateFixedGoals()
  randomGoals.value = generateRandomGoals()
  saveState()
}

function setMode(newMode) {
  if (mode.value === newMode) return
  mode.value = newMode
  saveState()
}

function completeGoal(index) {
  if (completedCount.value >= dailyTarget.value) {
    uni.showToast({ title: '今日目标已达成', icon: 'none' })
    return
  }

  const arr = mode.value === 'fixed' ? fixedGoals.value : randomGoals.value
  arr.splice(index, 1)
  completedCount.value += 1
  saveState()

  if (completedCount.value >= dailyTarget.value) {
    uni.showToast({ title: '今日打卡完成！', icon: 'success' })
  }
}

function refreshRandomGoals() {
  if (completedCount.value >= dailyTarget.value) return
  randomGoals.value = generateRandomGoals()
  saveState()
  uni.showToast({ title: '已刷新小目标', icon: 'none' })
}

function goToTaskFlow() {
  uni.switchTab({ url: '/pages/task/task-flow' })
}

onMounted(() => {
  loadState()
})

onShow(() => {
  loadState()
})

onPullDownRefresh(() => {
  loadState()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4f8 0%, #f8fafc 100%);
  padding: 30rpx;
  box-sizing: border-box;
  position: relative;
}

/* 右上角模式切换 */
.mode-bar {
  position: absolute;
  top: 20rpx;
  right: 30rpx;
  display: flex;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 32rpx;
  padding: 4rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.mode-btn {
  padding: 10rpx 24rpx;
  font-size: 24rpx;
  color: #64748b;
  border-radius: 28rpx;

  &.active {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: #fff;
    font-weight: 600;
  }
}

/* 任务计划小卡片 */
.task-card {
  margin-top: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 36rpx;
  color: #fff;
  box-shadow: 0 8rpx 30rpx rgba(102, 126, 234, 0.3);

  &:active {
    transform: scale(0.99);
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.task-title {
  font-size: 34rpx;
  font-weight: 700;
}

.task-status {
  font-size: 24rpx;
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.task-progress {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.progress-ring {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.progress-num {
  font-size: 44rpx;
  font-weight: 700;
}

.progress-total {
  font-size: 24rpx;
  opacity: 0.9;
}

.progress-label {
  flex: 1;
  font-size: 28rpx;
  opacity: 0.95;
}

/* 小目标区域 */
.goals-area {
  margin-top: 40rpx;
}

.goals-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 24rpx;
}

.goals-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1c1c1e;
}

.goals-sub {
  font-size: 24rpx;
  color: #64748b;
}

.goals-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.goal-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.96);
    background: #f8fafc;
  }

  .goal-icon {
    font-size: 48rpx;
    display: block;
    margin-bottom: 12rpx;
  }

  .goal-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #1c1c1e;
    display: block;
    margin-bottom: 6rpx;
  }

  .goal-amount {
    font-size: 26rpx;
    color: #667eea;
    display: block;
    margin-bottom: 10rpx;
  }

  .goal-tip {
    font-size: 22rpx;
    color: #94a3b8;
    background: #f1f5f9;
    padding: 4rpx 14rpx;
    border-radius: 12rpx;
    display: inline-block;
  }
}

.empty-state {
  background: #fff;
  border-radius: 20rpx;
  padding: 60rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);

  .empty-icon {
    font-size: 64rpx;
    display: block;
    margin-bottom: 16rpx;
  }

  .empty-text {
    font-size: 30rpx;
    font-weight: 600;
    color: #1c1c1e;
    display: block;
    margin-bottom: 8rpx;
  }

  .empty-sub {
    font-size: 24rpx;
    color: #64748b;
  }
}

/* 刷新按钮 */
.refresh-btn {
  position: fixed;
  right: 40rpx;
  bottom: calc(40rpx + env(safe-area-inset-bottom));
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.35);
  z-index: 100;

  &:active {
    transform: scale(0.92);
  }

  .refresh-icon {
    font-size: 44rpx;
    color: #fff;
    font-weight: 700;
  }
}
</style>
