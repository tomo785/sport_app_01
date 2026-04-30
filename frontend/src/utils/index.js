/**
 * 格式化时间
 * @param {Date|String|Number} date 日期
 * @param {String} format 格式
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''

  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化时长
 * @param {Number} seconds 秒数
 */
export function formatDuration(seconds) {
  if (!seconds) return '00:00:00'

  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60

  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/**
 * 格式化距离
 * @param {Number} meters 米
 */
export function formatDistance(meters) {
  if (!meters) return '0km'

  if (meters < 1000) {
    return `${meters}m`
  }
  return `${(meters / 1000).toFixed(2)}km`
}

/**
 * 格式化配速
 * @param {Number} secondsPerKm 每公里秒数
 */
export function formatPace(secondsPerKm) {
  if (!secondsPerKm || secondsPerKm === 0) return "0'00\""

  const min = Math.floor(secondsPerKm / 60)
  const sec = Math.floor(secondsPerKm % 60)
  return `${min}'${String(sec).padStart(2, '0')}"`
}

/**
 * 格式化卡路里
 * @param {Number} calories 卡路里
 */
export function formatCalories(calories) {
  if (!calories) return '0kcal'
  return `${calories}kcal`
}

/**
 * 获取运动类型名称
 * @param {Number} type 运动类型
 */
export function getWorkoutTypeName(type) {
  const types = {
    1: '跑步',
    2: '健走',
    3: '骑行'
  }
  return types[type] || '运动'
}

/**
 * 获取运动类型颜色
 * @param {Number} type 运动类型
 */
export function getWorkoutTypeColor(type) {
  const colors = {
    1: '#FF5722', // 跑步 - 橙色
    2: '#4CAF50', // 健走 - 绿色
    3: '#2196F3'  // 骑行 - 蓝色
  }
  return colors[type] || '#999999'
}

/**
 * 防抖函数
 * @param {Function} func 函数
 * @param {Number} delay 延迟时间
 */
export function debounce(func, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      func.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} func 函数
 * @param {Number} delay 延迟时间
 */
export function throttle(func, delay = 300) {
  let timer = null
  return function (...args) {
    if (!timer) {
      timer = setTimeout(() => {
        func.apply(this, args)
        timer = null
      }, delay)
    }
  }
}

/**
 * 手机号验证
 * @param {String} phone 手机号
 */
export function validatePhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证码验证
 * @param {String} code 验证码
 */
export function validateCode(code) {
  const reg = /^\d{6}$/
  return reg.test(code)
}
