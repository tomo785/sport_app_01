package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 训练记录实体
 */
@Data
@TableName("t_training_record")
public class TrainingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long coursePlanId;

    private Long courseId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration;

    private Integer calories;

    private BigDecimal avgHeartRate;

    private Integer maxHeartRate;

    private BigDecimal distance;

    private Integer avgSpeed;

    private Boolean isCompleted;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
