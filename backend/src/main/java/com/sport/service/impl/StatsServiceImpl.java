package com.sport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.api.vo.StatsDailyVO;
import com.sport.api.vo.StatsSummaryVO;
import com.sport.api.vo.StatsTrendVO;
import com.sport.mapper.UserDailyStatsMapper;
import com.sport.mapper.WorkoutRecordMapper;
import com.sport.model.entity.UserDailyStats;
import com.sport.model.entity.WorkoutRecord;
import com.sport.model.enums.WorkoutStatusEnum;
import com.sport.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserDailyStatsMapper userDailyStatsMapper;
    private final WorkoutRecordMapper workoutRecordMapper;

    @Override
    public StatsDailyVO getDailyStats(Long userId, LocalDate statDate) {
        UserDailyStats stats = userDailyStatsMapper.selectByUserIdAndDate(userId, statDate);
        if (stats == null) {
            return createEmptyDailyVO(statDate);
        }
        return convertToDailyVO(stats);
    }

    @Override
    public List<StatsDailyVO> getDailyStatsList(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<UserDailyStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDailyStats::getUserId, userId)
                .ge(UserDailyStats::getStatDate, startDate)
                .le(UserDailyStats::getStatDate, endDate)
                .orderByAsc(UserDailyStats::getStatDate);

        List<UserDailyStats> statsList = userDailyStatsMapper.selectList(wrapper);
        return statsList.stream()
                .map(this::convertToDailyVO)
                .collect(Collectors.toList());
    }

    @Override
    public StatsSummaryVO getStatsSummary(Long userId) {
        StatsSummaryVO vo = new StatsSummaryVO();

        // 查询所有已完成的运动记录
        LambdaQueryWrapper<WorkoutRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutRecord::getUserId, userId)
                .eq(WorkoutRecord::getStatus, WorkoutStatusEnum.COMPLETED.getCode());
        List<WorkoutRecord> records = workoutRecordMapper.selectList(wrapper);

        // 计算总数据
        int totalWorkouts = records.size();
        int totalDistance = records.stream().mapToInt(r -> r.getDistance() != null ? r.getDistance() : 0).sum();
        int totalDuration = records.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        int totalCalories = records.stream().mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0).sum();

        vo.setTotalWorkouts(totalWorkouts);
        vo.setTotalDistance(totalDistance);
        vo.setTotalDuration(totalDuration);
        vo.setTotalCalories(totalCalories);

        // 计算连续运动天数
        vo.setStreakDays(calculateStreakDays(userId));

        // 计算本周运动次数
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        long weeklyCount = records.stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().toLocalDate().isBefore(weekStart))
                .count();
        vo.setWeeklyWorkouts((int) weeklyCount);

        // 计算本月运动次数
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long monthlyCount = records.stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().toLocalDate().isBefore(monthStart))
                .count();
        vo.setMonthlyWorkouts((int) monthlyCount);

        // 格式化数据
        vo.setTotalDistanceStr(formatDistance(totalDistance));
        vo.setTotalDurationStr(formatDuration(totalDuration));
        vo.setTotalCaloriesStr(formatCalories(totalCalories));

        return vo;
    }

    @Override
    public StatsTrendVO getWeeklyTrend(Long userId) {
        StatsTrendVO vo = new StatsTrendVO();
        vo.setType("week");

        List<String> dates = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        List<Integer> calories = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 获取最近7天的数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<StatsDailyVO> dailyStats = getDailyStatsList(userId, startDate, endDate);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            dates.add(date.toString().substring(5)); // MM-dd格式

            StatsDailyVO stats = dailyStats.stream()
                    .filter(s -> s.getStatDate().equals(currentDate))
                    .findFirst()
                    .orElse(null);

            if (stats != null) {
                distances.add(stats.getTotalDistance());
                durations.add(stats.getTotalDuration());
                calories.add(stats.getTotalCalories());
                counts.add(stats.getRecordCount());
            } else {
                distances.add(0);
                durations.add(0);
                calories.add(0);
                counts.add(0);
            }
        }

        vo.setDates(dates);
        vo.setDistances(distances);
        vo.setDurations(durations);
        vo.setCalories(calories);
        vo.setCounts(counts);

        return vo;
    }

    @Override
    public StatsTrendVO getMonthlyTrend(Long userId) {
        StatsTrendVO vo = new StatsTrendVO();
        vo.setType("month");

        List<String> dates = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        List<Integer> calories = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 获取最近30天的数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        List<StatsDailyVO> dailyStats = getDailyStatsList(userId, startDate, endDate);

        // 每3天聚合一次数据
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(3)) {
            final LocalDate currentStart = date;
            final LocalDate currentEnd = date.plusDays(2).isAfter(endDate) ? endDate : date.plusDays(2);
            dates.add(currentStart.toString().substring(5) + "~" + currentEnd.toString().substring(5));

            int periodDistance = 0;
            int periodDuration = 0;
            int periodCalories = 0;
            int periodCount = 0;

            for (LocalDate d = currentStart; !d.isAfter(currentEnd); d = d.plusDays(1)) {
                final LocalDate currentDate = d;
                StatsDailyVO stats = dailyStats.stream()
                        .filter(s -> s.getStatDate().equals(currentDate))
                        .findFirst()
                        .orElse(null);

                if (stats != null) {
                    periodDistance += stats.getTotalDistance();
                    periodDuration += stats.getTotalDuration();
                    periodCalories += stats.getTotalCalories();
                    periodCount += stats.getRecordCount();
                }
            }

            distances.add(periodDistance);
            durations.add(periodDuration);
            calories.add(periodCalories);
            counts.add(periodCount);
        }

        vo.setDates(dates);
        vo.setDistances(distances);
        vo.setDurations(durations);
        vo.setCalories(calories);
        vo.setCounts(counts);

        return vo;
    }

    @Override
    public StatsTrendVO getYearlyTrend(Long userId, Integer year) {
        StatsTrendVO vo = new StatsTrendVO();
        vo.setType("year");

        List<String> dates = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        List<Integer> calories = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 获取指定年份的12个月的数据
        for (int month = 1; month <= 12; month++) {
            dates.add(month + "月");

            // 查询该月的运动记录
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());

            LambdaQueryWrapper<WorkoutRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkoutRecord::getUserId, userId)
                    .eq(WorkoutRecord::getStatus, WorkoutStatusEnum.COMPLETED.getCode())
                    .ge(WorkoutRecord::getStartTime, monthStart.atStartOfDay())
                    .lt(WorkoutRecord::getStartTime, monthEnd.plusDays(1).atStartOfDay());

            List<WorkoutRecord> records = workoutRecordMapper.selectList(wrapper);

            int monthDistance = records.stream().mapToInt(r -> r.getDistance() != null ? r.getDistance() : 0).sum();
            int monthDuration = records.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
            int monthCalories = records.stream().mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0).sum();

            distances.add(monthDistance);
            durations.add(monthDuration);
            calories.add(monthCalories);
            counts.add(records.size());
        }

        vo.setDates(dates);
        vo.setDistances(distances);
        vo.setDurations(durations);
        vo.setCalories(calories);
        vo.setCounts(counts);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStepsData(Long userId, Integer steps, Integer calories, Integer distance, Integer duration) {
        LocalDate today = LocalDate.now();
        UserDailyStats stats = userDailyStatsMapper.selectByUserIdAndDate(userId, today);

        if (stats == null) {
            stats = new UserDailyStats();
            stats.setUserId(userId);
            stats.setStatDate(today);
            stats.setTotalSteps(steps != null ? steps : 0);
            if (calories != null) stats.setTotalCalories(calories);
            if (distance != null) stats.setTotalDistance(distance);
            if (duration != null) stats.setTotalDuration(duration);
            stats.setRecordCount(0);
            stats.setCreateTime(java.time.LocalDateTime.now());
            stats.setUpdateTime(java.time.LocalDateTime.now());
            userDailyStatsMapper.insert(stats);
        } else {
            // 优先使用更大的步数（防止覆盖已存在的运动记录数据）
            int newSteps = steps != null ? steps : 0;
            if (newSteps > (stats.getTotalSteps() != null ? stats.getTotalSteps() : 0)) {
                stats.setTotalSteps(newSteps);
            }
            if (calories != null) stats.setTotalCalories(calories);
            if (distance != null) stats.setTotalDistance(distance);
            if (duration != null) stats.setTotalDuration(duration);
            stats.setUpdateTime(java.time.LocalDateTime.now());
            userDailyStatsMapper.updateById(stats);
        }

        log.info("保存步数数据 - 用户ID: {}, 步数: {}", userId, steps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDailyStats(Long userId, LocalDate statDate) {
        // 查询当天的运动记录
        LambdaQueryWrapper<WorkoutRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutRecord::getUserId, userId)
                .eq(WorkoutRecord::getStatus, WorkoutStatusEnum.COMPLETED.getCode())
                .ge(WorkoutRecord::getStartTime, statDate.atStartOfDay())
                .lt(WorkoutRecord::getStartTime, statDate.plusDays(1).atStartOfDay());

        List<WorkoutRecord> records = workoutRecordMapper.selectList(wrapper);

        // 统计数据
        int totalDistance = records.stream().mapToInt(r -> r.getDistance() != null ? r.getDistance() : 0).sum();
        int totalDuration = records.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        int totalCalories = records.stream().mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0).sum();
        int totalSteps = records.stream().mapToInt(r -> r.getSteps() != null ? r.getSteps() : 0).sum();
        int recordCount = records.size();

        // 查询是否已有记录
        UserDailyStats stats = userDailyStatsMapper.selectByUserIdAndDate(userId, statDate);

        if (stats == null) {
            stats = new UserDailyStats();
            stats.setUserId(userId);
            stats.setStatDate(statDate);
            stats.setTotalDistance(totalDistance);
            stats.setTotalDuration(totalDuration);
            stats.setTotalCalories(totalCalories);
            stats.setTotalSteps(totalSteps);
            stats.setRecordCount(recordCount);
            stats.setCreateTime(java.time.LocalDateTime.now());
            stats.setUpdateTime(java.time.LocalDateTime.now());
            userDailyStatsMapper.insert(stats);
        } else {
            stats.setTotalDistance(totalDistance);
            stats.setTotalDuration(totalDuration);
            stats.setTotalCalories(totalCalories);
            stats.setTotalSteps(totalSteps);
            stats.setRecordCount(recordCount);
            stats.setUpdateTime(java.time.LocalDateTime.now());
            userDailyStatsMapper.updateById(stats);
        }

        log.info("更新每日统计 - 用户ID: {}, 日期: {}, 距离: {}, 次数: {}",
                userId, statDate, totalDistance, recordCount);
    }

    /**
     * 计算连续运动天数
     */
    private int calculateStreakDays(Long userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            UserDailyStats stats = userDailyStatsMapper.selectByUserIdAndDate(userId, date);
            if (stats != null && stats.getRecordCount() > 0) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 转换为DailyVO
     */
    private StatsDailyVO convertToDailyVO(UserDailyStats stats) {
        StatsDailyVO vo = new StatsDailyVO();
        BeanUtils.copyProperties(stats, vo);
        vo.setSteps(stats.getTotalSteps());

        // 格式化数据
        vo.setDistanceStr(formatDistance(stats.getTotalDistance()));
        vo.setDurationStr(formatDuration(stats.getTotalDuration()));
        vo.setCaloriesStr(formatCalories(stats.getTotalCalories()));

        return vo;
    }

    /**
     * 创建空的DailyVO
     */
    private StatsDailyVO createEmptyDailyVO(LocalDate statDate) {
        StatsDailyVO vo = new StatsDailyVO();
        vo.setStatDate(statDate);
        vo.setTotalDistance(0);
        vo.setTotalDuration(0);
        vo.setTotalCalories(0);
        vo.setSteps(0);
        vo.setRecordCount(0);
        vo.setGoalProgress(0);
        vo.setDistanceStr("0m");
        vo.setDurationStr("0分");
        vo.setCaloriesStr("0千卡");
        return vo;
    }

    /**
     * 格式化距离
     */
    private String formatDistance(Integer meters) {
        if (meters == null || meters == 0) {
            return "0m";
        }
        if (meters < 1000) {
            return meters + "m";
        }
        BigDecimal km = new BigDecimal(meters).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
        return km.stripTrailingZeros().toPlainString() + "km";
    }

    /**
     * 格式化时长
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) {
            return "0分";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return hours + "小时" + minutes + "分";
        } else if (minutes > 0) {
            return minutes + "分" + (secs > 0 ? secs + "秒" : "");
        } else {
            return secs + "秒";
        }
    }

    /**
     * 格式化卡路里
     */
    private String formatCalories(Integer calories) {
        if (calories == null || calories == 0) {
            return "0千卡";
        }
        return calories + "千卡";
    }
}
