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

/**
 * 生成默认周计划（新手入门方案）
 * 当用户没有任何计划时自动创建，让用户首页就能看到可用方案
 * 经过人性化优化，鼓励用户坚持运动
 * 使用多活动格式，每天支持多个项目
 */
export function createDefaultWeeklyPlan() {
  const now = new Date()
  const day = now.getDay()
  const diff = now.getDate() - day + (day === 0 ? -6 : 1)
  const monday = new Date(now.setDate(diff))
  const weekStart = `${monday.getFullYear()}-${String(monday.getMonth() + 1).padStart(2, '0')}-${String(monday.getDate()).padStart(2, '0')}`

  const baseId = Date.now()
  const plan = {
    id: 'wp_default_' + baseId,
    name: '轻松入门周计划',
    weekStartDate: weekStart,
    days: [
      // 周一：热身跑 + 核心训练
      {
        id: 'dp_' + baseId + '_1',
        dayOfWeek: 1,
        order: 1,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '🏃', type: 'run', name: '晨间唤醒跑', duration: 35,
            tags: ['有氧', '跑步'],
            summary: '5km · 配速 6:00/km',
            details: { description: '用舒适的配速跑完 5 公里，感受清晨的微风，让身体慢慢苏醒。坚持比强度更重要，今天是最棒的开端！', duration: 35, runParams: { subType: 'easy', distance: 5 } }
          },
          {
            icon: '🧘', type: 'yoga', name: '跑后拉伸', duration: 10,
            tags: ['拉伸', '恢复'],
            summary: '哈他瑜伽',
            details: { description: '跑后全身拉伸，帮助肌肉恢复', duration: 10, yogaStyle: 'hatha' }
          }
        ]
      },
      // 周二：上肢力量
      {
        id: 'dp_' + baseId + '_2',
        dayOfWeek: 2,
        order: 2,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '💪', type: 'strength', name: '上肢力量充电', duration: 45,
            tags: ['力量', '上肢'],
            summary: '俯卧撑 4×12 · 哑铃推举 4×12 · 平板支撑 3×60s · 卷腹 3×20',
            details: { description: '俯卧撑、哑铃训练与核心稳定性练习。每一点力量的增长，都是你努力最好的见证。', duration: 45, bodyPart: '胸', exercises: [
              { name: '俯卧撑', sets: 4, reps: 12 }, { name: '哑铃推举', sets: 4, reps: 12 },
              { name: '平板支撑', sets: 3, reps: 60 }, { name: '卷腹', sets: 3, reps: 20 }
            ]}
          }
        ]
      },
      // 周三：休息自由活动
      {
        id: 'dp_' + baseId + '_3',
        dayOfWeek: 3,
        order: 3,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '😴', type: 'rest', name: '休息自由活动', duration: 0,
            tags: ['休息', '恢复'],
            summary: '充分休息，让身体恢复',
            details: { description: '肌肉在休息时才会生长，好好放松是为了明天更强！今天可以散散步、做些轻度活动。', duration: 0, restType: 'full' }
          }
        ]
      },
      // 周四：间歇跑挑战
      {
        id: 'dp_' + baseId + '_4',
        dayOfWeek: 4,
        order: 4,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '🏃', type: 'run', name: '间歇跑挑战', duration: 40,
            tags: ['有氧', '高强度'],
            summary: '5km · 6组间歇 · 配速 5:30/km',
            details: { description: '通过间歇训练提升心肺能力。短距离冲刺与恢复交替，燃烧更多卡路里，效率翻倍！', duration: 40, runParams: { subType: 'interval', distance: 5, sets: 6, pace: '5:30/km' } }
          }
        ]
      },
      // 周五：腿部力量 + 有氧燃脂
      {
        id: 'dp_' + baseId + '_5',
        dayOfWeek: 5,
        order: 5,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '💪', type: 'strength', name: '腿部力量训练', duration: 35,
            tags: ['力量', '下肢'],
            summary: '深蹲 4×12 · 弓步蹲 3×12 · 臀桥 3×15 · 小腿提踵 3×20',
            details: { description: '练好腿部力量，跑步更轻松，日常更有活力！', duration: 35, bodyPart: '腿', exercises: [
              { name: '深蹲', sets: 4, reps: 12 }, { name: '弓步蹲', sets: 3, reps: 12 },
              { name: '臀桥', sets: 3, reps: 15 }, { name: '小腿提踵', sets: 3, reps: 20 }
            ]}
          },
          {
            icon: '🚶', type: 'run', name: '有氧燃脂走', duration: 20,
            tags: ['有氧', '燃脂'],
            summary: '快走 2km · 保持心率 130',
            details: { description: '力量训练后加一组快走，提升燃脂效率', duration: 20, runParams: { subType: 'easy', distance: 2 } }
          }
        ]
      },
      // 周六：长距离慢跑 + 拉伸
      {
        id: 'dp_' + baseId + '_6',
        dayOfWeek: 6,
        order: 6,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '🏃', type: 'run', name: '周末长距离慢跑', duration: 60,
            tags: ['有氧', '耐力'],
            summary: '10km · 配速 6:30/km',
            details: { description: '周末来一场长距离慢跑，建立有氧耐力基础。不用追求速度，享受跑步的过程就好。', duration: 60, runParams: { subType: 'long', distance: 10 } }
          },
          {
            icon: '🧘', type: 'yoga', name: '跑后拉伸放松', duration: 15,
            tags: ['拉伸', '恢复'],
            summary: '全身拉伸',
            details: { description: '长跑后的拉伸非常重要', duration: 15, yogaStyle: 'hatha' }
          }
        ]
      },
      // 周日：瑜伽拉伸放松
      {
        id: 'dp_' + baseId + '_7',
        dayOfWeek: 7,
        order: 7,
        isCompleted: false,
        personalNote: '',
        activities: [
          {
            icon: '🧘', type: 'yoga', name: '瑜伽拉伸放松', duration: 30,
            tags: ['拉伸', '放松'],
            summary: '哈他瑜伽 · 全身拉伸',
            details: { description: '全身拉伸放松，缓解一周训练疲劳。柔韧性和力量同样重要，做一个柔软的健身人 🧘', duration: 30, yogaStyle: 'hatha' }
          }
        ]
      }
    ],
    isFromTemplate: false
  }

  saveCurrentPlan(plan)
  // 保存到我的计划列表
  savePlanToMyTrainingPlans(plan, '轻松入门周计划')
  return plan
}

