package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户训练计划关联实体
 */
@Data
@TableName("t_user_training_plan")
public class UserTrainingPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long planId;

    private Integer status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer currentWeek;

    private Integer currentDay;

    private Integer completedCourses;

    private Integer totalCourses;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
