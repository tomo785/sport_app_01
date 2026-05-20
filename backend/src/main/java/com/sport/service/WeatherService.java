package com.sport.service;

import com.sport.api.vo.WeatherNowVO;

import java.math.BigDecimal;

/**
 * 天气服务接口 — 代理和风天气 API
 */
public interface WeatherService {

    /**
     * 获取实时天气 + AQI
     *
     * @param lat 纬度（GCJ02）
     * @param lng 经度（GCJ02）
     * @return 实时天气数据
     */
    WeatherNowVO getNowWeather(BigDecimal lat, BigDecimal lng);
}
