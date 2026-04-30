<template>
  <view class="ai-chat-section">
    <!-- 对话区域 -->
    <view class="chat-container">
      <!-- 对话消息列表 -->
      <scroll-view 
        class="chat-messages" 
        scroll-y 
        :scroll-top="scrollTop"
        :style="{ height: chatHeight + 'px' }"
      >
        <!-- 欢迎消息 -->
        <view class="welcome-message" v-if="messages.length === 0">
          <text class="welcome-text">有什么我可以帮你的吗？</text>
        </view>

        <!-- 消息列表 -->
        <view 
          class="message-wrapper" 
          v-for="(msg, index) in messages" 
          :key="index"
          :class="{ 'user': msg.type === 'user', 'ai': msg.type === 'ai' }"
        >
          <!-- AI消息 - 左侧 -->
          <template v-if="msg.type === 'ai'">
            <view class="ai-avatar">
              <text class="avatar-text">AI</text>
            </view>
            <view class="message-content ai-message">
              <text class="message-text">{{ msg.content }}</text>
              <view class="message-time">{{ msg.time }}</view>
            </view>
          </template>

          <!-- 用户消息 - 右侧 -->
          <template v-else>
            <view class="message-content user-message">
              <text class="message-text">{{ msg.content }}</text>
              <view class="message-time">{{ msg.time }}</view>
            </view>
            <view class="user-avatar">
              <image class="avatar-img" :src="userAvatar" mode="aspectFill" />
            </view>
          </template>
        </view>

        <!-- 输入中提示 -->
        <view class="typing-indicator" v-if="isTyping">
          <view class="ai-avatar small">
            <text class="avatar-text">AI</text>
          </view>
          <view class="typing-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 底部输入栏 -->
    <view class="input-bar">
      <!-- 左侧语音按钮 -->
      <view 
        class="voice-btn" 
        :class="{ 'recording': isRecording }"
        @touchstart="startRecording"
        @touchend="stopRecording"
        @click="toggleVoiceMode"
      >
        <text class="btn-icon" v-if="!isRecording">🎤</text>
        <text class="btn-icon recording-icon" v-else>🔴</text>
      </view>

      <!-- 中间输入区域 -->
      <view class="input-wrapper">
        <input 
          v-if="!isVoiceMode"
          class="chat-input"
          type="text"
          v-model="inputMessage"
          placeholder="输入问题或训练需求..."
          confirm-type="send"
          @confirm="sendMessage"
          :focus="inputFocus"
          @blur="inputFocus = false"
        />
        <view v-else class="voice-hint" @click="toggleVoiceMode">
          <text class="hint-text">按住说话</text>
        </view>
      </view>

      <!-- 右侧更多按钮 -->
      <view class="more-btn" @click="showMoreOptions">
        <text class="btn-icon">+</text>
      </view>

      <!-- 发送按钮 (有内容时显示) -->
      <view 
        class="send-btn" 
        v-if="inputMessage.trim()"
        @click="sendMessage"
      >
        <text class="send-icon">➤</text>
      </view>
    </view>

    <!-- 更多功能面板 -->
    <view class="more-panel" v-if="showMore">
      <view class="panel-grid">
        <view class="panel-item" @click="selectImage">
          <view class="item-icon">📷</view>
          <text class="item-text">相册</text>
        </view>
        <view class="panel-item" @click="takePhoto">
          <view class="item-icon">📸</view>
          <text class="item-text">拍摄</text>
        </view>
        <view class="panel-item" @click="sendLocation">
          <view class="item-icon">📍</view>
          <text class="item-text">位置</text>
        </view>
        <view class="panel-item" @click="sendEmoji">
          <view class="item-icon">😊</view>
          <text class="item-text">表情</text>
        </view>
      </view>
    </view>

    <!-- 语音录制提示 -->
    <view class="voice-modal" v-if="isRecording">
      <view class="voice-content">
        <view class="voice-waves">
          <view class="wave"></view>
          <view class="wave"></view>
          <view class="wave"></view>
          <view class="wave"></view>
          <view class="wave"></view>
        </view>
        <text class="voice-text">正在聆听...</text>
        <text class="voice-hint">松开结束</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useUserStore } from '../stores/user'

