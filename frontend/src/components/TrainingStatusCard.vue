<template>
  <view class="ts-wrapper">
    <swiper class="ts-swiper" :current="currentIndex" @change="onSwiperChange">
      <swiper-item v-for="(card, idx) in cards" :key="idx">
        <view class="ts-card">
          <view class="ts-header">
            <view class="ts-heading">
              <text class="ts-title">{{ card.title }}</text>
              <text class="ts-subtitle">{{ card.subtitle }}</text>
            </view>
            <text class="ts-status">{{ card.status }}</text>
          </view>

          <view class="ts-body">
            <view class="ts-chart">
              <view class="ts-chart-bars">
                <view
                  class="ts-bar-wrap"
                  v-for="(bar, bi) in card.chartData"
                  :key="bi"
                >
                  <view
                    class="ts-bar"
                    :style="{ height: bar.pct + '%', background: bar.color || '#2563EB' }"
                  ></view>
                  <text class="ts-bar-label">{{ bar.label }}</text>
                </view>
              </view>
            </view>

            <view class="ts-metrics">
              <view class="ts-metric" v-for="(m, mi) in card.metrics" :key="mi">
                <text class="ts-metric-value">{{ m.value }}</text>
                <text class="ts-metric-label">{{ m.label }}</text>
              </view>
            </view>
          </view>

          <view class="ts-progress-track">
            <view
              class="ts-progress-segment"
              v-for="(seg, si) in card.progress"
              :key="si"
              :style="{ width: seg.width, background: seg.color }"
            ></view>
          </view>
        </view>
      </swiper-item>
    </swiper>
    <view class="ts-dots">
      <view
        class="ts-dot"
        v-for="(_, di) in cards"
        :key="di"
        :class="{ active: currentIndex === di }"
      ></view>
    </view>
  </view>
