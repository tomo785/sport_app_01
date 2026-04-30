package com.sport.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 训练课程DTO
 */
@Data
public class CourseDTO {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "周次不能为空")
    private Integer week;

    @NotNull(message = "天次不能为空")
    private Integer day;

    private Integer type;

    private Integer level;

    private Integer duration;

    private Integer warmUpDuration;

    private Integer coolDownDuration;

    private Integer sortOrder;

    /**
     * 动作/步骤列表
     */
    @Valid
    private List<ExerciseDTO> exercises;
}
