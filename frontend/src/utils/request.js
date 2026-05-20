// 基础URL配置
//const BASE_URL = 'http://localhost:8080/api/v1'
const BASE_URL = 'http://10.106.35.133:8080/api/v1'
//const BASE_URL = 'https://1el2459ul5916.vicp.fun:433'
// 请求拦截器
const request = (options) => {
  // 获取token
  const token = uni.getStorageSync('token')

  return new Promise((resolve, reject) => {
    const headers = {
      'Content-Type': 'application/json',
      ...options.header
    }
    // token 为空时不发送 Authorization 头，避免后端收到空字符串
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data)
          } else if (res.data.code === 401) {
            // 未授权，清除token并跳转登录
            uni.removeStorageSync('token')
            uni.showToast({
              title: '请先登录',
              icon: 'none',
              duration: 1500
            })
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/user/login' })
            }, 1500)
            reject(res.data)
          } else {
            // 业务错误
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none',
              duration: 2000
            })
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          // HTTP 401
          uni.removeStorageSync('token')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 1500
          })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/user/login' })
          }, 1500)
          reject(res)
        } else {
          // HTTP错误
          uni.showToast({
            title: '网络请求失败',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none',
          duration: 2000
        })
        reject(err)
      }
    })
  })
}

// GET请求
export const get = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

// POST请求
export const post = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

// PUT请求
export const put = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

// DELETE请求
export const del = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

export default request
