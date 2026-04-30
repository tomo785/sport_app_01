package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 排行榜历史记录实体
 */
@Data
@TableName("t_leaderboard_history")
public class LeaderboardHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private Integer period;

    private LocalDate statDate;

    private Integer rank;

    private Long steps;

    private Integer calories;

    private Integer distance;

    private Integer recordCount;

    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;
}
