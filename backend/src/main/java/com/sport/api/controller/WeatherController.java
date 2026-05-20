package com.sport.api.controller;

import com.sport.api.vo.WeatherNowVO;
import com.sport.common.util.Result;
import com.sport.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 天气控制器 — 代理天气 API 返回实时数据
 */
@Slf4j
@Tag(name = "天气", description = "实时天气与空气质量查询")
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "获取实时天气（含AQI）")
    @GetMapping("/now")
    public Result<WeatherNowVO> getNowWeather(
            @Parameter(description = "纬度", required = true) @RequestParam BigDecimal lat,
            @Parameter(description = "经度", required = true) @RequestParam BigDecimal lng) {
        log.info(">>> 收到天气请求: lat={}, lng={}", lat, lng);
        WeatherNowVO vo = weatherService.getNowWeather(lat, lng);
        log.info("<<< 天气响应: temp={}, city={}, aqi={}, sport={}",
                vo.getTemp(), vo.getCity(), vo.getAqi(), vo.getSportCategory());
        return Result.success(vo);
    }
}
