package com.sport.api.vo;

import lombok.Data;

/**
 * 任务流程图目标VO
 */
@Data
public class TaskFlowGoalVO {

    /**
     * 目标ID
     */
    private Long id;

    /**
     * 目标标题
     */
    private String title;

    /**
     * 目标描述
     */
    private String description;

    /**
     * 目标进度值
     */
    private Integer targetProgress;

    /**
     * 当前进度值
     */
    private Integer currentProgress;
}
