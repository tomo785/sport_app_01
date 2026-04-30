package com.sport.api.vo;

import lombok.Data;

/**
 * 任务流程图单项VO
 */
@Data
public class TaskFlowItemVO {

    /**
     * 任务ID（动作记录ID）
     */
    private Long id;

    /**
     * 任务名称（动作名称）
     */
    private String name;

    /**
     * 任务类型: warmup/strength/cardio/core/stretch/rest
     */
    private String type;

    /**
     * 优先级: high/medium/low
     */
    private String priority;

    /**
     * 状态: 0-未开始, 1-进行中, 2-已完成
     */
    private Integer status;

    /**
     * 预计时长(分钟)
     */
    private Integer duration;

    /**
     * 组数
     */
    private Integer sets;

    /**
     * 消耗卡路里
     */
    private Integer calories;

    /**
     * 顺序
     */
    private Integer order;

    /**
     * 图标
     */
    private String icon;
}
