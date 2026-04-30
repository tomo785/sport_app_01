package com.sport.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 训练动作DTO
 */
@Data
public class ExerciseDTO {

    @NotBlank(message = "动作名称不能为空")
    private String name;

    private String description;

    private Integer type;

    /** 时长(秒) */
    private Integer duration;

    /** 组数 */
    private Integer sets;

    /** 每组次数 */
    private Integer reps;

    /** 组间休息(秒) */
    private Integer restTime;

    /** 距离(米)，用于跑步 */
    private Integer distance;

    /** 所需器械 */
    private String equipment;

    /** 目标肌群 */
    private String targetMuscles;

    private Integer difficulty;

    private Integer sortOrder;

    /** 是否为重复组 */
    private Boolean isRepeatGroup;

    /** 重复次数 */
    private Integer repeatCount;
}
