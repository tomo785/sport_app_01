package com.sport.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户资料更新DTO
 */
@Data
@Schema(description = "用户资料更新DTO")
public class UserUpdateDTO {

    @Schema(description = "昵称", example = "运动达人")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Min(value = 0, message = "性别格式错误")
    @Max(value = 2, message = "性别格式错误")
    @Schema(description = "性别 0未知 1男 2女", example = "1")
    private Integer gender;

    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄不能超过150")
    @Schema(description = "年龄", example = "25")
    private Integer age;

    @Min(value = 1, message = "身高必须大于0")
    @Max(value = 300, message = "身高不能超过300")
    @Schema(description = "身高(厘米)", example = "175")
    private Integer height;

    @Min(value = 1, message = "体重必须大于0")
    @Schema(description = "体重(公斤)", example = "70.5")
    private BigDecimal weight;

    @Min(value = 1, message = "腰围必须大于0")
    @Schema(description = "腰围(厘米)", example = "82.5")
    private BigDecimal waistCircumference;

    @Min(value = 40, message = "收缩压不能低于40")
    @Max(value = 260, message = "收缩压不能超过260")
    @Schema(description = "收缩压(mmHg)", example = "118")
    private Integer systolicPressure;

    @Min(value = 30, message = "舒张压不能低于30")
    @Max(value = 180, message = "舒张压不能超过180")
    @Schema(description = "舒张压(mmHg)", example = "76")
    private Integer diastolicPressure;

    @Min(value = 30, message = "静息心率不能低于30")
    @Max(value = 220, message = "静息心率不能超过220")
    @Schema(description = "静息心率(次/分)", example = "62")
    private Integer restingHeartRate;

    @Min(value = 40, message = "运动平均心率不能低于40")
    @Max(value = 240, message = "运动平均心率不能超过240")
    @Schema(description = "运动平均心率(次/分)", example = "138")
    private Integer avgExerciseHeartRate;

    @Min(value = 40, message = "运动最高心率不能低于40")
    @Max(value = 240, message = "运动最高心率不能超过240")
    @Schema(description = "运动最高心率(次/分)", example = "168")
    private Integer maxHeartRate;
}
