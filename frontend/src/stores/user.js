import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, login as loginApi, loginByPassword as loginByPasswordApi, adminLogin as adminLoginApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(uni.getStorageSync('userInfo') || {})
  const isLoading = ref(false)
  const isFirstLogin = ref(false)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isGuest = computed(() => userInfo.value?.isGuest || false)
  const isAdmin = computed(() => userInfo.value?.isAdmin || false)
  const nickname = computed(() => userInfo.value?.nickname || '运动爱好者')
  const avatar = computed(() => userInfo.value?.avatar || '/static/icons/user.png')

  // Actions
  const setToken = (newToken) => {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  const clearUserData = () => {
    token.value = ''
    userInfo.value = {}
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  const fetchUserInfo = async () => {
    if (!token.value) return null
    
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
        uni.setStorageSync('userInfo', res.data)
        return res.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
    return null
  }

  // 验证码登录
  const login = async (phone, code) => {
    isLoading.value = true
    try {
      const res = await loginApi(phone, code)
      if (res.code === 200) {
        setToken(res.data.token)
        setUserInfo(res.data)
        // 标记不是首次登录
        isFirstLogin.value = false
        return { success: true, data: res.data }
      }
      return { success: false, message: res.message }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    } finally {
      isLoading.value = false
    }
  }

  // 密码登录
  const loginByPassword = async (phone, password) => {
    isLoading.value = true
    try {
      const res = await loginByPasswordApi(phone, password)
      if (res.code === 200) {
        setToken(res.data.token)
        setUserInfo(res.data)
        // 标记不是首次登录
        isFirstLogin.value = false
        return { success: true, data: res.data }
      }
      return { success: false, message: res.message }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    } finally {
      isLoading.value = false
    }
  }

  // 管理员登录
  const adminLogin = async (username, password) => {
    isLoading.value = true
    try {
      const res = await adminLoginApi(username, password)
      if (res.code === 200) {
        setToken(res.data.token)
        setUserInfo({
          ...res.data,
          isAdmin: true
        })
        isFirstLogin.value = false
        return { success: true, data: res.data }
      }
      return { success: false, message: res.message }
    } catch (error) {
      return { success: false, message: error.message || '管理员登录失败' }
    } finally {
      isLoading.value = false
    }
  }

  // 检查是否首次登录
  const checkFirstLogin = async () => {
    const hasLoggedBefore = uni.getStorageSync('hasLoggedBefore')
    if (!hasLoggedBefore && !isLoggedIn.value) {
      isFirstLogin.value = true
      return true
    }
    isFirstLogin.value = false
    return false
  }

  const logout = () => {
    clearUserData()
    // 清除游客模式标记
    uni.removeStorageSync('isGuestMode')
    // 清除首次登录标记，下次进入需要重新登录
    uni.removeStorageSync('hasLoggedBefore')
    // 跳转到登录页面
    uni.reLaunch({ url: '/pages/user/login' })
  }
  
  // 退出游客模式（跳转到登录页）
  const exitGuestMode = () => {
    clearUserData()
    uni.removeStorageSync('isGuestMode')
    uni.reLaunch({ url: '/pages/user/login' })
  }

  const checkLogin = (redirect = false) => {
    if (!isLoggedIn.value) {
      if (redirect) {
        uni.navigateTo({ url: '/pages/user/login' })
      }
      return false
    }
    return true
  }

  return {
    // State
    token,
    userInfo,
    isLoading,
    isFirstLogin,
    // Getters
    isLoggedIn,
    isGuest,
    isAdmin,
    nickname,
    avatar,
    // Actions
    setToken,
    setUserInfo,
    clearUserData,
    fetchUserInfo,
    login,
    loginByPassword,
    adminLogin,
    checkFirstLogin,
    logout,
    exitGuestMode,
    checkLogin
  }
})
