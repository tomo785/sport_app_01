package com.sport.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建训练计划DTO
 */
@Data
public class PlanCreateDTO {

    @NotBlank(message = "计划名称不能为空")
    private String name;

    private String description;

    private String coverImage;

    @NotNull(message = "难度等级不能为空")
    private Integer level;

    @NotNull(message = "总周数不能为空")
    @Min(value = 1, message = "总周数至少为1")
    private Integer totalWeeks;

    /**
     * 课程列表（按周和天组织）
     */
    @Valid
    private List<CourseDTO> courses;
}
