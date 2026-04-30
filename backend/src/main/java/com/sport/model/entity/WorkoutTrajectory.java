package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运动轨迹点实体
 */
@Data
@TableName("t_workout_trajectory")
public class WorkoutTrajectory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long userId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer altitude;

    private BigDecimal speed;

    private Long timestamp;

    private Integer sequence;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
