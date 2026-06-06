<template>
  <view class="ai-settings-container">
    <!-- 选择模型 -->
    <view class="ai-section">
      <view class="ai-section-title">选择模型</view>
      <view class="ai-provider-list">
        <view
          v-for="model in modelList"
          :key="model.id"
          class="ai-provider-card"
          :class="{ 'ai-provider-active': model.id === selectedModelId }"
          @click="selectModel(model)"
        >
          <view class="ai-provider-info">
            <text class="ai-provider-name">{{ model.name }}</text>
            <text class="ai-provider-id">{{ model.model }}</text>
          </view>
          <AppIcon v-if="model.id === selectedModelId" class="ai-provider-check" name="check" size="28" />
        </view>
      </view>
    </view>
    <!-- API Key -->
    <view class="ai-section">
      <view class="ai-section-title">API Key</view>
      <input
        class="ai-input"
        :value="customApiKey"
        @input="updateApiKey($event.detail.value)"
        placeholder="输入自定义 API Key（留空使用后端配置）"
        password
      />
    </view>
    <!-- 连接测试 -->
    <view class="ai-section">
      <view class="ai-section-title">连接测试</view>
      <view class="ai-test-row">
        <view
          class="ai-test-btn"
          :class="{ 'ai-test-running': testingConnection }"
          @click="testConnection"
        >
          <text>{{ testingConnection ? '测试中...' : '测试连接' }}</text>
        </view>
        <view class="ai-test-result" v-if="testResult">
          <text :class="testResult.ok ? 'ai-test-success' : 'ai-test-fail'">
            {{ testResult.message }}
          </text>
        </view>
      </view>
    </view>
    <!-- 提示 -->
    <view class="ai-hint">
      <text>配置仅保存在当前设备</text>
    </view>
  </view>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue'
import { get, post } from '../../utils/request.js'
const modelList = ref([])
const selectedModelId = ref(uni.getStorageSync('ai_selected_model') || '')
// 按模型分别存储 API Key: { kimi: 'xxx', deepseek: 'yyy' }
const apiKeyMap = ref(JSON.parse(uni.getStorageSync('ai_api_key_map') || '{}'))
const customApiKey = computed(() => apiKeyMap.value[selectedModelId.value] || '')
const testingConnection = ref(false)
const testResult = ref(null)
const fetchModels = async () => {
  try {
    const res = await get('/ai/models')
    console.log('[AI配置] 获取模型列表:', res)
    const list = res?.data || res
    if (Array.isArray(list)) {
      modelList.value = list
      if (!selectedModelId.value) {
        const active = list.find(m => m.active)
        if (active) selectedModelId.value = active.id
      }
    }
  } catch (e) {
    console.warn('[AI配置] 获取模型列表失败:', e)
  }
}
const selectModel = async (model) => {
  selectedModelId.value = model.id
  uni.setStorageSync('ai_selected_model', model.id)
  try {
    await post('/ai/switch-model', { modelId: model.id })
    uni.showToast({ title: `已切换为 ${model.name}`, icon: 'none', duration: 1500 })
  } catch (e) {
    console.error('切换模型失败:', e)
    uni.showToast({ title: '切换失败', icon: 'none', duration: 1500 })
  }
}
const updateApiKey = (val) => {
  apiKeyMap.value[selectedModelId.value] = val
  uni.setStorageSync('ai_api_key_map', JSON.stringify(apiKeyMap.value))
}
const testConnection = async () => {
  if (testingConnection.value) return
  testingConnection.value = true
  testResult.value = null
  const modelId = selectedModelId.value || 'deepseek'
  const apiKey = customApiKey.value || ''
  console.log('[AI测试] 请求参数:', { modelId, apiKey: apiKey ? '***' : '空' })
  try {
    const res = await post('/ai/test-connection', { modelId, apiKey })
    console.log('[AI测试] 响应:', res)
    if (res && res.code === 200) {
      testResult.value = { ok: true, message: '连接成功！' }
    } else {
      testResult.value = { ok: false, message: res?.message || '连接失败' }
    }
  } catch (e) {
    console.error('[AI测试] 请求失败:', JSON.stringify(e))
    testResult.value = { ok: false, message: e?.message || e?.msg || '网络请求失败，请检查后端服务' }
  } finally {
    testingConnection.value = false
  }
}
onMounted(() => {
  fetchModels()
})
</script>
<style lang="scss" scoped>
.ai-settings-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}
.ai-section {
  margin: 20rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.ai-section-title {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 20rpx;
}
.ai-provider-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.ai-provider-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #f8f8fc;
  border: 2rpx solid transparent;
  &:active {
    background: #f0f0f7;
  }
}
.ai-provider-active {
  border-color: #667eea;
  background: #f0f0ff;
}
.ai-provider-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.ai-provider-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}
.ai-provider-id {
  font-size: 22rpx;
  color: #999;
}
.ai-provider-check {
  color: #667eea;
  font-size: 28rpx;
  font-weight: bold;
}
.ai-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: #f8f8fc;
  border-radius: 12rpx;
  font-size: 26rpx;
  border: 2rpx solid #e0e0e0;
  box-sizing: border-box;
}
.ai-test-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-wrap: wrap;
}
.ai-test-btn {
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 26rpx;
  color: #fff;
  &:active {
    opacity: 0.85;
  }
}
.ai-test-running {
  opacity: 0.6;
  pointer-events: none;
}
.ai-test-result {
  font-size: 26rpx;
}
.ai-test-success {
  color: #10b981;
}
.ai-test-fail {
  color: #e74c3c;
}
.ai-hint {
  text-align: center;
  padding: 30rpx;
  text {
    font-size: 22rpx;
    color: #bbb;
  }
}
</style>