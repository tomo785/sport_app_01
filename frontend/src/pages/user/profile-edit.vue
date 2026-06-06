<template>
  <view class="edit-container">
    <!-- 头像编辑 -->
    <view class="avatar-section" @click="chooseAvatar">
      <image class="avatar-img" :src="form.avatar || '/static/images/default-avatar.png'" mode="aspectFill"></image>
      <text class="avatar-tip">点击更换头像</text>
    </view>
    <!-- 表单区域 -->
    <view class="form-section">
      <view class="form-item">
        <text class="form-label">昵称</text>
        <input class="form-input" v-model="form.nickname" placeholder="请输入昵称" maxlength="20" />
      </view>
      <view class="form-item">
        <text class="form-label">性别</text>
        <view class="gender-options">
          <view
            class="gender-option"
            :class="{ active: form.gender === 1 }"
            @click="form.gender = 1"
          >
            <AppIcon class="gender-icon" name="man" size="30" />
            <text class="gender-text">男</text>
          </view>
          <view
            class="gender-option"
            :class="{ active: form.gender === 2 }"
            @click="form.gender = 2"
          >
            <AppIcon class="gender-icon" name="woman" size="30" />
            <text class="gender-text">女</text>
          </view>
          <view
            class="gender-option"
            :class="{ active: form.gender === 0 }"
            @click="form.gender = 0"
          >
            <text class="gender-icon">?</text>
            <text class="gender-text">保密</text>
          </view>
        </view>
      </view>
      <view class="form-item">
        <text class="form-label">年龄</text>
        <input class="form-input" v-model.number="form.age" type="number" placeholder="请输入年龄" />
      </view>
      <view class="form-item">
        <text class="form-label">身高 (cm)</text>
        <input class="form-input" v-model.number="form.height" type="number" placeholder="请输入身高" />
      </view>
      <view class="form-item">
        <text class="form-label">体重 (kg)</text>
        <input class="form-input" v-model="form.weight" type="digit" placeholder="请输入体重" />
      </view>
    </view>
    <view class="form-section health-section">
      <view class="section-title">健康指标</view>
      <view class="form-item">
        <text class="form-label">腰围 (cm)</text>
        <input class="form-input" v-model="form.waistCircumference" type="digit" placeholder="请输入腰围" />
      </view>
      <view class="form-item">
        <text class="form-label">收缩压</text>
        <input class="form-input" v-model.number="form.systolicPressure" type="number" placeholder="mmHg" />
      </view>
      <view class="form-item">
        <text class="form-label">舒张压</text>
        <input class="form-input" v-model.number="form.diastolicPressure" type="number" placeholder="mmHg" />
      </view>
      <view class="form-item">
        <text class="form-label">静息心率</text>
        <input class="form-input" v-model.number="form.restingHeartRate" type="number" placeholder="次/分" />
      </view>
      <view class="form-item">
        <text class="form-label">运动平均心率</text>
        <input class="form-input" v-model.number="form.avgExerciseHeartRate" type="number" placeholder="次/分" />
      </view>
      <view class="form-item">
        <text class="form-label">运动最高心率</text>
        <input class="form-input" v-model.number="form.maxHeartRate" type="number" placeholder="次/分" />
      </view>
    </view>
    <!-- 保存按钮 -->
    <view class="btn-section">
      <button class="save-btn" :loading="saving" @click="handleSave">保存</button>
    </view>
  </view>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getUserInfo, updateUserInfo, uploadAvatar } from '../../api/user'
