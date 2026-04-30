package com.sport.api.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 任务看板VO（上区块完整数据）
 */
@Data
@Builder
public class TaskDashboardVO {

    /**
     * 今日任务
     */
    private TodayTaskVO todayTask;

    /**
     * 训练进度
     */
    private TaskProgressVO progress;

    /**
     * 数据看板
     */
    private DataBoardVO dataBoard;

    /**
     * 7天训练趋势
     */
    private List<TrainingTrendVO> weeklyTrend;

    /**
     * 30天训练趋势
     */
    private List<TrainingTrendVO> monthlyTrend;
}
