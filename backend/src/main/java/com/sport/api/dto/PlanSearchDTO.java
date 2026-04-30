package com.sport.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 计划搜索/匹配DTO
 */
@Data
public class PlanSearchDTO {

    /** 搜索关键词 */
    private String keyword;

    /** 训练类型 1跑步 2力量 3有氧 */
    private Integer trainingType;

    /** 经验水平 1新手 2进阶 3高手 */
    private Integer level;

    /** 可用训练日(1-7) */
    private List<Integer> availableDays;

    /** 每周可训练天数 */
    private Integer daysPerWeek;

    /** 计划周数 */
    private Integer totalWeeks;

    /** 每次训练时长(分钟) */
    private Integer durationPerSession;

    /** 目标：减脂/增肌/耐力/备赛 */
    private String goal;

    /** 排序方式：rating/viewCount/enrollCount */
    private String sortBy;
}
