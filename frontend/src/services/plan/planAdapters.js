import {
  PLAN_TYPE_META,
  activityTypeToNumber,
  clampInt,
  createEmptyUnifiedPlan,
  createPlanId,
  getOrCreateUnifiedDay,
  normalizeActivityType,
  normalizeUnifiedPlan,
  toNumber
} from './unifiedPlan'

function courseToSession(course = {}) {
  const type = normalizeActivityType(course.type)
  const meta = PLAN_TYPE_META[type] || PLAN_TYPE_META.custom
  const exercises = Array.isArray(course.exercises) ? course.exercises : []
  const targetDistance = exercises.find(item => toNumber(item.distance, 0) > 0)?.distance || course.distance || 0
  const target = targetDistance
    ? { value: +(toNumber(targetDistance, 0) / 1000).toFixed(2), unit: 'km' }
    : null
  return {
    id: course.id || createPlanId('session'),
    name: course.name || meta.label,
    type,
    typeCode: activityTypeToNumber(type),
    icon: course.icon || meta.icon,
    duration: toNumber(course.duration, 0),
    summary: course.summary || buildCourseSummary(course, exercises),
    description: course.description || '',
    tags: course.tags || [meta.label],
    target,
    details: course.details || {},
    items: exercises.map(item => ({
      id: item.id || createPlanId('item'),
      name: item.name || '训练项目',
      type: normalizeActivityType(item.type || type),
      duration: toNumber(item.duration, 0),
      sets: item.sets ?? null,
      reps: item.reps ?? null,
      restTime: item.restTime ?? null,
      distance: item.distance ?? null,
      description: item.description || '',
      isRepeatGroup: !!item.isRepeatGroup,
      repeatCount: item.repeatCount || 1
    }))
  }
}

function activityToSession(activity = {}) {
  const type = normalizeActivityType(activity.type)
  const meta = PLAN_TYPE_META[type] || PLAN_TYPE_META.custom
  const details = activity.details || {}
  const exercises = Array.isArray(details.exercises) ? details.exercises : []
  const target = details.runParams?.distance
    ? { value: toNumber(details.runParams.distance, 0), unit: 'km' }
    : null
  return {
    id: activity.id || createPlanId('session'),
    name: activity.name || activity.title || meta.label,
    type,
    typeCode: activityTypeToNumber(type),
    icon: activity.icon || meta.icon,
    duration: toNumber(activity.duration || details.duration, 0),
    summary: activity.summary || '',
    description: details.description || activity.description || '',
    tags: activity.tags || [meta.label],
    target,
    details,
    items: exercises.map(item => ({
      id: item.id || createPlanId('item'),
      name: item.name || '训练项目',
      type,
      duration: toNumber(item.duration, 0),
      sets: item.sets ?? null,
      reps: item.reps ?? null,
      restTime: item.restTime ?? 60,
      distance: item.distance ?? null,
      description: item.description || ''
    }))
  }
}

function buildCourseSummary(course, exercises) {
  if (course.type === 6 || course.type === 'rest') return '休息恢复'
  const parts = []
  if (course.duration) parts.push(`${course.duration}分钟`)
  const firstDistance = exercises.find(item => toNumber(item.distance, 0) > 0)?.distance
  if (firstDistance) parts.push(`${(toNumber(firstDistance, 0) / 1000).toFixed(1)}km`)
  if (exercises.length) parts.push(`${exercises.length}个项目`)
  return parts.join(' · ')
}

export function aiPlanToUnified(aiPlan = {}) {
  const totalWeeks = clampInt(aiPlan.totalWeeks, 1, 52, 4)
  const unified = createEmptyUnifiedPlan({
    id: aiPlan.id || createPlanId('ai_plan'),
    name: aiPlan.name || 'AI 训练方案',
    description: aiPlan.description || '',
    totalWeeks,
    level: aiPlan.level || 1,
    source: 'ai'
  })
  ;(aiPlan.courses || []).forEach(course => {
    const week = clampInt(course.week, 1, totalWeeks, 1)
    const day = clampInt(course.day, 1, 7, 1)
    const targetDay = getOrCreateUnifiedDay(unified, week, day)
    targetDay.sessions.push(courseToSession(course))
  })
  return normalizeUnifiedPlan(unified)
}

export function weeklyPlanToUnified(weeklyPlan = {}) {
  const unified = createEmptyUnifiedPlan({
    id: weeklyPlan.id || createPlanId('weekly_plan'),
    name: weeklyPlan.name || '本周训练计划',
    description: weeklyPlan.description || '',
    totalWeeks: 1,
    level: weeklyPlan.level || 1,
    source: 'weekly',
    startDate: weeklyPlan.weekStartDate || weeklyPlan.startDate
  })
  ;(weeklyPlan.days || []).forEach(dayPlan => {
    const targetDay = getOrCreateUnifiedDay(unified, 1, dayPlan.dayOfWeek)
    const activities = Array.isArray(dayPlan.activities) && dayPlan.activities.length
      ? dayPlan.activities
      : dayPlan.type
        ? [dayPlan]
        : []
    targetDay.sessions = activities.map(activityToSession)
  })
  return normalizeUnifiedPlan(unified)
}