/** 活动类型映射：字符串 → 数字（用于 myTrainingPlans） */
const activityTypeMap = { run: 1, strength: 2, yoga: 3, rest: 6, custom: 5 }

/** 活动图标映射 */
export const activityIconMap = {
  run: '🏃', strength: '💪', yoga: '🧘', rest: '😴', custom: '⚡'
}

/** 活动标签颜色映射 */
export const activityStyleMap = {
  run: { color: '#22c55e', bg: '#ecfdf5', label: '跑步' },
  strength: { color: '#3b82f6', bg: '#eff6ff', label: '力量' },
  yoga: { color: '#a855f7', bg: '#fdf4ff', label: '瑜伽' },
  rest: { color: '#94a3b8', bg: '#f1f5f9', label: '休息' },
  custom: { color: '#f97316', bg: '#fff7ed', label: '自定义' }
}

/**
 * 标准化 dayPlan：兼容旧格式 → 统一转为 activities 数组
 * 旧格式（单 type+details）自动包装为新格式
 */
export function normalizeDayPlan(dayPlan) {
  if (!dayPlan) return null
  // 已有 activities 数组 → 直接使用
  if (dayPlan.activities && dayPlan.activities.length > 0) {
    return { ...dayPlan, activities: dayPlan.activities }
  }
  // 旧格式（单活动）→ 包装为 activities 数组
  if (dayPlan.type) {
    const style = activityStyleMap[dayPlan.type] || activityStyleMap.custom
    return {
      ...dayPlan,
      activities: [{
        icon: activityIconMap[dayPlan.type] || '⚡',
        type: dayPlan.type,
        name: dayPlan.title || style.label,
        duration: dayPlan.details?.duration || 30,
        tags: [style.label],
        summary: buildActivitySummary(dayPlan),
        details: dayPlan.details || {}
      }]
    }
  }
  return dayPlan
}

/** 构建活动摘要文本 */
function buildActivitySummary(dayPlan) {
  const d = dayPlan.details || {}
  if (dayPlan.type === 'run') {
    const rp = d.runParams || {}
    const parts = []
    if (rp.distance) parts.push(`${rp.distance}km`)
    if (rp.pace) parts.push(`配速 ${rp.pace}`)
    if (rp.sets) parts.push(`${rp.sets}组`)
    return parts.join(' · ') || '跑步训练'
  }
  if (dayPlan.type === 'strength') {
    const ex = d.exercises || []
    return ex.slice(0, 2).map(e => `${e.name} ${e.sets||''}×${e.reps||''}`).join(' · ') || (d.bodyPart ? `${d.bodyPart}训练` : '力量训练')
  }
  if (dayPlan.type === 'yoga') {
    return d.yogaStyle ? `${d.yogaStyle}瑜伽` : '瑜伽练习'
  }
  if (dayPlan.type === 'rest') {
    return '自由活动，充分休息'
  }
  return d.description || ''
}

/**
 * 将默认周计划同步保存到 myTrainingPlans 列表
 * 确保用户在「定制」页能看到已有计划
 * 支持多活动格式（同一 day 多个 course）
 */
function savePlanToMyTrainingPlans(weeklyPlan, planName) {
  try {
    const raw = uni.getStorageSync('myTrainingPlans')
    let plans = []
    if (raw) {
      try { plans = JSON.parse(raw) } catch (e) { plans = [] }
    }
    if (plans.some(p => p.id === weeklyPlan.id || p.name === planName)) return

    // 遍历每天，将每个 activity 转为一条 course
    const courses = []
    weeklyPlan.days.forEach(d => {
      const acts = d.activities && d.activities.length > 0 ? d.activities : (d.type ? [{ type: d.type, name: d.title, duration: d.details?.duration, details: d.details }] : [])
      acts.forEach((act, idx) => {
        courses.push({
          week: 1,
          day: d.dayOfWeek,
          name: act.name,
          type: activityTypeMap[act.type] || activityTypeMap.custom || 5,
          duration: act.duration || act.details?.duration || 30,
          description: act.details?.description || '',
          exercises: act.details?.exercises?.map(ex => ({
            name: ex.name, type: activityTypeMap[act.type] || 1,
            sets: ex.sets || 3, reps: ex.reps || 12,
            duration: ex.duration || 0, distance: ex.distance || 0,
            restTime: 60, description: ''
          })) || []
        })
      })
    })

    plans.unshift({
      id: weeklyPlan.id,
      name: planName,
      description: '新手友好的综合训练方案，跑步、力量、瑜伽结合，一周 5 练，轻松开启运动之旅。',
      totalWeeks: 4,
      level: 1,
      levelName: '入门',
      currentWeek: 1,
      completedCourses: 0,
      totalCourses: courses.length * 4,
      courses
    })
    uni.setStorageSync('myTrainingPlans', JSON.stringify(plans))
  } catch (e) {
    console.warn('[默认计划] 保存到 myTrainingPlans 失败:', e)
  }
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