const props = defineProps({
  // 聊天高度（像素）
  chatHeight: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['send', 'voice', 'more'])

const userStore = useUserStore()

// 用户头像
const userAvatar = computed(() => {
  return userStore.avatar || '/static/images/default-avatar.png'
})

// 消息列表
const messages = ref([
  {
    type: 'ai',
    content: '今天训练状态不错！要不要调整下一组的重量？或者有什么动作疑问可以随时问我～',
    time: getCurrentTime()
  }
])

// 输入状态
const inputMessage = ref('')
const inputFocus = ref(false)
const isVoiceMode = ref(false)
const isRecording = ref(false)
const isTyping = ref(false)
const showMore = ref(false)
const scrollTop = ref(0)

// 获取当前时间
function getCurrentTime() {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 发送消息
function sendMessage() {
  const content = inputMessage.value.trim()
  if (!content) return

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: content,
    time: getCurrentTime()
  })

  inputMessage.value = ''
  showMore.value = false
  scrollToBottom()

  // 触发发送事件
  emit('send', content)

  // 模拟AI回复
  simulateAIResponse()
}

// 模拟AI回复
function simulateAIResponse() {
  isTyping.value = true
  scrollToBottom()

  setTimeout(() => {
    isTyping.value = false
    messages.value.push({
      type: 'ai',
      content: '收到！我来为你分析一下。根据你今天的训练数据，建议适当降低重量，增加组数，这样可以更好地刺激肌肉生长。',
      time: getCurrentTime()
    })
    scrollToBottom()
  }, 1500)
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    scrollTop.value = messages.value.length * 1000
  })
}

// 语音模式切换
function toggleVoiceMode() {
  isVoiceMode.value = !isVoiceMode.value
  showMore.value = false
}

// 开始录音
function startRecording() {
  if (!isVoiceMode.value) return
  isRecording.value = true
}

// 停止录音
function stopRecording() {
  if (!isRecording.value) return
  isRecording.value = false
  
  // 模拟语音输入
  messages.value.push({
    type: 'user',
    content: '【语音消息】',
    time: getCurrentTime()
  })
  scrollToBottom()
  simulateAIResponse()
  
  emit('voice', { action: 'stop' })
}

// 显示更多选项
function showMoreOptions() {
  showMore.value = !showMore.value
  inputFocus.value = false
}

// 选择图片
function selectImage() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      messages.value.push({
        type: 'user',
        content: '[图片]',
        time: getCurrentTime(),
        image: res.tempFilePaths[0]
      })
      scrollToBottom()
      showMore.value = false
    }
  })
}

// 拍照
function takePhoto() {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    success: (res) => {
      messages.value.push({
        type: 'user',
        content: '[图片]',
        time: getCurrentTime(),
        image: res.tempFilePaths[0]
      })
      scrollToBottom()
      showMore.value = false
    }
  })
}

// 发送位置
function sendLocation() {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      messages.value.push({
        type: 'user',
        content: `[位置] 纬度:${res.latitude.toFixed(2)} 经度:${res.longitude.toFixed(2)}`,
        time: getCurrentTime()
      })
      scrollToBottom()
      showMore.value = false
    }
  })
}

// 发送表情
function sendEmoji() {
  inputMessage.value += '😊'
  showMore.value = false
}

// 暴露方法
defineExpose({
  addAIMessage: (content) => {
    messages.value.push({
      type: 'ai',
      content: content,
      time: getCurrentTime()
    })
    scrollToBottom()
  },
  clearMessages: () => {
    messages.value = []
  }
})
</script>

<style lang="scss" scoped>
.ai-chat-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px 24px 0 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
}

// 聊天容器
.chat-container {
  flex: 1;
  overflow: hidden;
  position: relative;
}

// 消息列表
.chat-messages {
  padding: 16px;
  box-sizing: border-box;
}

// 欢迎消息
.welcome-message {
  text-align: center;
  padding: 40px 0;

  .welcome-text {
    font-size: 14px;
    color: #94a3b8;
  }
}

// 消息包装器
.message-wrapper {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  animation: messageSlide 0.3s ease;

  @keyframes messageSlide {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // AI消息 - 左侧
  &.ai {
    justify-content: flex-start;
  }

  // 用户消息 - 右侧
  &.user {
    justify-content: flex-end;
    flex-direction: row-reverse;
  }
}

// AI头像
.ai-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);

  &.small {
    width: 28px;
    height: 28px;

    .avatar-text {
      font-size: 10px;
    }
  }

  .avatar-text {
    font-size: 12px;
    font-weight: 700;
    color: white;
  }
}

