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
}
