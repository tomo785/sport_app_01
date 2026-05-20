import { get, post } from '@/utils/request'

/**
 * 数据统计相关API
 */

/**
 * 获取今日统计
 */
export function getTodayStats() {
  return get('/stats/today')
}

/**
 * 获取指定日期统计
 * @param {string} date - 日期 (yyyy-MM-dd)
 */
export function getDailyStats(date) {
  return get('/stats/daily', { date })
}

/**
 * 获取日期范围统计列表
 * @param {string} startDate - 开始日期 (yyyy-MM-dd)
 * @param {string} endDate - 结束日期 (yyyy-MM-dd)
 */
export function getDailyStatsList(startDate, endDate) {
  return get('/stats/daily/list', { startDate, endDate })
}

/**
 * 获取统计汇总
 */
export function getStatsSummary() {
  return get('/stats/summary')
}

/**
 * 获取周趋势
 */
export function getWeeklyTrend() {
  return get('/stats/trend/weekly')
}

/**
 * 获取月趋势
 */
export function getMonthlyTrend() {
  return get('/stats/trend/monthly')
}

/**
 * 获取年趋势
 * @param {number} year - 年份
 */
export function getYearlyTrend(year) {
  return get('/stats/trend/yearly', { year })
}

/**
 * 上报今日步数（将 App 原生读取或微信运动解密后的步数同步到后端）
 * @param {Object} data
 * @param {number} data.steps - 步数
 * @param {number} data.calories - 卡路里（可选）
 * @param {number} data.distance - 距离（可选）
 * @param {number} data.duration - 时长（可选）
 * @param {string} data.source - 数据来源：health / pedometer / werun / backend
 */
export function reportTodaySteps(data) {
  return post('/stats/steps/today', data)
}

/**
 * 上报微信运动加密数据（供后端解密）
 * @param {Object} data
 * @param {string} data.encryptedData - 微信运动加密数据
 * @param {string} data.iv - 加密算法的初始向量
 */
export function reportWeRunData(data) {
  return post('/stats/werun/decrypt', data)
}