const userStore = useUserStore()
const saving = ref(false)
const form = ref({
  nickname: '',
  avatar: '',
  gender: 0,
  age: null,
  height: null,
  weight: '',
  waistCircumference: '',
  systolicPressure: null,
  diastolicPressure: null,
  restingHeartRate: null,
  avgExerciseHeartRate: null,
  maxHeartRate: null
})
onMounted(() => {
  loadUserInfo()
})
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      const data = res.data
      form.value = {
        nickname: data.nickname || '',
        avatar: data.avatar || '',
        gender: data.gender ?? 0,
        age: data.age || null,
        height: data.height || null,
        weight: data.weight ? String(data.weight) : '',
        waistCircumference: data.waistCircumference ? String(data.waistCircumference) : '',
        systolicPressure: data.systolicPressure || null,
        diastolicPressure: data.diastolicPressure || null,
        restingHeartRate: data.restingHeartRate || null,
        avgExerciseHeartRate: data.avgExerciseHeartRate || null,
        maxHeartRate: data.maxHeartRate || null
      }
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
    uni.showToast({ title: '获取信息失败', icon: 'none' })
  }
}
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 先本地预览
      form.value.avatar = tempFilePath
      // 上传头像
      uploadAvatarFile(tempFilePath)
    }
  })
}
const uploadAvatarFile = async (filePath) => {
  try {
    const uploadRes = await uploadAvatar(filePath)
    const data = JSON.parse(uploadRes.data)
    if (data.code === 200 && data.data) {
      form.value.avatar = data.data
      uni.showToast({ title: '头像上传成功', icon: 'success' })
    } else {
      uni.showToast({ title: data.message || '上传失败', icon: 'none' })
    }
  } catch (e) {
    console.error('上传头像失败:', e)
    uni.showToast({ title: '上传失败', icon: 'none' })
  }
}
const handleSave = async () => {
  if (!form.value.nickname.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  const payload = {
    nickname: form.value.nickname.trim(),
    avatar: form.value.avatar,
    gender: form.value.gender,
    age: form.value.age ? parseInt(form.value.age) : null,
    height: form.value.height ? parseInt(form.value.height) : null,
    weight: form.value.weight ? parseFloat(form.value.weight) : null,
    waistCircumference: form.value.waistCircumference ? parseFloat(form.value.waistCircumference) : null,
    systolicPressure: form.value.systolicPressure ? parseInt(form.value.systolicPressure) : null,
    diastolicPressure: form.value.diastolicPressure ? parseInt(form.value.diastolicPressure) : null,
    restingHeartRate: form.value.restingHeartRate ? parseInt(form.value.restingHeartRate) : null,
    avgExerciseHeartRate: form.value.avgExerciseHeartRate ? parseInt(form.value.avgExerciseHeartRate) : null,
    maxHeartRate: form.value.maxHeartRate ? parseInt(form.value.maxHeartRate) : null
  }
  saving.value = true
  try {
    const res = await updateUserInfo(payload)
    if (res.code === 200) {
      // 更新本地存储
      if (res.data) {
        userStore.setUserInfo(res.data)
      }
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 800)
    } else {
      uni.showToast({ title: res.message || '保存失败', icon: 'none' })
    }
  } catch (e) {
    console.error('保存资料失败:', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}
</script>
<style lang="scss" scoped>
.edit-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  background: #fff;
  margin-bottom: 20rpx;
  .avatar-img {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: #eee;
    margin-bottom: 20rpx;
  }
  .avatar-tip {
    font-size: 28rpx;
    color: #666;
  }
}
.form-section {
  background: #fff;
  padding: 0 30rpx;
}
.health-section {
  margin-top: 20rpx;
}
.section-title {
  padding: 26rpx 0 8rpx;
  font-size: 26rpx;
  color: #8a94a6;
  font-weight: 600;
}
.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  &:last-child {
    border-bottom: none;
  }
  .form-label {
    width: 180rpx;
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
  }
  .form-input {
    flex: 1;
    font-size: 30rpx;
    color: #333;
    text-align: right;
  }
}
.gender-options {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  .gender-option {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 24rpx;
    border-radius: 32rpx;
    background: #f5f5f5;
    transition: all 0.2s;
    &.active {
      background: #4CAF50;
      .gender-icon,
      .gender-text {
        color: #fff;
      }
    }
    .gender-icon {
      font-size: 26rpx;
      color: #666;
    }
    .gender-text {
      font-size: 26rpx;
      color: #666;
    }
  }
}
.btn-section {
  margin: 60rpx 40rpx 0;
  .save-btn {
    width: 100%;
    height: 90rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
    color: #fff;
    font-size: 32rpx;
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
}
</style>