export function myTrainingPlanToUnified(plan = {}) {
  const totalWeeks = clampInt(plan.totalWeeks, 1, 52, 4)
  const unified = createEmptyUnifiedPlan({
    id: plan.id || createPlanId('training_plan'),
    name: plan.name || '训练计划',
    description: plan.description || '',
    totalWeeks,
    level: plan.level || 1,
    levelName: plan.levelName,
    source: plan.source || 'custom',
    startDate: plan.startDate
  })
  ;(plan.courses || []).forEach(course => {
    const week = clampInt(course.week, 1, totalWeeks, 1)
    const day = clampInt(course.day, 1, 7, 1)
    getOrCreateUnifiedDay(unified, week, day).sessions.push(courseToSession(course))
  })
  return normalizeUnifiedPlan(unified)
}

export function unifiedToWeeklyPlan(unifiedPlan, options = {}) {
  const plan = normalizeUnifiedPlan(unifiedPlan)
  const sourceWeek = clampInt(options.week, 1, plan.totalWeeks, 1)
  const weekPlan = plan.weeks.find(item => item.week === sourceWeek) || plan.weeks[0]
  return {
    id: options.id || `wp_${plan.id}`,
    name: options.name || plan.name || '本周训练计划',
    description: plan.description || '',
    weekStartDate: options.weekStartDate || plan.startDate,
    isFromTemplate: false,
    sourcePlanId: plan.id,
    sourceWeek,
    days: (weekPlan?.days || []).map(day => ({
      id: createPlanId('dp'),
      dayOfWeek: day.dayOfWeek,
      order: day.dayOfWeek,
      isCompleted: false,
      personalNote: '',
      activities: (day.sessions || []).map(session => sessionToActivity(session))
    }))
  }
}

export function unifiedToMyTrainingPlan(unifiedPlan, options = {}) {
  const plan = normalizeUnifiedPlan(unifiedPlan)
  const courses = []
  plan.weeks.forEach(week => {
    week.days.forEach(day => {
      ;(day.sessions || []).forEach(session => {
        courses.push(sessionToCourse(session, week.week, day.dayOfWeek, plan.level))
      })
    })
  })
  return {
    id: options.id || plan.id || createPlanId('training_plan'),
    name: options.name || plan.name || '训练计划',
    description: plan.description || '',
    totalWeeks: plan.totalWeeks || 1,
    level: plan.level || 1,
    levelName: plan.levelName,
    currentWeek: options.currentWeek || 1,
    completedCourses: options.completedCourses || 0,
    totalCourses: courses.length,
    startDate: options.startDate || plan.startDate,
    source: plan.source,
    courses
  }
}

function sessionToActivity(session = {}) {
  const type = normalizeActivityType(session.type)
  const meta = PLAN_TYPE_META[type] || PLAN_TYPE_META.custom
  const details = {
    ...(session.details || {}),
    description: session.description || session.details?.description || '',
    duration: toNumber(session.duration, 0)
  }
  if (type === 'run' && session.target?.unit === 'km') {
    details.runParams = {
      ...(details.runParams || {}),
      distance: session.target.value
    }
  }
  if (session.items?.length) {
    details.exercises = session.items.map(item => ({
      name: item.name,
      sets: item.sets,
      reps: item.reps,
      duration: item.duration,
      distance: item.distance,
      restTime: item.restTime,
      description: item.description
    }))
  }
  return {
    icon: session.icon || meta.icon,
    type,
    name: session.name || meta.label,
    duration: toNumber(session.duration, 0),
    tags: session.tags || [meta.label],
    summary: session.summary || buildCourseSummary(session, session.items || []),
    details
  }
}

function sessionToCourse(session = {}, week, day, level) {
  const typeCode = activityTypeToNumber(session.type)
  return {
    week,
    day,
    name: session.name,
    type: typeCode,
    level,
    duration: toNumber(session.duration, 0),
    description: session.description || '',
    warmUpDuration: session.warmUpDuration || 0,
    coolDownDuration: session.coolDownDuration || 0,
    exercises: (session.items || []).map(item => ({
      name: item.name,
      type: activityTypeToNumber(item.type || session.type),
      duration: toNumber(item.duration, 0),
      sets: item.sets,
      reps: item.reps,
      restTime: item.restTime,
      distance: item.distance,
      description: item.description || '',
      isRepeatGroup: !!item.isRepeatGroup,
      repeatCount: item.repeatCount || 1
    }))
  }
}
