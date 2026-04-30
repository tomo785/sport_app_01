package com.sport.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建目标DTO
 */
@Data
public class GoalCreateDTO {

    /**
     * 目标类型 1每日距离 2每周次数
     */
    @NotNull(message = "目标类型不能为空")
    private Integer type;

    /**
     * 目标值(米/次数)
     */
    @NotNull(message = "目标值不能为空")
    @Min(value = 1, message = "目标值必须大于0")
    private Integer targetValue;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期 NULL表示长期
     */
    private LocalDate endDate;
}
