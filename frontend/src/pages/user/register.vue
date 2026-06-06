<template>
  <view class="register-container">
    <view class="register-header">
      <text class="header-title">注册账号</text>
      <text class="header-subtitle">填写信息，开启运动之旅</text>
    </view>
    <view class="register-form">
      <!-- 账号输入 -->
      <view class="form-item">
        <text class="form-label">账号</text>
        <input
          class="form-input"
          type="text"
          v-model="form.username"
          placeholder="请输入账号（4-20位字母数字）"
          maxlength="20"
        />
      </view>
      <!-- 密码输入 -->
      <view class="form-item">
        <text class="form-label">设置密码</text>
        <input
          class="form-input"
          type="password"
          v-model="form.password"
          placeholder="请输入6-20位密码"
          maxlength="20"
        />
      </view>
      <!-- 确认密码 -->
      <view class="form-item">
        <text class="form-label">确认密码</text>
        <input
          class="form-input"
          type="password"
          v-model="form.confirmPassword"
          placeholder="请再次输入密码"
          maxlength="20"
        />
      </view>
      <view class="password-tips">
        <text class="tips-text">密码需6-20位字符，建议包含字母和数字</text>
      </view>
      <!-- 协议 -->
      <view class="agreement">
        <checkbox-group @change="handleAgreementChange">
          <checkbox :checked="form.isAgreed" color="#4CAF50" />
        </checkbox-group>
        <text class="agreement-text">
          我已阅读并同意
          <text class="agreement-link" @click="showAgreement">《用户协议》</text>
          和
          <text class="agreement-link" @click="showPrivacy">《隐私政策》</text>
        </text>
      </view>
      <button class="submit-btn" @click="handleRegister" :disabled="isSubmitting">
        {{ isSubmitting ? '注册中...' : '立即注册' }}
      </button>
      <!-- 游客模式 -->
      <view class="guest-mode">
        <view class="guest-divider">
          <text class="guest-divider-text">或者</text>
        </view>
        <button class="guest-btn" @click="enterAsGuest">
          <view class="guest-btn-text"><AppIcon name="guest" size="26" /><text>游客模式</text></view>
          <text class="guest-btn-desc">无需注册，立即体验</text>
        </button>
      </view>
    </view>
    <!-- 返回登录 -->
    <view class="back-login">
      <text class="back-text" @click="goToLogin">已有账号？去登录</text>
    </view>
  </view>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '../../stores/user'
import { register } from '../../api/user'
const userStore = useUserStore()
// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  isAgreed: false
})
const isSubmitting = ref(false)
// 验证账号
const validateUsername = (username) => {
  const reg = /^[a-zA-Z0-9_]{4,20}$/
  return reg.test(username)
}
// 协议勾选
const handleAgreementChange = (e) => {
  form.isAgreed = e.detail.value.length > 0
}
// 显示用户协议
const showAgreement = () => {
  uni.showModal({
    title: '用户协议',
    content: '用户协议内容...',
    showCancel: false
  })
}
// 显示隐私政策
const showPrivacy = () => {
  uni.showModal({
    title: '隐私政策',
    content: '隐私政策内容...',
    showCancel: false
  })
}
// 注册
const handleRegister = async () => {
  // 验证账号
  if (!form.username.trim()) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!validateUsername(form.username)) {
    uni.showToast({ title: '账号需4-20位字母数字或下划线', icon: 'none' })
    return
  }
  // 验证密码
  if (!form.password || form.password.length < 6) {
    uni.showToast({ title: '密码至少6位字符', icon: 'none' })
    return
  }
  if (form.password.length > 20) {
    uni.showToast({ title: '密码最多20位字符', icon: 'none' })
    return
  }
  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return
  }
  // 验证协议
  if (!form.isAgreed) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }
  isSubmitting.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password
    })
    if (res.code === 200) {
      // 保存登录状态
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      // 标记已登录过
      uni.setStorageSync('hasLoggedBefore', true)
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index/index' })
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '注册失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: error.message || '注册失败', icon: 'none' })
  } finally {
    isSubmitting.value = false
  }
}
// 返回登录
const goToLogin = () => {
  uni.navigateBack()
}
// 游客模式进入
const enterAsGuest = () => {
  uni.showModal({
    title: '游客模式',
    content: '游客模式下的数据不会永久保存，建议注册账号以获得更好的体验。',
    confirmText: '继续',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        // 设置游客状态
        const guestInfo = {
          id: 'guest_' + Date.now(),
          nickname: '游客用户',
          avatar: '/static/icons/user.png',
          isGuest: true,
          phone: null
        }
        // 游客模式不设置 token，避免请求时携带无效 token 导致 401
        userStore.setToken('')
        userStore.setUserInfo(guestInfo)
        // 标记已登录过（避免再次弹出登录）
        uni.setStorageSync('hasLoggedBefore', true)
        uni.setStorageSync('isGuestMode', true)
        uni.showToast({ title: '欢迎游客', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 1000)
      }
    }
  })
}
</script>
<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background: #fff;
  padding: 60rpx 40rpx;
}
.register-header {
  margin-bottom: 60rpx;
}
.header-title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}
.header-subtitle {
  font-size: 28rpx;
  color: #999;
}
.register-form {
  margin-bottom: 40rpx;
}
.form-item {
  margin-bottom: 40rpx;
}
.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
  font-weight: 500;
}
.form-input {
  width: 100%;
  height: 90rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 10rpx;
  font-size: 28rpx;
}
.password-tips {
  margin-top: -20rpx;
  margin-bottom: 30rpx;
}
.tips-text {
  font-size: 24rpx;
  color: #999;
}
.agreement {
  display: flex;
  align-items: flex-start;
  margin-bottom: 40rpx;
}
.agreement-text {
  font-size: 24rpx;
  color: #999;
  margin-left: 10rpx;
  line-height: 1.5;
}
.agreement-link {
  color: #4CAF50;
}
.submit-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: #fff;
  border: none;
  border-radius: 10rpx;
  font-size: 32rpx;
  font-weight: bold;
}
.submit-btn[disabled] {
  opacity: 0.6;
}
.back-login {
  text-align: center;
  margin-top: 40rpx;
}
.back-text {
  font-size: 28rpx;
  color: #4CAF50;
}
// 游客模式
.guest-mode {
  margin-top: 60rpx;
}
.guest-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  position: relative;
  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, #e0e0e0);
  }
  &::after {
    background: linear-gradient(90deg, #e0e0e0, transparent);
  }
}
.guest-divider-text {
  font-size: 24rpx;
  color: #999;
  padding: 0 20rpx;
}
.guest-btn {
  width: 100%;
  height: 120rpx;
  background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
  border: 2rpx dashed #ccc;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  transition: all 0.3s;
  &:active {
    background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
    border-color: #4CAF50;
  }
}
.guest-btn-text {
  font-size: 32rpx;
  color: #666;
  font-weight: 500;
}
.guest-btn-desc {
  font-size: 24rpx;
  color: #999;
}
</style>