import { get, post, put, del } from '@/utils/request'

/**
 * 计划相关API（本地存储 + 云端接口）
 */

// ==================== 本地存储（兼容现有组件） ====================

const STORAGE_KEYS = {
  currentPlan: 'weeklyPlan_current',
  templates: 'planTemplates',
  communityTemplates: 'communityTemplates',
  cardOrder: (date) => `planCardOrder_${date}`
}

export function getCurrentPlan() {
  const data = uni.getStorageSync(STORAGE_KEYS.currentPlan)
  if (data) {
    try { return JSON.parse(data) } catch (e) { return null }
  }
  return null
}

export function saveCurrentPlan(plan) {
  uni.setStorageSync(STORAGE_KEYS.currentPlan, JSON.stringify(plan))
  return Promise.resolve({ code: 200, data: plan })
}

export function getPlanTemplates() {
  const data = uni.getStorageSync(STORAGE_KEYS.templates)
  if (data) {
    try { return JSON.parse(data) } catch (e) { return [] }
  }
  return []
}

export function savePlanTemplate(template) {
  const templates = getPlanTemplates()
  const index = templates.findIndex(t => t.id === template.id)
  if (index > -1) { templates[index] = template }
  else { templates.push(template) }
  uni.setStorageSync(STORAGE_KEYS.templates, JSON.stringify(templates))
  return Promise.resolve({ code: 200, data: template })
}

export function deletePlanTemplate(id) {
  const templates = getPlanTemplates().filter(t => t.id !== id)
  uni.setStorageSync(STORAGE_KEYS.templates, JSON.stringify(templates))
  return Promise.resolve({ code: 200 })
}

export function getCommunityTemplates() {
  const data = uni.getStorageSync(STORAGE_KEYS.communityTemplates)
  if (data) {
    try { return JSON.parse(data) } catch (e) { return getDefaultCommunityTemplates() }
  }
  return getDefaultCommunityTemplates()
}

export function saveCommunityTemplate(template) {
  const templates = getCommunityTemplates()
  const index = templates.findIndex(t => t.id === template.id)
  if (index > -1) { templates[index] = template }
  else { templates.push({ ...template, id: 'ct_' + Date.now(), usageCount: 0, rating: 5 }) }
  uni.setStorageSync(STORAGE_KEYS.communityTemplates, JSON.stringify(templates))
  return Promise.resolve({ code: 200, data: template })
}

export function getCardOrder(date) {
  const key = STORAGE_KEYS.cardOrder(date)
  const data = uni.getStorageSync(key)
  if (data) {
    try { return JSON.parse(data) } catch (e) { return null }
  }
  return null
}

export function saveCardOrder(date, order) {
  const key = STORAGE_KEYS.cardOrder(date)
  uni.setStorageSync(key, JSON.stringify(order))
  return Promise.resolve({ code: 200 })
}

