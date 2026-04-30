package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 训练课程VO
 */
@Data
public class CourseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long planId;

    private String name;

    private String description;

    private Integer week;

    private Integer day;

    private Integer type;

    private String typeName;

    private Integer level;

    private Integer duration;

    private Integer warmUpDuration;

    private Integer coolDownDuration;

    private Integer sortOrder;

    /** 动作/步骤列表 */
    private List<ExerciseVO> exercises;
}
