package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统计趋势VO
 */
@Data
public class StatsTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期列表
     */
    private List<String> dates;

    /**
     * 距离列表(米)
     */
    private List<Integer> distances;

    /**
     * 时长列表(秒)
     */
    private List<Integer> durations;

    /**
     * 卡路里列表
     */
    private List<Integer> calories;

    /**
     * 次数列表
     */
    private List<Integer> counts;

    /**
     * 趋势类型: week-周趋势 month-月趋势 year-年趋势
     */
    private String type;
}
