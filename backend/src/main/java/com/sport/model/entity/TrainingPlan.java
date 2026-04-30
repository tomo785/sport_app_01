package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 训练计划实体
 */
@Data
@TableName("t_training_plan")
public class TrainingPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer level;

    private Integer totalWeeks;

    private Integer workoutsPerWeek;

    private Integer totalDuration;

    private Long targetDistance;

    private Integer targetCalories;

    private String coachName;

    private Boolean isOfficial;

    private Integer viewCount;

    private Integer enrollCount;

    private Float rating;

    private Integer ratingCount;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
