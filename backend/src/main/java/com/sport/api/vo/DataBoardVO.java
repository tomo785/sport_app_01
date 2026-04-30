package com.sport.api.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 数据看板VO
 */
@Data
@Builder
public class DataBoardVO {

    /**
     * 实时心率
     */
    private Integer heartRate;

    /**
     * 今日消耗卡路里
     */
    private Integer caloriesBurned;

    /**
     * 训练完成率(%)
     */
    private Integer completionRate;

    /**
     * 目标完成度(%)
     */
    private Integer goalProgress;

    /**
     * 今日目标卡路里
     */
    private Integer targetCalories;

    /**
     * 今日目标时长(分钟)
     */
    private Integer targetDuration;
}
