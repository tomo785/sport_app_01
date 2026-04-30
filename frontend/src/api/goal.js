import { get, post, put, del } from '@/utils/request'

/**
 * 目标相关API
 */

/**
 * 创建目标
 * @param {Object} data - 目标数据 { type, targetValue, startDate, endDate }
 */
export function createGoal(data) {
  return post('/goal', data)
}

/**
 * 获取目标列表
 * @param {Object} params - 查询参数 { status }
 */
export function getGoalList(params = {}) {
  return get('/goal', params)
}

/**
 * 获取目标详情
 * @param {number} id - 目标ID
 */
export function getGoalDetail(id) {
  return get(`/goal/${id}`)
}

/**
 * 更新目标
 * @param {number} id - 目标ID
 * @param {Object} data - 更新数据
 */
export function updateGoal(id, data) {
  return put(`/goal/${id}`, data)
}

/**
 * 删除目标
 * @param {number} id - 目标ID
 */
export function deleteGoal(id) {
  return del(`/goal/${id}`)
}

/**
 * 完成目标
 * @param {number} id - 目标ID
 */
export function completeGoal(id) {
  return put(`/goal/${id}/complete`)
}
