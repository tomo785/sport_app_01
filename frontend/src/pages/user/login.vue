<template>
  <view class="login-container">
    <view class="login-header">
      <image class="logo" src="/static/icons/logo.png" mode="aspectFit"></image>
      <text class="app-name">运动App</text>
      <text class="app-slogan">记录每一次运动</text>
    </view>
    <view class="login-form">
      <!-- 账号输入 -->
      <view class="form-item">
        <text class="form-label">账号</text>
        <input
          class="form-input"
          type="text"
          v-model="form.username"
          placeholder="请输入账号"
          maxlength="20"
        />
      </view>
      <!-- 密码输入 -->
      <view class="form-item">
        <text class="form-label">密码</text>
        <input
          class="form-input"
          type="password"
          v-model="form.password"
          placeholder="请输入密码"
          maxlength="20"
        />
      </view>
      <!-- 记住密码 -->
      <view class="remember-box">
        <checkbox-group @change="handleRememberChange">
          <checkbox :checked="form.remember" color="#4CAF50" />
        </checkbox-group>
        <text class="remember-text">记住密码</text>
      </view>
      <button class="login-btn" @click="handleLogin" :disabled="userStore.isLoading">
        {{ userStore.isLoading ? '登录中...' : '登录' }}
      </button>
      <!-- 底部操作 -->
      <view class="form-footer">
        <text class="footer-link" @click="goToRegister">注册账号</text>
      </view>
    </view>
    <!-- 其他登录方式 -->
    <view class="other-login">
      <view class="divider">
        <text class="divider-text">其他方式</text>
      </view>
      <view class="other-btn-group">
        <view class="other-btn guest" @click="enterAsGuest">
          <AppIcon class="guest-icon" name="guest" size="28" />
          <text class="other-text">游客模式</text>
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
import { reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { loginByPassword } from '../../api/user'
const userStore = useUserStore()
// 表单数据
const form = reactive({
  username: '',
  password: '',
  remember: false
})
// 页面加载时检查是否有保存的账号
onMounted(() => {
  const savedUsername = uni.getStorageSync('savedUsername')
  if (savedUsername) {
    form.username = savedUsername
    form.remember = true
  }
})
// 登录
const handleLogin = async () => {
  if (!form.username.trim()) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!form.password || form.password.length < 6) {
    uni.showToast({ title: '请输入6位以上密码', icon: 'none' })
    return
  }
  userStore.isLoading = true
  try {
    const res = await loginByPassword(form.username, form.password)
    if (res.code === 200) {
      // 保存登录状态
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      // 记住账号
      if (form.remember) {
        uni.setStorageSync('savedUsername', form.username)
      } else {
        uni.removeStorageSync('savedUsername')
      }
      // 标记已登录过
      uni.setStorageSync('hasLoggedBefore', true)
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index/index' })
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '登录失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  } finally {
    userStore.isLoading = false
  }
}
// 记住密码切换
const handleRememberChange = (e) => {
  form.remember = e.detail.value.length > 0
}
// 跳转到注册页面
const goToRegister = () => {
  uni.navigateTo({ url: '/pages/user/register' })
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
.login-container {
  min-height: 100vh;
  background: #fff;
  padding: 60rpx 40rpx;
}
.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
}
.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}
.app-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}
.app-slogan {
  font-size: 28rpx;
  color: #999;
}
.login-form {
  margin-bottom: 60rpx;
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
.remember-box {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
}
.remember-text {
  font-size: 26rpx;
  color: #666;
  margin-left: 10rpx;
}
.login-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: #fff;
  border: none;
  border-radius: 10rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-top: 20rpx;
}
.login-btn[disabled] {
  opacity: 0.6;
}
.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 40rpx;
}
.footer-link {
  font-size: 28rpx;
  color: #4CAF50;
}
.other-login {
  margin-top: 80rpx;
}
.divider {
  position: relative;
  text-align: center;
  margin-bottom: 40rpx;
}
.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1rpx;
  background: #e5e5e5;
}
.divider::before {
  left: 0;
}
.divider::after {
  right: 0;
}
.divider-text {
  font-size: 24rpx;
  color: #999;
}
.other-btn-group {
  display: flex;
  justify-content: center;
}
.other-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 60rpx;
  border-radius: 16rpx;
  transition: all 0.3s;
  &:active {
    background: #f5f5f5;
  }
  &.guest {
    background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
    border: 2rpx dashed #ddd;
    &:active {
      background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
      border-color: #4CAF50;
    }
  }
}
.guest-icon {
  font-size: 56rpx;
  margin-bottom: 8rpx;
}
.other-text {
  font-size: 26rpx;
  color: #666;
}
</style>