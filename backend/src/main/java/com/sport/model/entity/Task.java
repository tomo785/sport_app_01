package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务卡片实体
 */
@Data
@TableName("t_task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String icon;

    private Integer type;

    private String condition;

    private Integer targetValue;

    private Integer rewardType;

    private Integer rewardValue;

    private Long rewardResourceId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer sortOrder;

    private Integer status;

    private Boolean isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
