/**
 * 应用常量定义
 */

// 运动类型
export const WORKOUT_TYPES = {
  RUNNING: 1,
  WALKING: 2,
  CYCLING: 3
}

// 运动类型名称
export const WORKOUT_TYPE_NAMES = {
  [WORKOUT_TYPES.RUNNING]: '跑步',
  [WORKOUT_TYPES.WALKING]: '健走',
  [WORKOUT_TYPES.CYCLING]: '骑行'
}

// 运动类型颜色
export const WORKOUT_TYPE_COLORS = {
  [WORKOUT_TYPES.RUNNING]: '#FF5722',
  [WORKOUT_TYPES.WALKING]: '#4CAF50',
  [WORKOUT_TYPES.CYCLING]: '#2196F3'
}

// 目标类型
export const GOAL_TYPES = {
  DAILY_DISTANCE: 1,
  WEEKLY_COUNT: 2
}

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  SETTINGS: 'settings'
}

// 分页默认参数
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  MAX_SIZE: 100
}

// 时间相关
export const TIME = {
  SECOND: 1000,
  MINUTE: 60 * 1000,
  HOUR: 60 * 60 * 1000,
  DAY: 24 * 60 * 60 * 1000,
  WEEK: 7 * 24 * 60 * 60 * 1000
}

// 正则表达式
export const REGEX = {
  PHONE: /^1[3-9]\d{9}$/,
  CODE: /^\d{6}$/,
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
}
