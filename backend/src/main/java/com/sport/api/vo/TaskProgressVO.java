package com.sport.api.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 任务进度VO
 */
@Data
@Builder
public class TaskProgressVO {

    /**
     * 总任务数
     */
    private Integer totalTasks;

    /**
     * 已完成任务数
     */
    private Integer completedTasks;

    /**
     * 剩余任务数
     */
    private Integer remainingTasks;

    /**
     * 完成进度百分比
     */
    private Integer progressPercent;

    /**
     * 预计总时长(分钟)
     */
    private Integer totalDuration;

    /**
     * 已完成时长(分钟)
     */
    private Integer completedDuration;

    /**
     * 剩余时长(分钟)
     */
    private Integer remainingTime;

    /**
     * 总组数
     */
    private Integer totalSets;

    /**
     * 已完成组数
     */
    private Integer completedSets;
}
