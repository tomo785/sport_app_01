package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运动记录实体
 */
@Data
@TableName("t_workout_record")
public class WorkoutRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration;

    private Integer distance;

    private Integer calories;

    private BigDecimal avgSpeed;

    private BigDecimal maxSpeed;

    private Integer minAltitude;

    private Integer maxAltitude;

    private Integer totalAscent;

    private Integer totalDescent;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