function getDefaultCommunityTemplates() {
  const templates = [
    {
      id: 'ct_1', name: '马拉松备赛第3周', authorName: '跑步达人',
      tags: ['马拉松', '进阶'], targetAudience: '跑步爱好者',
      description: '逐步提升跑量，强化间歇训练，为比赛做好准备',
      usageCount: 128, rating: 4.8,
      weeklyStructure: [
        { dayOfWeek: 1, type: 'run', title: '400米间歇', details: { description: '8组400米间歇', duration: 45, runParams: { subType: 'interval', distance: 6, sets: 8 } } },
        { dayOfWeek: 2, type: 'run', title: '长距离慢跑', details: { description: '有氧基础跑', duration: 90, runParams: { subType: 'long', distance: 15 } } },
        { dayOfWeek: 3, type: 'rest', title: '休息日', details: { description: '完全休息', duration: 0 } },
        { dayOfWeek: 4, type: 'run', title: '节奏跑', details: { description: '5km节奏跑', duration: 50, runParams: { subType: 'tempo', distance: 9 } } },
        { dayOfWeek: 5, type: 'run', title: '轻松跑', details: { description: '恢复性慢跑', duration: 40, runParams: { subType: 'easy', distance: 6 } } },
        { dayOfWeek: 6, type: 'run', title: 'LSD', details: { description: '长距离耐力', duration: 120, runParams: { subType: 'long', distance: 20 } } },
        { dayOfWeek: 7, type: 'rest', title: '休息日', details: { description: '主动恢复', duration: 30 } }
      ]
    },
    {
      id: 'ct_2', name: '增肌塑形周计划', authorName: '健身教练',
      tags: ['增肌', '力量举'], targetAudience: '健身入门',
      description: '经典五分化训练，全身肌群均衡刺激',
      usageCount: 256, rating: 4.9,
      weeklyStructure: [
        { dayOfWeek: 1, type: 'strength', title: '胸部训练', details: { description: '卧推5×8、上斜哑铃4×12', duration: 60, exercises: [{ name: '卧推', sets: 5, reps: 8 }, { name: '上斜哑铃', sets: 4, reps: 12 }] } },
        { dayOfWeek: 2, type: 'strength', title: '背部训练', details: { description: '引体向上4组、杠铃划船4×10', duration: 60 } },
        { dayOfWeek: 3, type: 'rest', title: '休息日', details: { description: '完全休息', duration: 0 } },
        { dayOfWeek: 4, type: 'strength', title: '腿部训练', details: { description: '深蹲5×8、腿举4×12', duration: 70 } },
        { dayOfWeek: 5, type: 'strength', title: '肩部训练', details: { description: '肩推4×10、侧平举4×15', duration: 55 } },
        { dayOfWeek: 6, type: 'strength', title: '手臂训练', details: { description: '弯举+三头', duration: 50 } },
        { dayOfWeek: 7, type: 'run', title: '有氧日', details: { description: '慢跑45分钟', duration: 45 } }
      ]
    },
    {
      id: 'ct_3', name: '新手减脂计划', authorName: '减脂专家',
      tags: ['减脂', '新手'], targetAudience: '通用',
      description: '低强度有氧+全身力量，适合运动基础较弱人群',
      usageCount: 89, rating: 4.6,
      weeklyStructure: [
        { dayOfWeek: 1, type: 'run', title: '快走30分钟', details: { description: '保持心率120-130', duration: 30 } },
        { dayOfWeek: 2, type: 'strength', title: '全身力量', details: { description: '深蹲3×15、俯卧撑3×10', duration: 40 } },
        { dayOfWeek: 3, type: 'rest', title: '休息日', details: { description: '轻度散步', duration: 20 } },
        { dayOfWeek: 4, type: 'run', title: '间歇快走', details: { description: '快走+慢走循环', duration: 35 } },
        { dayOfWeek: 5, type: 'strength', title: '核心训练', details: { description: '卷腹、臀桥', duration: 35 } },
        { dayOfWeek: 6, type: 'yoga', title: '瑜伽拉伸', details: { description: '全身拉伸放松', duration: 45 } },
        { dayOfWeek: 7, type: 'rest', title: '完全休息', details: { description: '好好休息', duration: 0 } }
      ]
    }
  ]
  uni.setStorageSync(STORAGE_KEYS.communityTemplates, JSON.stringify(templates))
  return templates
}

// ==================== 云端API ====================

/**
 * 创建训练计划
 */
export function createPlan(data) {
  return post('/plan', data)
}

/**
 * 获取我的计划列表
 */
export function getMyPlans(params = {}) {
  return get('/plan/my', params)
}

/**
 * 获取社区计划列表
 */
export function getCommunityPlans(params = {}) {
  return get('/plan/community', params)
}

/**
 * 智能匹配计划
 */
export function matchPlans(data) {
  return post('/plan/match', data)
}

/**
 * 获取计划详情
 */
export function getPlanDetail(id) {
  return get(`/plan/${id}`)
}

/**
 * 更新计划
 */
export function updatePlan(id, data) {
  return put(`/plan/${id}`, data)
}

/**
 * 删除计划
 */
export function deletePlan(id) {
  return del(`/plan/${id}`)
}

/**
 * 上传计划到社区
 */
export function uploadPlanToCommunity(id) {
  return post(`/plan/${id}/upload`)
}

/**
 * 采用社区计划
 */
export function adoptPlan(id) {
  return post(`/plan/${id}/adopt`)
}

// ==================== 兼容旧接口 ====================

export function fetchPlanFromCloud(id) {
  return get(`/plan/${id}`)
}

export function uploadPlanToCloud(data) {
  return post('/plan', data)
}

export function fetchCommunityPlans(params) {
  return get('/plan/community', params)
}
