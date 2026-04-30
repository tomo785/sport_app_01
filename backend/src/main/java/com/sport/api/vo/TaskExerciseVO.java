package com.sport.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务动作VO
 */
@Data
public class TaskExerciseVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 动作ID
     */
    private Long exerciseId;

    /**
     * 动作名称
     */
    private String exerciseName;

    /**
     * 动作类型 1有氧 2力量 3拉伸
     */
    private Integer type;

    /**
     * 动作类型描述
     */
    private String typeDesc;

    /**
     * 计划组数
     */
    private Integer setsPlanned;

    /**
     * 已完成组数
     */
    private Integer setsCompleted;

    /**
     * 计划每组次数
     */
    private Integer repsPlanned;

    /**
     * 实际完成次数
     */
    private Integer repsCompleted;

    /**
     * 使用重量(kg)
     */
    private BigDecimal weightUsed;

    /**
     * 计划时长(秒)
     */
    private Integer durationPlanned;

    /**
     * 实际用时(秒)
     */
    private Integer durationActual;

    /**
     * 休息时长(秒)
     */
    private Integer restTime;

    /**
     * 状态 0未完成 1进行中 2已完成
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 消耗卡路里
     */
    private Integer caloriesBurned;

    /**
     * 平均心率
     */
    private Integer heartRateAvg;

    /**
     * 最大心率
     */
    private Integer heartRateMax;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 示范图片
     */
    private String demoImage;

    /**
     * 目标肌群
     */
    private String targetMuscles;
}
