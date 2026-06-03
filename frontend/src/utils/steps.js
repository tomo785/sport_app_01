/**
 * 今日步数获取工具
 * 策略（按优先级）：
 * 1. 微信小程序：button open-type=getWeRunData 触发（需用户点击）
 * 2. App 端：plus.health / plus.pedometer
 * 3. 所有平台：降级到后端 API (/stats/today)
 * 4. 全部失败：本地缓存兜底
 *
 * ⚠ 微信小程序注意：
 *   - getWeRunData 必须由 <button open-type="getWeRunData"> 用户点击触发
 *   - 加密数据需发往后端用 session_key 解密
 *   - 后端已提供 POST /stats/werun/decrypt 接口
 *
 * ⚠ H5 注意事项：
 *   - 微信小程序 API 不可用，会降级到后端 API 或演示数据
 */

import { getTodayStats, reportWeRunData } from '@/api/stats'

const STEPS_CACHE_KEY = 'today_steps_cache'
const STEPS_CACHE_DATE_KEY = 'today_steps_cache_date'

/**
 * 获取今日步数（统一入口）
 * @returns {Promise<{steps: number, source: string, calories?: number, distance?: number, duration?: number}>}
 */
export function fetchTodaySteps() {
  return new Promise((resolve) => {
    const today = formatDate(new Date())

    // 先尝试读取本地缓存（当日有效）
    const cachedDate = uni.getStorageSync(STEPS_CACHE_DATE_KEY)
    const cached = uni.getStorageSync(STEPS_CACHE_KEY)
    if (cachedDate === today && cached && cached.steps > 0) {
      resolve({ ...cached, fromCache: true })
      return
    }

    // 按平台优先级构建获取链
    const tryChain = []

    // #ifdef MP-WEIXIN
    tryChain.push(() => tryWeRunWithAuth())
    // #endif

    // #ifdef APP-PLUS
    tryChain.push(tryNativeHealthSteps)
    // #endif

    // 通用降级：后端 API
    tryChain.push(tryBackendSteps)

    // 全部失败后的最终降级：演示数据
    tryChain.push(() => Promise.resolve({ steps: 0, source: 'fallback' }))

    executeChain(tryChain, 0, resolve, today)
  })
}

/**
 * 顺序执行获取链，任一成功即返回
 */
function executeChain(chain, index, resolve, today) {
  if (index >= chain.length) {
    resolve({ steps: 0, source: 'none' })
    return
  }

  const fn = chain[index]
  fn()
    .then((res) => {
      if (res && res.steps >= 0 && !res.needDecrypt) {
        // 缓存有效数据
        uni.setStorageSync(STEPS_CACHE_KEY, res)
        uni.setStorageSync(STEPS_CACHE_DATE_KEY, today)
        resolve(res)
      } else {
        executeChain(chain, index + 1, resolve, today)
      }
    })
    .catch((err) => {
      console.warn(`[步数] 来源 ${fn.name} 失败:`, err.message || err)
      executeChain(chain, index + 1, resolve, today)
    })
}

// ===================== 微信小程序：button 触发模式 =====================
// 使用方式：在页面中放置
//   <button open-type="getWeRunData" @getwerundata="onWeRunData">
//     获取微信运动步数
//   </button>
// 然后在 script 中调用 handleWeRunData(e)

/**
 * 处理 button 返回的微信运动加密数据
 * 在页面的 @getwerundata 回调中调用此函数
 *
 * @param {Event} e - button 的 getwerundata 事件对象
 * @returns {Promise<number>} 解密后的步数
 */
export async function handleWeRunData(e) {
  if (e.detail.errMsg !== 'getWeRunData:ok') {
    const msg = e.detail.errMsg === 'getWeRunData:fail auth deny'
      ? '请授权微信运动步数'
      : '获取步数失败'
    uni.showToast({ title: msg, icon: 'none' })
    throw new Error(msg)
  }

  const { encryptedData, iv } = e.detail

  // 发送到后端解密（后端用 session_key + AES-128-CBC 解密）
  const res = await reportWeRunData({ encryptedData, iv })
  if (res.code === 200 && res.data) {
    const steps = res.data.steps || res.data.stepCount || 0
    const calories = res.data.calories || 0
    const distance = res.data.distance || 0
    const duration = res.data.duration || 0

    // 缓存到本地
    const today = formatDate(new Date())
    uni.setStorageSync(STEPS_CACHE_KEY, { steps, calories, distance, duration, source: 'werun_decrypted' })
    uni.setStorageSync(STEPS_CACHE_DATE_KEY, today)

    return steps
  }
  throw new Error(res.message || '解密失败')
}

/**
 * 小程序内自动尝试授权并获取步数（无需 button 触发）
 * 注意：该方式可能受微信基础库版本限制
 */
