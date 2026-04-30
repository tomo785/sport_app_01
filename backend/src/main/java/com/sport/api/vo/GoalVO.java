package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动目标VO
 */
@Data
public class GoalVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标类型 1每日距离 2每周次数
     */
    private Integer type;

    /**
     * 目标类型名称
     */
    private String typeName;

    /**
     * 目标值(米/次数)
     */
    private Integer targetValue;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 状态 0已取消 1进行中 2已完成
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
