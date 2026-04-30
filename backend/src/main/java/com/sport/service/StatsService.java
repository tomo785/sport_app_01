package com.sport.service;

import com.sport.api.vo.StatsDailyVO;
import com.sport.api.vo.StatsSummaryVO;
import com.sport.api.vo.StatsTrendVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计服务接口
 */
public interface StatsService {

    /**
     * 获取每日统计
     *
     * @param userId   用户ID
     * @param statDate 统计日期
     * @return 每日统计
     */
    StatsDailyVO getDailyStats(Long userId, LocalDate statDate);

    /**
     * 获取一段时间内的每日统计列表
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计列表
     */
    List<StatsDailyVO> getDailyStatsList(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取统计汇总
     *
     * @param userId 用户ID
     * @return 统计汇总
     */
    StatsSummaryVO getStatsSummary(Long userId);

    /**
     * 获取周趋势
     *
     * @param userId 用户ID
     * @return 周趋势
     */
    StatsTrendVO getWeeklyTrend(Long userId);

    /**
     * 获取月趋势
     *
     * @param userId 用户ID
     * @return 月趋势
     */
    StatsTrendVO getMonthlyTrend(Long userId);

    /**
     * 获取年趋势
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 年趋势
     */
    StatsTrendVO getYearlyTrend(Long userId, Integer year);

    /**
     * 更新或创建每日统计
     *
     * @param userId   用户ID
     * @param statDate 统计日期
     */
    void updateDailyStats(Long userId, LocalDate statDate);
}
