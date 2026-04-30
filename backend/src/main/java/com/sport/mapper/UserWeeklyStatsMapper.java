package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.UserWeeklyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 用户每周统计Mapper
 */
@Mapper
public interface UserWeeklyStatsMapper extends BaseMapper<UserWeeklyStats> {

    /**
     * 根据用户ID和周开始日期查询统计记录
     */
    @Select("SELECT * FROM t_user_weekly_stats WHERE user_id = #{userId} AND week_start = #{weekStart} LIMIT 1")
    UserWeeklyStats selectByUserIdAndWeekStart(@Param("userId") Long userId, @Param("weekStart") LocalDate weekStart);
}
