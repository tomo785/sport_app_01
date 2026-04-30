/**
 * 认证相关工具函数
 */

/**
 * 检查是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  return !!uni.getStorageSync('token')
}

/**
 * 获取 Token
 * @returns {string}
 */
export function getToken() {
  return uni.getStorageSync('token') || ''
}

/**
 * 保存 Token
 * @param {string} token
 */
export function setToken(token) {
  uni.setStorageSync('token', token)
}

/**
 * 清除登录状态
 */
export function clearAuth() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
}

/**
 * 检查登录状态，未登录则跳转
 * @param {boolean} redirect - 是否自动跳转到登录页
 * @returns {boolean}
 */
export function checkLogin(redirect = true) {
  const token = getToken()
  if (!token && redirect) {
    uni.navigateTo({
      url: '/pages/user/login'
    })
    return false
  }
  return !!token
}

/**
 * 获取用户信息
 * @returns {object|null}
 */
export function getUserInfo() {
  try {
    return uni.getStorageSync('userInfo') || null
  } catch {
    return null
  }
}

/**
 * 保存用户信息
 * @param {object} userInfo
 */
export function setUserInfo(userInfo) {
  uni.setStorageSync('userInfo', userInfo)
}
