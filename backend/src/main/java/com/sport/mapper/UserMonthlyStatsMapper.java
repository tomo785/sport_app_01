package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.UserMonthlyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户每月统计Mapper
 */
@Mapper
public interface UserMonthlyStatsMapper extends BaseMapper<UserMonthlyStats> {

    /**
     * 根据用户ID、年份、月份查询统计记录
     */
    @Select("SELECT * FROM t_user_monthly_stats WHERE user_id = #{userId} AND stat_year = #{year} AND stat_month = #{month} LIMIT 1")
    UserMonthlyStats selectByUserIdAndYearMonth(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);
}
