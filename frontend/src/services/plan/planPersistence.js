import {
  aiPlanToUnified,
  myTrainingPlanToUnified,
  unifiedToMyTrainingPlan,
  unifiedToWeeklyPlan,
  weeklyPlanToUnified
} from './planAdapters'
import { PLAN_STORAGE_KEYS, createPlanId, normalizeUnifiedPlan } from './unifiedPlan'

function readJsonStorage(key, fallback) {
  const raw = uni.getStorageSync(key)
  if (!raw) return fallback
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch (e) {
    return fallback
  }
}

function writeJsonStorage(key, value) {
  uni.setStorageSync(key, JSON.stringify(value))
}

export function saveUnifiedPlanToWeekly(unifiedPlan, options = {}) {
  const normalized = normalizeUnifiedPlan(unifiedPlan)
  const weeklyPlan = unifiedToWeeklyPlan(normalized, {
    week: options.week || 1,
    id: options.id || `wp_${createPlanId('unified')}`,
    weekStartDate: options.weekStartDate || normalized.startDate
  })
  writeJsonStorage(PLAN_STORAGE_KEYS.currentPlan, weeklyPlan)
  return weeklyPlan
}

export function saveUnifiedPlanToMyTrainingPlans(unifiedPlan, options = {}) {
  const normalized = normalizeUnifiedPlan(unifiedPlan)
  const plan = unifiedToMyTrainingPlan(normalized, {
    id: options.id || normalized.id || createPlanId('training_plan'),
    startDate: options.startDate || normalized.startDate
  })
  const plans = readJsonStorage(PLAN_STORAGE_KEYS.myTrainingPlans, [])
  const idx = plans.findIndex(item => String(item.id) === String(plan.id))
  if (idx > -1) {
    plans[idx] = { ...plans[idx], ...plan }
  } else {
    plans.unshift(plan)
  }
  writeJsonStorage(PLAN_STORAGE_KEYS.myTrainingPlans, plans)
  return plan
}

export function saveAiPlanToWeekly(aiPlan, options = {}) {
  return saveUnifiedPlanToWeekly(aiPlanToUnified(aiPlan), options)
}

export function saveAiPlanToMyTrainingPlans(aiPlan, options = {}) {
  return saveUnifiedPlanToMyTrainingPlans(aiPlanToUnified(aiPlan), options)
}

export function getCurrentWeeklyUnifiedPlan() {
  const weeklyPlan = readJsonStorage(PLAN_STORAGE_KEYS.currentPlan, null)
  return weeklyPlan ? weeklyPlanToUnified(weeklyPlan) : null
}

export function getMyUnifiedTrainingPlans() {
  const plans = readJsonStorage(PLAN_STORAGE_KEYS.myTrainingPlans, [])
  return plans.map(plan => myTrainingPlanToUnified(plan))
}

export function cacheAiPlanDraft(aiPlan) {
  const unified = aiPlan?.weeks ? normalizeUnifiedPlan(aiPlan) : aiPlanToUnified(aiPlan)
  writeJsonStorage(PLAN_STORAGE_KEYS.unifiedDraft, unified)
  uni.setStorageSync(PLAN_STORAGE_KEYS.aiGeneratedPlan, aiPlan)
  return unified
}
