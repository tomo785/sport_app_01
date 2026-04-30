package com.sport.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 排行榜规则实体
 */
@Data
@TableName("t_leaderboard_rule")
public class LeaderboardRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer type;

    private Integer period;

    private Integer displayCount;

    private Integer minSteps;

    private Integer minCalories;

    private Integer minDistance;

    private Integer minRecords;

    private Integer refreshInterval;

    private String badgeRanks;

    private Integer sortOrder;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
