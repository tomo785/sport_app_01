import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getWorkoutList, getWorkoutDetail, getWorkoutTrajectory } from '../api/workout'

export const useWorkoutStore = defineStore('workout', () => {
  // ===================== State =====================
  const records = ref([])
  const currentDetail = ref(null)
  const currentTrajectory = ref([])
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0,
    pages: 0
  })
  const loading = ref(false)
  const finished = ref(false)
  const error = ref(null)
  const filters = ref({
    type: null,
    startDate: '',
    endDate: ''
  })

  // ===================== Getters =====================
  const hasRecords = computed(() => records.value.length > 0)
  const totalCount = computed(() => pagination.value.total)
  const getRecordById = computed(() => (id) => {
    return records.value.find(r => r.id === id) || null
  })

  // ===================== Actions =====================
  /**
   * 拉取运动记录列表
   * @param {boolean} reset 是否重置列表
   */
  const fetchRecords = async (reset = false) => {
    if (loading.value) return
    if (reset) {
      pagination.value.page = 1
      records.value = []
      finished.value = false
    }
    if (finished.value && !reset) return

    loading.value = true
    error.value = null

    try {
      const params = {
        page: pagination.value.page,
        size: pagination.value.size,
        ...filters.value
      }
      // 过滤空值
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key]
        }
      })

      const res = await getWorkoutList(params)
      if (res.code === 200 && res.data) {
        const list = res.data.list || []
        records.value = reset ? list : [...records.value, ...list]
        pagination.value.total = res.data.total || 0
        pagination.value.pages = res.data.pages || 0

        if (
          pagination.value.page >= pagination.value.pages ||
          list.length < pagination.value.size
        ) {
          finished.value = true
        } else {
          pagination.value.page++
        }
      } else {
        error.value = res.message || '加载失败'
      }
    } catch (err) {
      error.value = err.message || '网络错误'
      console.error('加载运动记录失败:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 拉取单条运动详情
   * @param {number} id 记录ID
   */
  const fetchDetail = async (id) => {
    loading.value = true
    error.value = null
    currentDetail.value = null
    currentTrajectory.value = []

    try {
      const [detailRes, trajRes] = await Promise.all([
        getWorkoutDetail(id),
        getWorkoutTrajectory(id).catch(() => ({ code: 200, data: [] }))
      ])

      if (detailRes.code === 200) {
        currentDetail.value = detailRes.data || null
      } else {
        error.value = detailRes.message || '获取详情失败'
      }

      if (trajRes.code === 200 && trajRes.data) {
        currentTrajectory.value = Array.isArray(trajRes.data) ? trajRes.data : []
      }
    } catch (err) {
      error.value = err.message || '网络错误'
      console.error('获取运动详情失败:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置筛选条件并重新加载
   * @param {Object} newFilters 新筛选条件
   */
  const setFilters = (newFilters) => {
    filters.value = { ...filters.value, ...newFilters }
    fetchRecords(true)
  }

  /**
   * 清空当前详情
   */
  const clearCurrent = () => {
    currentDetail.value = null
    currentTrajectory.value = []
    error.value = null
  }

  return {
    // State
    records,
    currentDetail,
    currentTrajectory,
    pagination,
    loading,
    finished,
    error,
    filters,
    // Getters
    hasRecords,
    totalCount,
    getRecordById,
    // Actions
    fetchRecords,
    fetchDetail,
    setFilters,
    clearCurrent
  }
})
