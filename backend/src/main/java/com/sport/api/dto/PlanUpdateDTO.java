package com.sport.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 更新训练计划DTO
 */
@Data
public class PlanUpdateDTO {

    private String name;

    private String description;

    private String coverImage;

    private Integer level;

    @Min(value = 1)
    private Integer totalWeeks;

    /** 更新课程时传入完整课程列表 */
    @Valid
    private List<CourseDTO> courses;
}
