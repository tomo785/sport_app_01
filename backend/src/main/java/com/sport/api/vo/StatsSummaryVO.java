package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计汇总VO
 */
@Data
public class StatsSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总运动次数
     */
    private Integer totalWorkouts;

    /**
     * 总距离(米)
     */
    private Integer totalDistance;

    /**
     * 总时长(秒)
     */
    private Integer totalDuration;

    /**
     * 总卡路里(千卡)
     */
    private Integer totalCalories;

    /**
     * 连续运动天数
     */
    private Integer streakDays;

    /**
     * 本周运动次数
     */
    private Integer weeklyWorkouts;

    /**
     * 本月运动次数
     */
    private Integer monthlyWorkouts;

    // ========== 格式化字段 ==========

    /**
     * 格式化总距离
     */
    private String totalDistanceStr;

    /**
     * 格式化总时长
     */
    private String totalDurationStr;

    /**
     * 格式化总卡路里
     */
    private String totalCaloriesStr;
}
