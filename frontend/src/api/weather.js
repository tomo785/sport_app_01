import { get } from '@/utils/request'

/**
 * 天气相关 API — 通过后端代理和风天气
 */

/** 根据经纬度获取实时天气（含 AQI） */
export function getNowWeather(lat, lng) {
  return get('/weather/now', { lat, lng })
}
