package com.sport.api.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 训练趋势VO
 */
@Data
@Builder
public class TrainingTrendVO {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 日期标签（如：周一、1/15等）
     */
    private String dateLabel;

    /**
     * 训练时长(分钟)
     */
    private Integer duration;

    /**
     * 消耗卡路里
     */
    private Integer calories;

    /**
     * 完成动作数
     */
    private Integer exerciseCount;

    /**
     * 是否完成目标
     */
    private Boolean goalAchieved;
}
