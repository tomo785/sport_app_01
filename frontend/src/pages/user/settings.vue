<template>
  <view class="settings-container">
    <!-- 修改密码 -->
    <view class="section">
      <view class="section-title">修改密码</view>
      <view class="form-list">
        <view class="form-item">
          <text class="form-label">旧密码</text>
          <input 
            class="form-input" 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入旧密码" 
            maxlength="20"
          />
        </view>
        <view class="form-item">
          <text class="form-label">新密码</text>
          <input 
            class="form-input" 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码（6-20位）" 
            maxlength="20"
          />
        </view>
        <view class="form-item">
          <text class="form-label">确认密码</text>
          <input 
            class="form-input" 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            maxlength="20"
          />
        </view>
      </view>
      <view class="btn-wrap">
        <button class="primary-btn" :loading="saving" @click="handleUpdatePassword">确认修改</button>
      </view>
    </view>

    <!-- 其他设置 -->
    <view class="section">
      <view class="section-title">其他</view>
      <view class="menu-list">
        <view class="menu-item" @click="goToAiSettings">
          <text class="menu-text">AI 配置</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
        <view class="menu-item" @click="clearCache">
          <text class="menu-text">清除缓存</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
        <view class="menu-item" @click="checkVersion">
          <text class="menu-text">检查更新</text>
          <AppIcon class="menu-arrow" name="arrowRight" size="28" bold />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { updatePassword } from '../../api/user'

const saving = ref(false)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleUpdatePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value

  if (!oldPassword.trim()) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }
  if (!newPassword.trim()) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  if (newPassword.length < 6 || newPassword.length > 20) {
    uni.showToast({ title: '新密码长度需为6-20位', icon: 'none' })
    return
  }
  if (newPassword !== confirmPassword) {
    uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
    return
  }

  saving.value = true
  try {
    const res = await updatePassword({
      oldPassword: oldPassword.trim(),
      newPassword: newPassword.trim(),
      confirmPassword: confirmPassword.trim()
    })
    if (res.code === 200) {
      uni.showToast({ title: '密码修改成功', icon: 'success' })
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      uni.showToast({ title: res.message || '修改失败', icon: 'none' })
    }
  } catch (e) {
    console.error('修改密码失败:', e)
    uni.showToast({ title: e.message || '修改失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

const clearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    }
  })
}

const goToAiSettings = () => {
  uni.navigateTo({ url: '/pages/user/ai-settings' })
}

const checkVersion = () => {
  uni.showToast({ title: '当前已是最新版本', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.settings-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.section {
  margin: 20rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 28rpx;
  color: #999;
  padding: 0 30rpx 16rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-list {
  padding: 0 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .form-label {
    width: 160rpx;
    font-size: 30rpx;
    color: #333;
  }

  .form-input {
    flex: 1;
    font-size: 30rpx;
    color: #333;
    text-align: right;
  }
}

.btn-wrap {
  padding: 30rpx 30rpx 10rpx;
}

.primary-btn {
  width: 100%;
  height: 84rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 16rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    opacity: 0.9;
  }
}

.menu-list {
  padding: 0 30rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f9f9f9;
  }

  .menu-text {
    font-size: 30rpx;
    color: #333;
  }

  .menu-arrow {
    width: 52rpx;
    height: 52rpx;
    color: #374151;
    flex-shrink: 0;
  }
}
</style>
