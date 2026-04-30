import { get } from '@/utils/request'

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
