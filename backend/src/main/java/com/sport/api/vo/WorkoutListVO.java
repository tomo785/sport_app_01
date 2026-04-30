package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 运动记录列表VO
 */
@Data
public class WorkoutListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录列表
     */
    private List<WorkoutDetailVO> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Long pages;
}
