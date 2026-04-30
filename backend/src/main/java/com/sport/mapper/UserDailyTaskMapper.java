package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.UserDailyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户每日任务Mapper
 */
@Mapper
public interface UserDailyTaskMapper extends BaseMapper<UserDailyTask> {

    /**
     * 根据用户ID和日期查询任务
     */
    @Select("SELECT * FROM t_user_daily_task WHERE user_id = #{userId} AND task_date = #{taskDate} LIMIT 1")
    UserDailyTask selectByUserIdAndDate(@Param("userId") Long userId, @Param("taskDate") LocalDate taskDate);

    /**
     * 查询用户最近7天的任务
     */
    @Select("SELECT * FROM t_user_daily_task WHERE user_id = #{userId} AND task_date >= #{startDate} " +
            "ORDER BY task_date DESC")
    List<UserDailyTask> selectRecentTasks(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    /**
     * 查询用户今日任务（带动作详情）
     */
    UserDailyTask selectTodayTaskWithExercises(@Param("userId") Long userId, @Param("taskDate") LocalDate taskDate);
}