// 用户头像
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  margin-left: 8px;

  .avatar-img {
    width: 100%;
    height: 100%;
  }
}

// 消息内容
.message-content {
  max-width: 70%;
  margin: 0 10px;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;

  .message-text {
    font-size: 14px;
    line-height: 1.6;
  }

  .message-time {
    font-size: 10px;
    margin-top: 4px;
    opacity: 0.6;
  }

  // AI消息气泡
  &.ai-message {
    background: linear-gradient(135deg, #3b82f6 0%, #4f46e5 100%);
    color: white;
    border-bottom-left-radius: 4px;
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);

    .message-time {
      color: rgba(255, 255, 255, 0.7);
    }
  }

  // 用户消息气泡
  &.user-message {
    background: #f1f5f9;
    color: #1c1c1e;
    border-bottom-right-radius: 4px;

    .message-time {
      color: #94a3b8;
    }
  }
}

// 输入中指示器
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-left: 4px;

  .typing-dots {
    display: flex;
    gap: 4px;
    padding: 12px 16px;
    background: #f1f5f9;
    border-radius: 18px;
    border-bottom-left-radius: 4px;

    .dot {
      width: 8px;
      height: 8px;
      background: #94a3b8;
      border-radius: 50%;
      animation: typingBounce 1.4s infinite ease-in-out both;

      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
      &:nth-child(3) { animation-delay: 0; }
    }

    @keyframes typingBounce {
      0%, 80%, 100% {
        transform: scale(0.6);
      }
      40% {
        transform: scale(1);
      }
    }
  }
}

// 输入栏
.input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

// 语音按钮
.voice-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
    background: rgba(59, 130, 246, 0.2);
  }

  &.recording {
    background: #ef4444;
    animation: pulse 1.5s infinite;

    .recording-icon {
      color: white;
    }
  }

  @keyframes pulse {
    0%, 100% {
      box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
    }
    50% {
      box-shadow: 0 0 0 10px rgba(239, 68, 68, 0);
    }
  }

  .btn-icon {
    font-size: 18px;
  }
}

// 输入包装器
.input-wrapper {
  flex: 1;
  height: 40px;
  background: #f1f5f9;
  border-radius: 20px;
  display: flex;
  align-items: center;
  padding: 0 16px;
}

// 输入框
.chat-input {
  flex: 1;
  height: 100%;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1c1c1e;

  &::placeholder {
    color: #94a3b8;
  }
}

// 语音提示
.voice-hint {
  flex: 1;
  text-align: center;

  .hint-text {
    font-size: 14px;
    color: #64748b;
  }
}

// 更多按钮
.more-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
    background: rgba(0, 0, 0, 0.1);
  }

  .btn-icon {
    font-size: 24px;
    color: #64748b;
    font-weight: 300;
  }
}

// 发送按钮
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  animation: fadeIn 0.2s ease;

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: scale(0.8);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }

  &:active {
    transform: scale(0.95);
  }

  .send-icon {
    font-size: 16px;
    color: white;
    margin-left: 2px;
  }
}

// 更多面板
.more-panel {
  background: #f8fafc;
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  animation: slideUp 0.3s ease;

  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .panel-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }

  .panel-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    &:active {
      opacity: 0.7;
    }

    .item-icon {
      width: 56px;
      height: 56px;
      background: white;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .item-text {
      font-size: 12px;
      color: #64748b;
    }
  }
}

// 语音录制弹窗
.voice-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;

  .voice-content {
    background: rgba(0, 0, 0, 0.8);
    border-radius: 20px;
    padding: 40px 60px;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .voice-waves {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 20px;

    .wave {
      width: 6px;
      height: 30px;
      background: #3b82f6;
      border-radius: 3px;
      animation: wave 1s infinite ease-in-out;

      &:nth-child(1) { animation-delay: -0.4s; height: 20px; }
      &:nth-child(2) { animation-delay: -0.3s; height: 40px; }
      &:nth-child(3) { animation-delay: -0.2s; height: 60px; }
      &:nth-child(4) { animation-delay: -0.1s; height: 40px; }
      &:nth-child(5) { animation-delay: 0s; height: 20px; }
    }

    @keyframes wave {
      0%, 100% {
        transform: scaleY(0.5);
      }
      50% {
        transform: scaleY(1);
      }
    }
  }

  .voice-text {
    font-size: 18px;
    color: white;
    font-weight: 500;
    margin-bottom: 8px;
  }

  .voice-hint {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.6);
  }
}
</style>
