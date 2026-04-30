package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 训练动作实体
 */
@Data
@TableName("t_training_exercise")
public class TrainingExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 动作名称
     */
    private String name;

    /**
     * 动作描述
     */
    private String description;

    /**
     * 动作类型 1有氧 2力量 3拉伸
     */
    private Integer type;

    /**
     * 动作时长(秒)，用于计时动作
     */
    private Integer duration;

    /**
     * 组数
     */
    private Integer sets;

    /**
     * 每组次数
     */
    private Integer reps;

    /**
     * 组间休息(秒)
     */
    private Integer restTime;

    /**
     * 所需器械
     */
    private String equipment;

    /**
     * 目标肌群
     */
    private String targetMuscles;

    /**
     * 难度 1简单 2中等 3困难
     */
    private Integer difficulty;

    /**
     * 示范图片
     */
    private String demoImage;

    /**
     * 示范视频
     */
    private String demoVideo;

    /**
     * 排序
     */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
