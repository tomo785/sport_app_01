<template>
  <view class="design-container">
    <view class="design-header">
      <text class="design-title">设计</text>
      <text class="design-subtitle">Lottie 动画展示</text>
    </view>
    <view class="design-content">
      <view class="lottie-card" id="lottie-wrap">
        <!-- #ifdef H5 -->
        <Vue3Lottie
          class="lottie-full"
          :animationData="animationData"
          :height="600"
          :width="600"
          :loop="true"
          :autoplay="true"
        />
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <canvas
          type="2d"
          id="lottie-canvas"
          class="lottie-canvas"
        />
        <!-- #endif -->
      </view>
      <view class="design-info">
        <text class="info-title">Run complete</text>
        <text class="info-desc">Lottie 动画演示</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import { onReady } from '@dcloudio/uni-app'
import animationJson from '@/static/lottie/09abf458-91f2-465e-9ff8-ac236b51ffd8.json'

// #ifdef H5
import { Vue3Lottie } from 'vue3-lottie'
// #endif
// #ifndef H5
import lottie from 'lottie-miniprogram'
// #endif

const animationData = animationJson

// #ifdef H5
onMounted(() => {
  console.log('design page mounted (H5)')
})
// #endif

// #ifndef H5
onReady(() => {
  // 小程序端用 onReady 确保布局完成后再初始化 canvas
  setTimeout(() => {
    initMpLottie()
  }, 100)
})
// #endif

// #ifndef H5
const initMpLottie = () => {
  const query = uni.createSelectorQuery()
  query.select('#lottie-wrap').boundingClientRect()
  query.select('#lottie-canvas').fields({ node: true })
  query.exec((res) => {
    console.log('lottie init res:', res)
    const wrapRect = res[0]
    const canvasRes = res[1]
    console.log('wrapRect:', wrapRect)
    console.log('canvasRes:', canvasRes)
    if (!canvasRes || !canvasRes.node) {
      console.error('canvas node not found')
      return
    }
    const canvas = canvasRes.node
    const ctx = canvas.getContext('2d')
    const sysInfo = uni.getSystemInfoSync()
    const dpr = sysInfo.pixelRatio
    const w = wrapRect.width || sysInfo.windowWidth - 80
    const h = wrapRect.height || sysInfo.windowHeight * 0.5
    console.log('canvas size:', w, h, 'dpr:', dpr)
    canvas.width = w * dpr
    canvas.height = h * dpr
    ctx.scale(dpr, dpr)

    // 必须先 setup
    lottie.setup(canvas)

    const anim = lottie.loadAnimation({
      renderer: 'canvas',
      loop: true,
      autoplay: true,
      animationData: animationData,
      rendererSettings: {
        context: ctx,
        clearCanvas: true
      }
    })
    console.log('lottie animation loaded:', anim)
  })
}
// #endif
</script>

<style lang="scss" scoped>
.design-container {
  height: 100vh;
  background: #FDF6F0;
  padding-bottom: 40rpx;
  display: flex;
  flex-direction: column;
}

.design-header {
  padding: 60rpx 40rpx 30rpx;
}

.design-title {
  font-size: 48rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.design-subtitle {
  font-size: 28rpx;
  color: #999;
  margin-top: 12rpx;
  display: block;
}

.design-content {
  padding: 0 40rpx;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.lottie-card {
  background: #FFE8D6;
  border-radius: 24rpx;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  overflow: hidden;
  min-height: 400rpx;
}

.lottie-canvas {
  width: 100%;
  height: 100%;
  background: #FFE8D6;
}

.lottie-full {
  width: 100%;
  height: 100%;
}

.design-info {
  margin-top: 40rpx;
  text-align: center;
}

.info-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.info-desc {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
  display: block;
}
</style>
