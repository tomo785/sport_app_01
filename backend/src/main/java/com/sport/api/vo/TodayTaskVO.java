package com.sport.api.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 今日任务VO
 */
@Data
public class TodayTaskVO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务日期
     */
    private LocalDate taskDate;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 副标题/描述
     */
    private String subtitle;

    /**
     * 任务类型 1系统计划 2自定义 3AI推荐
     */
    private Integer taskType;

    /**
     * 任务类型描述
     */
    private String taskTypeDesc;

    /**
     * 总动作数
     */
    private Integer totalExercises;

    /**
     * 已完成动作数
     */
    private Integer completedExercises;

    /**
     * 预计总时长(分钟)
     */
    private Integer totalDuration;

    /**
     * 已完成时长(分钟)
     */
    private Integer completedDuration;

    /**
     * 总组数
     */
    private Integer totalSets;

    /**
     * 已完成组数
     */
    private Integer completedSets;

    /**
     * 默认休息时间(秒)
     */
    private Integer restTime;

    /**
     * 状态 0待开始 1进行中 2已完成 3已跳过
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 完成进度 0-100
     */
    private Integer progress;

    /**
     * 已消耗卡路里
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
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 剩余时长(分钟)
     */
    private Integer remainingTime;

    /**
     * 剩余任务数
     */
    private Integer remainingTasks;

    /**
     * 动作列表
     */
    private List<TaskExerciseVO> exercises;
}
