package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户每月统计实体
 */
@Data
@TableName("t_user_monthly_stats")
public class UserMonthlyStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer statYear;

    private Integer statMonth;

    private Integer totalDistance;

    private Integer totalDuration;

    private Integer totalCalories;

    private Integer recordCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
