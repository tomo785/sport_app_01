export const PLAN_STORAGE_KEYS = {
  currentPlan: 'weeklyPlan_current',
  myTrainingPlans: 'myTrainingPlans',
  aiGeneratedPlan: 'ai_generated_plan',
  unifiedDraft: 'unifiedPlan_draft'
}

export const PLAN_TYPE_TO_NUMBER = {
  run: 1,
  strength: 2,
  yoga: 3,
  stretch: 3,
  hiit: 4,
  custom: 5,
  rest: 6
}

export const PLAN_NUMBER_TO_TYPE = {
  1: 'run',
  2: 'strength',
  3: 'yoga',
  4: 'custom',
  5: 'custom',
  6: 'rest'
}

export const PLAN_TYPE_META = {
  run: { label: '有氧', icon: 'run' },
  strength: { label: '力量', icon: 'strength' },
  yoga: { label: '拉伸', icon: 'stretch' },
  custom: { label: '综合', icon: 'plan' },
  rest: { label: '休息', icon: 'rest' }
}

export const LEVEL_NAME_MAP = {
  1: '入门',
  2: '初级',
  3: '中级',
  4: '高级',
  5: '精英'
}

export function createPlanId(prefix = 'up') {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

export function toNumber(value, fallback = 0) {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

export function clampInt(value, min, max, fallback = min) {
  const num = Math.round(toNumber(value, fallback))
  return Math.min(max, Math.max(min, num))
}

export function normalizeActivityType(type) {
  if (typeof type === 'number') return PLAN_NUMBER_TO_TYPE[type] || 'custom'
  if (typeof type === 'string') {
    const key = type.trim()
    if (PLAN_TYPE_TO_NUMBER[key]) return key === 'stretch' ? 'yoga' : key
    if (key.includes('跑') || key.includes('有氧') || key.includes('走') || key.includes('骑') || key.includes('游')) return 'run'
    if (key.includes('力') || key.includes('肌')) return 'strength'
    if (key.includes('拉伸') || key.includes('瑜伽')) return 'yoga'
    if (key.includes('休')) return 'rest'
  }
  return 'custom'
}

export function activityTypeToNumber(type) {
  return PLAN_TYPE_TO_NUMBER[normalizeActivityType(type)] || PLAN_TYPE_TO_NUMBER.custom
}

export function getWeekStartDate(date = new Date()) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  return formatDate(d)
}

export function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function createEmptyUnifiedPlan(input = {}) {
  const totalWeeks = clampInt(input.totalWeeks, 1, 52, 1)
  return {
    id: input.id || createPlanId(),
    name: input.name || '训练计划',
    description: input.description || '',
    totalWeeks,
    level: clampInt(input.level, 1, 5, 1),
    levelName: input.levelName || LEVEL_NAME_MAP[clampInt(input.level, 1, 5, 1)],
    source: input.source || 'custom',
    startDate: input.startDate || getWeekStartDate(),
    createdAt: input.createdAt || new Date().toISOString(),
    weeks: Array.from({ length: totalWeeks }, (_, index) => ({
      week: index + 1,
      days: Array.from({ length: 7 }, (__, dayIndex) => ({
        dayOfWeek: dayIndex + 1,
        sessions: []
      }))
    }))
  }
}

export function getOrCreateUnifiedDay(unifiedPlan, week, dayOfWeek) {
  const weekNo = clampInt(week, 1, Math.max(unifiedPlan.totalWeeks || 1, 1), 1)
  const dayNo = clampInt(dayOfWeek, 1, 7, 1)
  let weekPlan = unifiedPlan.weeks.find(item => item.week === weekNo)
  if (!weekPlan) {
    weekPlan = {
      week: weekNo,
      days: Array.from({ length: 7 }, (_, dayIndex) => ({
        dayOfWeek: dayIndex + 1,
        sessions: []
      }))
    }
    unifiedPlan.weeks.push(weekPlan)
  }
  let dayPlan = weekPlan.days.find(item => item.dayOfWeek === dayNo)
  if (!dayPlan) {
    dayPlan = { dayOfWeek: dayNo, sessions: [] }
    weekPlan.days.push(dayPlan)
  }
  return dayPlan
}

export function normalizeUnifiedPlan(plan) {
  const normalized = createEmptyUnifiedPlan(plan || {})
  const weeks = Array.isArray(plan?.weeks) ? plan.weeks : []
  weeks.forEach(weekPlan => {
    const weekNo = clampInt(weekPlan.week, 1, normalized.totalWeeks, 1)
    const days = Array.isArray(weekPlan.days) ? weekPlan.days : []
    days.forEach(dayPlan => {
      const targetDay = getOrCreateUnifiedDay(normalized, weekNo, dayPlan.dayOfWeek)
      targetDay.sessions = (dayPlan.sessions || []).map(normalizeUnifiedSession)
    })
  })
  normalized.weeks.sort((a, b) => a.week - b.week)
  normalized.weeks.forEach(week => week.days.sort((a, b) => a.dayOfWeek - b.dayOfWeek))
  return normalized
}

export function normalizeUnifiedSession(session = {}) {
  const type = normalizeActivityType(session.type)
  const meta = PLAN_TYPE_META[type] || PLAN_TYPE_META.custom
  return {
    id: session.id || createPlanId('session'),
    name: session.name || meta.label,
    type,
    typeCode: activityTypeToNumber(type),
    icon: session.icon || meta.icon,
    duration: toNumber(session.duration, 0),
    summary: session.summary || '',
    description: session.description || '',
    tags: Array.isArray(session.tags) ? session.tags : [meta.label],
    target: session.target || null,
    details: session.details || {},
    items: Array.isArray(session.items) ? session.items.map(item => normalizeUnifiedItem(item, type)) : []
  }
}

export function normalizeUnifiedItem(item = {}, fallbackType = 'custom') {
  const type = normalizeActivityType(item.type || fallbackType)
  return {
    id: item.id || createPlanId('item'),
    name: item.name || '训练项目',
    type,
    typeCode: activityTypeToNumber(type),
    duration: toNumber(item.duration, 0),
    sets: item.sets ?? null,
    reps: item.reps ?? null,
    restTime: item.restTime ?? null,
    distance: item.distance ?? null,
    description: item.description || '',
    isRepeatGroup: !!item.isRepeatGroup,
    repeatCount: item.repeatCount || 1
  }
}
