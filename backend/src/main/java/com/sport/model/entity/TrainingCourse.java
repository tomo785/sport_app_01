package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 训练课程实体
 */
@Data
@TableName("t_training_course")
public class TrainingCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private String name;

    private String description;

    private Integer week;

    private Integer day;

    private Integer type;

    private Integer level;

    private Integer duration;

    private Integer warmUpDuration;

    private Integer coolDownDuration;

    private String videoUrl;

    private String audioUrl;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
