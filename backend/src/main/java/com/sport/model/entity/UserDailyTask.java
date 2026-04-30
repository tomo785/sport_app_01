package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日任务实体（任务卡片核心）
 */
@Data
@TableName("t_user_daily_task")
public class UserDailyTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务日期
     */
    private LocalDate taskDate;

    /**
     * 关联训练计划ID
     */
    private Long planId;

    /**
     * 关联课程ID
     */
    private Long courseId;

    /**
     * 任务类型 1系统计划 2自定义 3AI推荐
     */
    private Integer taskType;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 副标题/描述
     */
    private String subtitle;

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
