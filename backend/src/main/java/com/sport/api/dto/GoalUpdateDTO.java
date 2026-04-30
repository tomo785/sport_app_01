package com.sport.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新目标DTO
 */
@Data
public class GoalUpdateDTO {

    /**
     * 目标类型 1每日距离 2每周次数
     */
    private Integer type;

    /**
     * 目标值(米/次数)
     */
    @Min(value = 1, message = "目标值必须大于0")
    private Integer targetValue;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期 NULL表示长期
     */
    private LocalDate endDate;

    /**
     * 状态 0已取消 1进行中 2已完成
     */
    private Integer status;
}
