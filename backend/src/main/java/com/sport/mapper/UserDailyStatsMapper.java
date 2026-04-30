package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.UserDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 用户每日统计Mapper
 */
@Mapper
public interface UserDailyStatsMapper extends BaseMapper<UserDailyStats> {

    /**
     * 根据用户ID和日期查询统计记录
     */
    @Select("SELECT * FROM t_user_daily_stats WHERE user_id = #{userId} AND stat_date = #{statDate} LIMIT 1")
    UserDailyStats selectByUserIdAndDate(@Param("userId") Long userId, @Param("statDate") LocalDate statDate);
}
