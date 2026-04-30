package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户任务动作记录实体
 */
@Data
@TableName("t_user_task_exercise")
public class UserTaskExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 动作ID
     */
    private Long exerciseId;

    /**
     * 动作名称（快照）
     */
    private String exerciseName;

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
     * 优先级 high-高 medium-中 low-低
     */
    private String priority;

    /**
     * 任务流程类型 warmup-热身 strength-力量 cardio-有氧 core-核心 stretch-拉伸 rest-休息
     */
    private String taskFlowType;

    /**
     * 图标
     */
    private String icon;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
