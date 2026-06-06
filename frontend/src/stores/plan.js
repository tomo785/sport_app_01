import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getCurrentPlan,
  saveCurrentPlan,
  getPlanTemplates,
  savePlanTemplate,
  deletePlanTemplate,
  getCommunityTemplates,
  saveCommunityTemplate,
  getCardOrder,
  saveCardOrder,
  normalizeDayPlan
} from '../api/plan'
export const usePlanStore = defineStore('plan', () => {
  // ==================== State ====================
  const currentPlan = ref(null)
  const templates = ref([])
  const communityTemplates = ref([])
  const selectedDate = ref('')
  const isLoading = ref(false)
  // ==================== Getters ====================
  const hasPlan = computed(() => {
    return currentPlan.value && currentPlan.value.days && currentPlan.value.days.length > 0
  })
  const getDayPlan = computed(() => {
    return (dateStr) => {
      if (!currentPlan.value || !currentPlan.value.days) return null
      const dayOfWeek = getDayOfWeek(dateStr)
      const day = currentPlan.value.days.find(d => d.dayOfWeek === dayOfWeek)
      return day ? normalizeDayPlan(day) : null
    }
  })
  const weekStartDate = computed(() => {
    return currentPlan.value?.weekStartDate || getWeekStart(new Date())
  })
  // ==================== Actions ====================
  function initPlan(weekStart) {
    const saved = getCurrentPlan()
    if (saved && saved.weekStartDate === weekStart) {
      currentPlan.value = saved
    } else {
      currentPlan.value = {
        id: 'wp_' + Date.now(),
        name: '本周训练计划',
        weekStartDate: weekStart,
        days: [],
        isFromTemplate: false
      }
    }
    templates.value = getPlanTemplates()
    communityTemplates.value = getCommunityTemplates()
  }
  function updateDayPlan(dayOfWeek, planData) {
    if (!currentPlan.value) return
    const days = currentPlan.value.days || []
    const index = days.findIndex(d => d.dayOfWeek === dayOfWeek)
    // 支持多活动格式：如果传入的是 activities 数组或包含 type 的旧格式
    const newDay = {
      dayOfWeek,
      order: dayOfWeek,
      isCompleted: false,
      personalNote: '',
      ...planData
    }
    // 确保 activities 数组存在
    if (!newDay.activities && newDay.type) {
      // 旧格式单活动 → 包装为 activities 数组
      const style = { run: 'run', strength: 'strength', yoga: 'stretch', rest: 'rest', custom: 'hiit' }
      newDay.activities = [{
        icon: style[newDay.type] || 'hiit',
        type: newDay.type,
        name: newDay.title || '训练',
        duration: newDay.details?.duration || 30,
        tags: [newDay.type],
        summary: '',
        details: newDay.details || {}
      }]
    }
    if (!newDay.activities) newDay.activities = []
    if (index > -1) {
      days[index] = { ...days[index], ...newDay }
    } else {
      days.push(newDay)
    }
    currentPlan.value.days = days
    saveCurrentPlan(currentPlan.value)
  }
  function removeDayPlan(dayOfWeek) {
    if (!currentPlan.value) return
    currentPlan.value.days = currentPlan.value.days.filter(d => d.dayOfWeek !== dayOfWeek)
    saveCurrentPlan(currentPlan.value)
  }
  function reorderDays(dayOfWeek, newOrder) {
    if (!currentPlan.value) return
    const day = currentPlan.value.days.find(d => d.dayOfWeek === dayOfWeek)
    if (day) {
      day.order = newOrder
      saveCurrentPlan(currentPlan.value)
    }
  }
  function toggleComplete(dayOfWeek) {
    if (!currentPlan.value) return
    const day = currentPlan.value.days.find(d => d.dayOfWeek === dayOfWeek)
    if (day) {
      day.isCompleted = !day.isCompleted
      saveCurrentPlan(currentPlan.value)
    }
  }
  function updatePersonalNote(dayOfWeek, note) {
    if (!currentPlan.value) return
    const day = currentPlan.value.days.find(d => d.dayOfWeek === dayOfWeek)
    if (day) {
      day.personalNote = note
      saveCurrentPlan(currentPlan.value)
    }
  }
  function clearWeek() {
    if (!currentPlan.value) return
    currentPlan.value.days = []
    currentPlan.value.isFromTemplate = false
    currentPlan.value.templateId = undefined
    saveCurrentPlan(currentPlan.value)
  }
  function copyDayTo(sourceDayOfWeek, targetDays) {
    if (!currentPlan.value) return
    const sourceDay = currentPlan.value.days.find(d => d.dayOfWeek === sourceDayOfWeek)
    if (!sourceDay) return
    targetDays.forEach(targetDayOfWeek => {
      const index = currentPlan.value.days.findIndex(d => d.dayOfWeek === targetDayOfWeek)
      const copiedDay = { ...sourceDay, dayOfWeek: targetDayOfWeek, isCompleted: false, personalNote: '' }
      if (index > -1) {
        currentPlan.value.days[index] = copiedDay
      } else {
        currentPlan.value.days.push(copiedDay)
      }
    })
    saveCurrentPlan(currentPlan.value)
  }
  function saveAsTemplate(name) {
    if (!currentPlan.value) return
    const template = {
      id: 't_' + Date.now(),
      name,
      weeklyStructure: [...currentPlan.value.days],
      createdAt: new Date().toISOString()
    }
    templates.value.push(template)
    savePlanTemplate(template)
  }
  function deleteTemplate(id) {
    templates.value = templates.value.filter(t => t.id !== id)
    deletePlanTemplate(id)
  }
  function useTemplate(template) {
    if (!currentPlan.value) return
    currentPlan.value.days = template.weeklyStructure.map((day, index) => ({
      ...day,
      isCompleted: false,
      personalNote: '',
      order: index + 1
    }))
    currentPlan.value.isFromTemplate = true
    currentPlan.value.templateId = template.id
    saveCurrentPlan(currentPlan.value)
  }
  function saveToCommunity(data) {
    const template = {
      name: data.name,
      authorName: '我',
      tags: data.tags,
      targetAudience: data.targetAudience,
      description: data.description,
      weeklyStructure: currentPlan.value?.days || []
    }
    communityTemplates.value.push(template)
    saveCommunityTemplate(template)
  }
  function loadCardOrder(date) {
    return getCardOrder(date)
  }
  function saveDayCardOrder(date, order) {
    saveCardOrder(date, order)
  }
  return {
    currentPlan,
    templates,
    communityTemplates,
    selectedDate,
    isLoading,
    hasPlan,
    getDayPlan,
    weekStartDate,
    initPlan,
    updateDayPlan,
    removeDayPlan,
    reorderDays,
    toggleComplete,
    updatePersonalNote,
    clearWeek,
    copyDayTo,
    saveAsTemplate,
    deleteTemplate,
    useTemplate,
    saveToCommunity,
    loadCardOrder,
    saveDayCardOrder
  }
})
// ==================== 工具函数 ====================
function getDayOfWeek(dateStr) {
  const date = new Date(dateStr)
  let day = date.getDay()
  return day === 0 ? 7 : day
}
function getWeekStart(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  const monday = new Date(d.setDate(diff))
  return formatDate(monday)
}
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}