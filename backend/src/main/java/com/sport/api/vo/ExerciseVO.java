package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 训练动作VO
 */
@Data
public class ExerciseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long courseId;

    private String name;

    private String description;

    private Integer type;

    private String typeName;

    private Integer duration;

    private Integer sets;

    private Integer reps;

    private Integer restTime;

    private Integer distance;

    private String equipment;

    private String targetMuscles;

    private Integer difficulty;

    private Integer sortOrder;

    private Boolean isRepeatGroup;

    private Integer repeatCount;
}
