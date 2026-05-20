package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日统计实体
 */
@Data
@TableName("t_user_daily_stats")
public class UserDailyStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate statDate;

    private Integer totalDistance;

    private Integer totalDuration;

    private Integer totalCalories;

    private Integer totalSteps;

    private Integer recordCount;

    private Long goalId;

    private Integer goalProgress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
