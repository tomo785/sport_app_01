package com.sport.api.vo;

import lombok.Data;

import java.util.List;

/**
 * 任务流程图VO
 */
@Data
public class TaskFlowVO {

    /**
     * 目标信息
     */
    private TaskFlowGoalVO goal;

    /**
     * 任务列表
     */
    private List<TaskFlowItemVO> tasks;

    /**
     * 总任务数
     */
    private Integer totalTasks;

    /**
     * 已完成任务数
     */
    private Integer completedTasks;

    /**
     * 总进度百分比
     */
    private Integer totalProgress;
}