async function tryWeRunWithAuth() {
  // 1. 检查授权状态
  const setting = await uni.getSetting()
  const hasAuth = setting.authSetting['scope.werun'] === true

  if (!hasAuth) {
    // 未授权 → 尝试弹窗授权（需用户确认）
    try {
      await uni.authorize({ scope: 'scope.werun' })
    } catch (e) {
      throw new Error('用户拒绝授权微信运动')
    }
  }

  // 2. 登录获取 code（session_key 在服务端换取）
  const loginRes = await uni.login({ provider: 'weixin' })
  if (loginRes.errMsg !== 'login:ok') {
    throw new Error('微信登录失败')
  }

  // 3. 获取加密步数数据
  const weRunRes = await uni.getWeRunData({})
  if (weRunRes.errMsg !== 'getWeRunData:ok') {
    throw new Error('getWeRunData 失败: ' + (weRunRes.errMsg || ''))
  }

  // 4. 发往后端解密
  const decryptRes = await reportWeRunData({
    encryptedData: weRunRes.encryptedData,
    iv: weRunRes.iv
  })

  if (decryptRes.code === 200 && decryptRes.data) {
    const steps = decryptRes.data.steps || decryptRes.data.stepCount || 0
    return {
      steps: Number(steps) || 0,
      calories: decryptRes.data.calories || 0,
      distance: decryptRes.data.distance || 0,
      duration: decryptRes.data.duration || 0,
      source: 'werun_decrypted',
      raw: decryptRes.data
    }
  }
  throw new Error(decryptRes.message || '微信步数解密失败')
}

// ===================== App 端：原生健康 API =====================
/**
 * 尝试 App 端原生健康数据（plus.health / plus.pedometer）
 */
function tryNativeHealthSteps() {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    try {
      // 1. 尝试 plus.health
      if (typeof plus !== 'undefined' && plus.health) {
        const api = plus.health.getStepCount || plus.health.getSteps
        if (typeof api === 'function') {
          const startOfDay = new Date()
          startOfDay.setHours(0, 0, 0, 0)

          api.call(plus.health, {
            startDate: startOfDay.toISOString(),
            endDate: new Date().toISOString(),
            success: (res) => {
              const steps = parseSteps(res)
              if (steps > 0) {
                resolve({ steps, source: 'health', raw: res })
              } else {
                reject(new Error('health returned 0 steps'))
              }
            },
            fail: (err) => reject(err || new Error('plus.health fail'))
          })
          return
        }
      }

      // 2. 尝试 plus.pedometer
      if (typeof plus !== 'undefined' && plus.pedometer) {
        const api = plus.pedometer.getStepCount || plus.pedometer.getSteps || plus.pedometer.getCurrentStepCount
        if (typeof api === 'function') {
          api.call(plus.pedometer, {
            success: (res) => {
              const steps = parseSteps(res)
              if (steps > 0) {
                resolve({ steps, source: 'pedometer', raw: res })
              } else {
                reject(new Error('pedometer returned 0 steps'))
              }
            },
            fail: (err) => reject(err || new Error('plus.pedometer fail'))
          })
          return
        }
      }
    } catch (e) {
      console.error('[步数] 原生 API 异常:', e)
    }
    // #endif

    reject(new Error('Native step API not available'))
  })
}

// ===================== 通用降级：后端 API =====================
/**
 * 尝试后端 API（/stats/today）
 */
function tryBackendSteps() {
  return new Promise((resolve, reject) => {
    getTodayStats()
      .then((res) => {
        if (res.code === 200 && res.data) {
          const steps = res.data.steps ?? res.data.stepCount ?? 0
          const data = {
            steps: Number(steps) || 0,
            calories: res.data.calories ?? 0,
            distance: res.data.distance ?? 0,
            duration: res.data.duration ?? 0,
            source: 'backend',
            raw: res.data
          }
          if (data.steps > 0) {
            resolve(data)
          } else {
            reject(new Error('Backend returned 0 steps'))
          }
        } else {
          reject(new Error('Backend stats invalid'))
        }
      })
      .catch((err) => {
        console.warn('[步数] 后端 API 失败:', err.message || err)
        reject(err)
      })
  })
}

// ===================== 工具函数 =====================
/**
 * 解析步数（兼容不同 API 返回格式）
 */
function parseSteps(res) {
  if (!res) return 0
  if (typeof res.steps === 'number') return res.steps
  if (typeof res.stepCount === 'number') return res.stepCount
  if (typeof res.count === 'number') return res.count
  if (typeof res === 'number') return res
  return 0
}

/**
 * 格式化日期为 yyyy-MM-dd
 */
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/**
 * 上报步数到后端
 */
export async function reportStepsToBackend(data) {
  const today = formatDate(new Date())
  uni.setStorageSync(STEPS_CACHE_KEY, { ...data, date: today })
  uni.setStorageSync(STEPS_CACHE_DATE_KEY, today)
}
