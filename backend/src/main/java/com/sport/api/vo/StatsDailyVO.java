package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 每日统计VO
 */
@Data
public class StatsDailyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

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
     * 总步数
     */
    private Integer steps;

    /**
     * 运动次数
     */
    private Integer recordCount;

    /**
     * 目标完成进度(%)
     */
    private Integer goalProgress;

    // ========== 格式化字段 ==========

    /**
     * 格式化距离 (如: 5.2km)
     */
    private String distanceStr;

    /**
     * 格式化时长 (如: 1小时30分)
     */
    private String durationStr;

    /**
     * 格式化卡路里 (如: 520千卡)
     */
    private String caloriesStr;
}
