package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运动大转盘活动实体
 */
@Data
@TableName("t_lucky_wheel")
public class LuckyWheel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer dailySpinLimit;

    private Integer totalSpins;

    private Integer usedSpins;

    private String tips;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