</template>
<script setup>
import { ref } from 'vue'
const currentIndex = ref(0)
// 三个数据维度卡片
const cards = ref([
  {
    icon: 'run',
    title: '训练状态',
    subtitle: '最近7天负荷趋势',
    status: '负荷偏低',
    chartData: [
      { label: '一', value: '45', pct: 60, color: '#2563EB' },
      { label: '二', value: '60', pct: 80, color: '#2563EB' },
      { label: '三', value: '30', pct: 40, color: '#2563EB' },
      { label: '四', value: '75', pct: 100, color: '#2563EB' },
      { label: '五', value: '50', pct: 67, color: '#2563EB' },
      { label: '六', value: '20', pct: 27, color: '#10B981' },
      { label: '日', value: '0', pct: 5, color: '#CBD5E1' }
    ],
    metrics: [
      { value: '极佳', label: '最大摄氧量' },
      { value: '低', label: '负荷' },
      { value: '无状态', label: 'HRV状态' }
    ],
    progress: [
      { width: '35%', color: '#2563EB' },
      { width: '25%', color: '#EAB308' },
      { width: '25%', color: '#10B981' },
      { width: '15%', color: '#CBD5E1' }
    ]
  },
  {
    icon: 'cardio',
    title: '有氧基础',
    subtitle: '心肺与恢复节奏',
    status: '稳定',
    chartData: [
      { label: '一', value: '120', pct: 55, color: '#2563EB' },
      { label: '二', value: '135', pct: 70, color: '#2563EB' },
      { label: '三', value: '110', pct: 50, color: '#2563EB' },
      { label: '四', value: '145', pct: 85, color: '#2563EB' },
      { label: '五', value: '130', pct: 72, color: '#2563EB' },
      { label: '六', value: '90', pct: 35, color: '#10B981' },
      { label: '日', value: '85', pct: 30, color: '#10B981' }
    ],
    metrics: [
      { value: '良好', label: '有氧能力' },
      { value: '中等', label: '无氧能力' },
      { value: '稳定', label: '心率恢复' }
    ],
    progress: [
      { width: '30%', color: '#2563EB' },
      { width: '30%', color: '#10B981' },
      { width: '25%', color: '#EAB308' },
      { width: '15%', color: '#CBD5E1' }
    ]
  },
  {
    icon: 'rest',
    title: '恢复状态',
    subtitle: '睡眠与身体反馈',
    status: '恢复良好',
    chartData: [
      { label: '一', value: '8h', pct: 90, color: '#10B981' },
      { label: '二', value: '7h', pct: 78, color: '#10B981' },
      { label: '三', value: '6h', pct: 65, color: '#2563EB' },
      { label: '四', value: '7.5h', pct: 82, color: '#10B981' },
      { label: '五', value: '5.5h', pct: 55, color: '#EAB308' },
      { label: '六', value: '9h', pct: 95, color: '#10B981' },
      { label: '日', value: '8.5h', pct: 92, color: '#10B981' }
    ],
    metrics: [
      { value: '充足', label: '睡眠时长' },
      { value: '良好', label: '睡眠质量' },
      { value: '正常', label: '静息心率' }
    ],
    progress: [
      { width: '40%', color: '#10B981' },
      { width: '30%', color: '#2563EB' },
      { width: '20%', color: '#EAB308' },
      { width: '10%', color: '#CBD5E1' }
    ]
  }
])
function onSwiperChange(e) {
  currentIndex.value = e.detail.current
}
</script>
<style lang="scss" scoped>
.ts-wrapper {
  margin: 0 28rpx 18rpx;
}
.ts-swiper {
  height: 390rpx;
}
.ts-card {
  background: var(--surface-card-gradient);
  border: 1rpx solid var(--card-border);
  border-radius: 24rpx;
  padding: 22rpx 24rpx;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  box-shadow: var(--card-shadow);
  overflow: hidden;
  position: relative;
}
.ts-card::before {
  display: none;
}
.ts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 12rpx;
  position: relative;
  z-index: 1;
}
.ts-heading {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}
.ts-title {
  font-size: 30rpx;
  font-weight: 800;
  color: var(--text-primary);
}
.ts-subtitle {
  font-size: 22rpx;
  color: var(--text-tertiary);
}
.ts-status {
  flex-shrink: 0;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: var(--selected-soft);
  color: var(--accent-green-dark);
  font-size: 24rpx;
  font-weight: 700;
}
.ts-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  justify-content: space-between;
  min-height: 0;
  position: relative;
  z-index: 1;
}
.ts-chart {
  flex: none;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.ts-chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 98rpx;
  padding-bottom: 4rpx;
}
.ts-bar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  gap: 8rpx;
  position: relative;
}
.ts-bar {
  width: 22rpx;
  border-radius: 999rpx;
  min-height: 4rpx;
  transition: all 0.3s;
}
.ts-bar-label {
  font-size: 20rpx;
  color: var(--text-tertiary);
}
.ts-metrics {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 10rpx;
}
.ts-metric {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2rpx;
  height: 72rpx;
  padding: 10rpx;
  border-radius: 14rpx;
  background: var(--metric-bg);
  border: 1rpx solid var(--metric-border);
  min-width: 0;
  box-sizing: border-box;
}
.ts-metric-value {
  font-size: 26rpx;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ts-metric-label {
  font-size: 20rpx;
  color: var(--text-primary);
  opacity: 0.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ts-progress-track {
  display: flex;
  height: 8rpx;
  border-radius: 999rpx;
  overflow: hidden;
  margin-top: 12rpx;
  background: var(--border-color);
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}
.ts-progress-segment {
  height: 100%;
}
.ts-dots {
  display: flex;
  justify-content: center;
  gap: 12rpx;
  margin-top: 10rpx;
}
.ts-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: var(--text-tertiary);
  transition: all 0.3s;
  opacity: 0.4;
  &.active {
    background: var(--accent-green);
    width: 34rpx;
    border-radius: 999rpx;
    opacity: 1;
  }
}
</style>
