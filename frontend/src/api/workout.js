import { post, get, put } from '../utils/request'
/**
 * 运动记录相关API
 */
/**
 * 开始运动
 * @param {Object} data 运动数据
 * @param {Number} data.type 运动类型 1跑步 2健走 3骑行
 */
export function startWorkout(data) {
  const type = data && data.type ? data.type : 1
  return post(`/workout/start?type=${encodeURIComponent(type)}`, data)
}
/**
 * 上传轨迹点
 * @param {Object} data 轨迹数据
 * @param {Number} data.recordId 运动记录ID
 * @param {Array} data.trajectory 轨迹点数组
 */
export function uploadTrajectory(data) {
  return post('/workout/trajectory', data)
}
/**
 * 暂停运动
 * @param {Number} recordId 运动记录ID
 */
export function pauseWorkout(recordId) {
  return put(`/workout/pause?recordId=${encodeURIComponent(recordId)}`)
}
/**
 * 继续运动
 * @param {Number} recordId 运动记录ID
 */
export function resumeWorkout(recordId) {
  return put(`/workout/resume?recordId=${encodeURIComponent(recordId)}`)
}
/**
 * 结束运动
 * @param {Number} recordId 运动记录ID
 */
export function finishWorkout(recordId, data = {}) {
  return put(`/workout/finish?recordId=${encodeURIComponent(recordId)}`, data)
}
/**
 * 获取运动详情
 * @param {Number} id 运动记录ID
 */
export function getWorkoutDetail(id) {
  return get(`/workout/${id}`)
}
/**
 * 获取运动列表
 * @param {Object} params 查询参数
 * @param {Number} params.page 页码
 * @param {Number} params.size 每页数量
 * @param {Number} params.type 运动类型
 * @param {String} params.startDate 开始日期
 * @param {String} params.endDate 结束日期
 */
export function getWorkoutList(params) {
  return get('/workout/list', params)
}
/**
 * 获取运动轨迹
 * @param {Number} id 运动记录ID
 */
export function getWorkoutTrajectory(id) {
  return get(`/workout/${id}/trajectory`)
}