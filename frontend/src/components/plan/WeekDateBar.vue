<template>
  <view class="week-date-bar">
    <view class="week-top">
      <view class="week-switch">
        <view class="switch-btn" @click="changeWeek(-1)">
          <AppIcon name="arrowLeft" size="26" bold />
        </view>
        <text class="week-range">{{ weekRangeText }}</text>
        <view class="switch-btn" @click="changeWeek(1)">
          <AppIcon name="arrowRight" size="26" bold />
        </view>
      </view>
      <view class="edit-btn" @click="$emit('edit')">编辑</view>
    </view>

    <view class="week-header">
      <text v-for="label in weekHeaderLabels" :key="label" class="week-header-item">{{ label }}</text>
    </view>

    <view class="week-grid">
      <view
        v-for="day in weekDays"
        :key="day.fullDate"
        class="week-day"
        :class="{
          active: selectedDate === day.fullDate,
          today: day.isToday,
          weekend: day.weekDay === 0 || day.weekDay === 6
        }"
        @click="selectDay(day)"
      >
        <text class="day-number">{{ day.day }}</text>
        <view class="day-dot" v-if="hasPlan(day)"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  selectedDate: { type: String, default: '' },
  planDays: { type: Array, default: () => [] },
  weekOffset: { type: Number, default: 0 }
})

const emit = defineEmits(['select', 'edit'])

const localWeekOffset = ref(props.weekOffset || 0)
const weekHeaderLabels = ['日', '一', '二', '三', '四', '五', '六']

watch(
  () => props.weekOffset,
  value => {
    localWeekOffset.value = value || 0
  }
)

const weekDays = computed(() => {
  const days = []
  const now = new Date()
  const weekStart = new Date(now)
  weekStart.setHours(0, 0, 0, 0)
  weekStart.setDate(now.getDate() - now.getDay() + localWeekOffset.value * 7)

  for (let i = 0; i < 7; i++) {
    const current = new Date(weekStart)
    current.setDate(weekStart.getDate() + i)
    days.push({
      day: current.getDate(),
      fullDate: formatDate(current),
      weekDay: current.getDay(),
      dayOfWeek: current.getDay() === 0 ? 7 : current.getDay(),
      isToday: isSameDay(current, now)
    })
  }

  return days
})

const weekRangeText = computed(() => {
  if (weekDays.value.length !== 7) return ''
  const start = weekDays.value[0].fullDate
  const end = weekDays.value[6].fullDate
  return `${start.slice(5)} ~ ${end.slice(5)}`
})

function changeWeek(delta) {
  localWeekOffset.value += delta
  const target = weekDays.value[0]
  if (target) {
    emit('select', { ...target, weekOffset: localWeekOffset.value })
  }
}

function selectDay(day) {
  emit('select', { ...day, weekOffset: localWeekOffset.value })
}

function hasPlan(day) {
  return props.planDays.some(item => item.dayOfWeek === day.dayOfWeek)
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function isSameDay(a, b) {
  return a.getFullYear() === b.getFullYear()
    && a.getMonth() === b.getMonth()
    && a.getDate() === b.getDate()
}
</script>

<style lang="scss" scoped>
.week-date-bar {
  margin: 0 24rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: linear-gradient(155deg, #ffffff 0%, #f4f8ff 100%);
  box-shadow: 0 16rpx 36rpx rgba(15, 23, 42, 0.08);
}

.week-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18rpx;
}

.week-switch {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.switch-btn {
  width: 52rpx;
  height: 52rpx;
  background: transparent;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.week-range {
  font-size: 30rpx;
  color: #0f172a;
  font-weight: 700;
}

.edit-btn {
  height: 44rpx;
  padding: 0 20rpx;
  border-radius: 12rpx;
  background: #0f172a;
  color: #ffffff;
  font-size: 24rpx;
  line-height: 44rpx;
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 12rpx;
}

.week-header-item {
  text-align: center;
  font-size: 22rpx;
  color: #64748b;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10rpx;
}

.week-day {
  height: 82rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 2rpx solid transparent;
}

.week-day.weekend .day-number {
  color: #ea580c;
}

.week-day.today {
  border-color: #93c5fd;
}

.week-day.active {
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  box-shadow: 0 8rpx 22rpx rgba(37, 99, 235, 0.34);
}

.week-day.active .day-number {
  color: #ffffff;
  font-weight: 700;
}

.week-day.active .day-dot {
  background: #ffffff;
}

.day-number {
  font-size: 30rpx;
  color: #1e293b;
  font-weight: 600;
}

.day-dot {
  position: absolute;
  bottom: 10rpx;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #3b82f6;
}
</style>
