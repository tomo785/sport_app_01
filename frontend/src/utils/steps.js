/**
 * 今日步数获取工具
 * 策略（按优先级）：
 * 1. App 端：尝试 plus.health / plus.pedometer 读取系统原生步数
 * 2. 微信小程序 / App：尝试 uni.getWeRunData（返回加密数据需后端解密）
 * 3. 所有平台：降级到后端 API (/stats/today)
 * 4. 全部失败：使用本地缓存或演示数据
 */

import { getTodayStats } from '@/api/stats'

const STEPS_CACHE_KEY = 'today_steps_cache'
const STEPS_CACHE_DATE_KEY = 'today_steps_cache_date'

/**
 * 获取今日步数（统一入口）
 * @returns {Promise<{steps: number, source: string}>}
 */
export function fetchTodaySteps() {
  return new Promise((resolve) => {
    const today = formatDate(new Date())

    // 先尝试读取本地缓存（当日有效）
    const cachedDate = uni.getStorageSync(STEPS_CACHE_DATE_KEY)
    const cached = uni.getStorageSync(STEPS_CACHE_KEY)
    if (cachedDate === today && cached && cached.steps > 0) {
      console.log('[步数] 使用本地缓存:', cached)
      resolve({ ...cached, fromCache: true })
      return
    }

    // 按平台优先级获取
    const tryChain = []

    // #ifdef APP-PLUS
    tryChain.push(tryNativeHealthSteps)
    // #endif

    // #ifdef MP-WEIXIN
    tryChain.push(tryWeRunData)
    // #endif

    // 通用降级：后端 API
    tryChain.push(tryBackendSteps)

    // 全部失败后的最终降级
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
      if (res && res.steps >= 0) {
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

/**
 * 尝试 App 端原生健康数据（plus.health / plus.pedometer）
 */
function tryNativeHealthSteps() {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    try {
      // 1. 尝试 plus.health（manifest.json 中已声明 Health 模块）
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
              console.log('[步数] plus.health 成功:', steps)
              resolve({ steps, source: 'health', raw: res })
            },
            fail: (err) => reject(err || new Error('plus.health fail'))
          })
          return
        }
      }

      // 2. 尝试 plus.pedometer（部分平台支持）
      if (typeof plus !== 'undefined' && plus.pedometer) {
        const api = plus.pedometer.getStepCount || plus.pedometer.getSteps || plus.pedometer.getCurrentStepCount
        if (typeof api === 'function') {
          api.call(plus.pedometer, {
            success: (res) => {
              const steps = parseSteps(res)
              console.log('[步数] plus.pedometer 成功:', steps)
              resolve({ steps, source: 'pedometer', raw: res })
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

/**
 * 尝试微信运动数据（uni.getWeRunData）
 * 返回加密数据，需后端解密才能真正使用步数
 */
function tryWeRunData() {
  return new Promise((resolve, reject) => {
    if (typeof uni.getWeRunData !== 'function') {
      reject(new Error('getWeRunData not supported'))
      return
    }

    uni.getWeRunData({
      success: (res) => {
        // 返回的是加密数据，前端无法直接解密
        // 这里将加密数据传给调用方，由调用方决定是否上报后端
        console.log('[步数] uni.getWeRunData 成功，返回加密数据')
        resolve({
          steps: 0, // 前端无法直接解析，标记为需要解密
          source: 'werun_encrypted',
          encryptedData: res.encryptedData,
          iv: res.iv,
          needDecrypt: true
        })
      },
      fail: (err) => reject(err || new Error('getWeRunData fail'))
    })
  })
}

/**
 * 尝试后端 API（/stats/today）
 */
function tryBackendSteps() {
  return new Promise((resolve, reject) => {
    getTodayStats()
      .then((res) => {
        if (res.code === 200 && res.data) {
          const steps = res.data.steps ?? res.data.stepCount ?? 0
          const calories = res.data.calories ?? 0
          const distance = res.data.distance ?? 0
          const duration = res.data.duration ?? 0
          console.log('[步数] 后端 API 成功:', steps)
          resolve({
            steps: Number(steps) || 0,
            calories: Number(calories) || 0,
            distance: Number(distance) || 0,
            duration: Number(duration) || 0,
            source: 'backend',
            raw: res.data
          })
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
 * 上报步数到后端（用于将原生读取到的步数同步保存）
 * @param {Object} data
 */
export async function reportStepsToBackend(data) {
  // 如果项目后续新增了 /stats/report-steps 接口，可在这里调用
  // 目前仅做本地缓存
  const today = formatDate(new Date())
  uni.setStorageSync(STEPS_CACHE_KEY, { ...data, date: today })
  uni.setStorageSync(STEPS_CACHE_DATE_KEY, today)
}
