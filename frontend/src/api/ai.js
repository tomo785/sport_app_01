import { post } from '@/utils/request'

/**
 * AI 方案生成
 * @param {Object} data
 * @param {Array} data.messages - 对话消息列表
 */
export function generatePlan(data) {
  return post('/ai/generate-plan', data)
}
