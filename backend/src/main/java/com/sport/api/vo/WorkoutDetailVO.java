package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运动记录详情VO
 */
@Data
public class WorkoutDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 运动类型 1跑步 2健走 3骑行
     */
    private Integer type;

    /**
     * 运动类型名称
     */
    private String typeName;

    /**
     * 状态 0进行中 1已完成 2已取消
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 开始时间
     */
    private String startTimeStr;

    /**
     * 结束时间
     */
    private String endTimeStr;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 距离(米)
     */
    private Integer distance;

    /**
     * 卡路里(千卡)
     */
    private Integer calories;

    /**
     * 步数
     */
    private Integer steps;

    /**
     * 平均配速(秒/公里)
     */
    private Integer avgPace;

    /**
     * 最佳配速(秒/公里)
     */
    private Integer bestPace;

    /**
     * 平均速度(米/秒)
     */
    private BigDecimal avgSpeed;

    /**
     * 最大速度(米/秒)
     */
    private BigDecimal maxSpeed;

    /**
     * 最低海拔(米)
     */
    private Integer minAltitude;

    /**
     * 最高海拔(米)
     */
    private Integer maxAltitude;

    /**
     * 累计爬升(米)
     */
    private Integer totalAscent;

    /**
     * 累计下降(米)
     */
    private Integer totalDescent;

    /**
     * 轨迹点数量
     */
    private Integer trajectoryCount;
}
