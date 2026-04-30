package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 大转盘运动决定记录实体
 */
@Data
@TableName("t_lucky_wheel_record")
public class LuckyWheelRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wheelId;

    private Long prizeId;

    private Integer workoutType;

    private Integer workoutLevel;

    private Integer duration;

    private LocalDate decisionDate;

    private LocalDateTime spinTime;

    private Boolean isCompleted;

    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
