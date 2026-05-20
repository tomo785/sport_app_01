<template>
  <view class="ts-wrapper">
    <swiper class="ts-swiper" :current="currentIndex" @change="onSwiperChange">
      <swiper-item v-for="(card, idx) in cards" :key="idx">
        <view class="ts-card">
          <!-- 顶部标题 -->
          <view class="ts-header">
            <text class="ts-title">{{ card.title }}</text>
          </view>

          <!-- 中间内容区 -->
          <view class="ts-body">
            <!-- 左侧柱状图 -->
            <view class="ts-chart">
              <view class="ts-chart-bars">
                <view
                  class="ts-bar-wrap"
                  v-for="(bar, bi) in card.chartData"
                  :key="bi"
                  @click="onBarClick(bar, bi)"
                >
                  <text class="ts-bar-value" v-if="activeBarIdx === bi && activeCardIdx === idx">{{ bar.value }}</text>
                  <view
                    class="ts-bar"
                    :style="{ height: bar.pct + '%', background: bar.color || '#2563EB' }"
                  ></view>
                  <text class="ts-bar-label">{{ bar.label }}</text>
                </view>
              </view>
            </view>

            <!-- 右侧指标 -->
            <view class="ts-metrics">
              <view class="ts-metric" v-for="(m, mi) in card.metrics" :key="mi">
                <text class="ts-metric-value">{{ m.value }}</text>
                <text class="ts-metric-label">{{ m.label }}</text>
              </view>
            </view>
          </view>

          <!-- 底部进度条 -->
          <view class="ts-footer">
            <view class="ts-progress-track">
              <view
                class="ts-progress-segment"
                v-for="(seg, si) in card.progress"
                :key="si"
                :style="{ width: seg.width, background: seg.color }"
              ></view>
            </view>
            <text class="ts-footer-text">过去4周</text>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <!-- 轮播指示器 -->
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
const activeBarIdx = ref(-1)
const activeCardIdx = ref(-1)

// 三个数据维度卡片
const cards = ref([
  {
    icon: '🏃',
    title: '训练状态',
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
    icon: '❤️',
    title: '有氧基础',
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
    icon: '😴',
    title: '恢复状态',
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
  activeBarIdx.value = -1
  activeCardIdx.value = -1
}

function onBarClick(bar, idx) {
  activeBarIdx.value = idx
  activeCardIdx.value = currentIndex.value
}
</script>

<style lang="scss" scoped>
.ts-wrapper {
  margin: 0 28rpx 20rpx;
}

.ts-swiper {
  height: 480rpx;
}

.ts-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

/* 顶部标题 */
.ts-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.ts-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #000000;
}

/* 中间内容 */
.ts-body {
  flex: 1;
  display: flex;
  gap: 20rpx;
  min-height: 0;
}

/* 左侧柱状图 */
.ts-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.ts-chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 240rpx;
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

.ts-bar-value {
  font-size: 20rpx;
  font-weight: 600;
  color: #000000;
  position: absolute;
  top: -28rpx;
  white-space: nowrap;
}

.ts-bar {
  width: 28rpx;
  border-radius: 6rpx;
  min-height: 4rpx;
  transition: all 0.3s;
}

.ts-bar-label {
  font-size: 22rpx;
  color: #000000;
}

/* 右侧指标 */
.ts-metrics {
  width: 200rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24rpx;
}

.ts-metric {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ts-metric-value {
  font-size: 34rpx;
  font-weight: 700;
  color: #000000;
}

.ts-metric-label {
  font-size: 24rpx;
  color: #000000;
  opacity: 0.5;
}

/* 底部进度条 */
.ts-footer {
  margin-top: 16rpx;
}

.ts-progress-track {
  display: flex;
  height: 8rpx;
  border-radius: 999rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.ts-progress-segment {
  height: 100%;
}

.ts-footer-text {
  font-size: 20rpx;
  color: #000000;
  opacity: 0.4;
}

/* 轮播指示器 */
.ts-dots {
  display: flex;
  justify-content: center;
  gap: 12rpx;
  margin-top: 16rpx;
}

.ts-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #D1D5DB;
  transition: all 0.3s;

  &.active {
    background: #000000;
  }
}
</style>
