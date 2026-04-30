import { get, post } from '../utils/request'

/**
 * 任务卡片相关API（上区块）
 */

/**
 * 获取今日任务
 */
export function getTodayTask() {
  return get('/task/today')
}

/**
 * 获取任务看板（上区块完整数据）
 * 包含：今日任务、进度、数据看板、训练趋势
 */
export function getTaskDashboard() {
  return get('/task/dashboard')
}

/**
 * 获取7天训练趋势
 */
export function getWeeklyTrend() {
  return get('/task/trend/weekly')
}

/**
 * 获取30天训练趋势
 */
export function getMonthlyTrend() {
  return get('/task/trend/monthly')
}

/**
 * 开始今日任务
 */
export function startTodayTask() {
  return post('/task/start')
}

/**
 * 完成动作
 * @param {Number} taskExerciseId 任务动作记录ID
 * @param {Object} data 完成数据
 * @param {Number} data.actualReps 实际完成次数
 * @param {Number} data.actualDuration 实际用时(秒)
 */
export function completeExercise(taskExerciseId, data = {}) {
  return post(`/task/exercise/${taskExerciseId}/complete`, null, {
    params: data
  })
}

/**
 * 更新任务进度
 * @param {Number} taskId 任务ID
 */
export function updateTaskProgress(taskId) {
  return post(`/task/${taskId}/update-progress`)
}

/**
 * 创建今日任务
 */
export function createTodayTask() {
  return post('/task/create-today')
}

// ==================== 任务流程图API ====================

/**
 * 获取任务流程图数据
 * @param {String} date 日期 (YYYY-MM-DD)
 * @returns {Promise} 包含目标信息和任务链的数据
 */
export function getTaskFlow(date) {
  return get('/task/flow', { date })
}

/**
 * 获取指定日期的任务流程
 * @param {String} date 日期 (YYYY-MM-DD)
 */
export function getTaskFlowByDate(date) {
  return get('/task/flow/date', { date })
}

/**
 * 更新任务顺序
 * @param {Array} orderData 排序数据 [{taskId, order}, ...]
 */
export function updateTaskOrder(orderData) {
  return post('/task/flow/reorder', orderData)
}

/**
 * 开始指定任务
 * @param {Number} taskId 任务ID
 */
export function startTask(taskId) {
  return post(`/task/${taskId}/start`)
}

/**
 * 更新任务优先级
 * @param {Number} taskId 任务ID
 * @param {String} priority 优先级 (high/medium/low)
 */
export function updateTaskPriority(taskId, priority) {
  return post(`/task/${taskId}/priority`, { priority })
}

/**
 * 获取任务详情
 * @param {Number} taskId 任务ID
 */
export function getTaskDetail(taskId) {
  return get(`/task/${taskId}/detail`)
}

/**
 * 批量更新任务状态
 * @param {Array} taskIds 任务ID数组
 * @param {Number} status 状态值 (0未开始 1进行中 2已完成)
 */
export function batchUpdateTaskStatus(taskIds, status) {
  return post('/task/batch/status', { taskIds, status })
}
