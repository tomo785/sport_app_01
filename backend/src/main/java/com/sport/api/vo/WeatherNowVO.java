package com.sport.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 实时天气 VO
 */
@Data
@Schema(description = "实时天气")
public class WeatherNowVO {

    @Schema(description = "温度")
    private String temp;

    @Schema(description = "体感温度")
    private String feelsLike;

    @Schema(description = "天气描述")
    private String text;

    @Schema(description = "图标码")
    private String icon;

    @Schema(description = "风向")
    private String windDir;

    @Schema(description = "城市名")
    private String city;

    @Schema(description = "AQI数值")
    private String aqi;

    @Schema(description = "空气质量等级")
    private String aqiLevel;

    @Schema(description = "AQI 颜色 hex")
    private String aqiColor;

    @Schema(description = "运动指数等级")
    private Integer sportLevel;

    @Schema(description = "运动指数类别")
    private String sportCategory;

    @Schema(description = "运动指数建议")
    private String sportText;
}
