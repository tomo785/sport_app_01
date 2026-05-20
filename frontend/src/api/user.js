import { post, get, put } from '../utils/request'

/**
 * 用户相关API
 */

/**
 * 发送短信验证码
 * @param {String} phone 手机号
 * @param {Number} type 类型 1注册 2登录 3找回密码
 */
export function sendSmsCode(phone, type = 2) {
  return post('/user/sms-code', { phone, type })
}

/**
 * 手机号验证码登录
 * @param {String} phone 手机号
 * @param {String} code 验证码
 */
export function login(phone, code) {
  return post('/user/login', { phone, code })
}

/**
 * 账号密码登录
 * @param {String} username 账号
 * @param {String} password 密码
 */
export function loginByPassword(username, password) {
  return post('/user/login/password', { username, password })
}

/**
 * 用户注册
 * @param {Object} data 注册数据 { username, password }
 */
export function register(data) {
  return post('/user/register', data)
}

/**
 * 检查是否首次登录
 */
export function checkFirstLogin() {
  return get('/user/check-first-login')
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get('/user/info')
}

/**
 * 更新用户信息
 * @param {Object} data 用户数据
 */
export function updateUserInfo(data) {
  return put('/user/info', data)
}

/**
 * 修改密码
 * @param {Object} data { oldPassword, newPassword, confirmPassword }
 */
export function updatePassword(data) {
  return put('/user/password', data)
}

/**
 * 上传头像
 * @param {File} file 文件
 */
export function uploadAvatar(file) {
  return uni.uploadFile({
    url: 'http://localhost:8080/api/v1/user/avatar',
    filePath: file,
    name: 'file',
    header: {
      'Authorization': `Bearer ${uni.getStorageSync('token')}`
    }
  })
}
