package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 大转盘运动决定实体
 */
@Data
@TableName("t_lucky_wheel_prize")
public class LuckyWheelPrize implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long wheelId;

    private String name;

    private String icon;

    private Integer type;

    private Integer level;

    private Integer minDuration;

    private Integer maxDuration;

    private String description;

    private Integer probability;

    private Integer position;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
