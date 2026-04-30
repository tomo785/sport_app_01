package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.UserTaskExercise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户任务动作记录Mapper
 */
@Mapper
public interface UserTaskExerciseMapper extends BaseMapper<UserTaskExercise> {

    /**
     * 根据任务ID查询动作记录列表
     */
    @Select("SELECT * FROM t_user_task_exercise WHERE task_id = #{taskId} ORDER BY sort_order ASC")
    List<UserTaskExercise> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据任务ID查询未完成的动作数量
     */
    @Select("SELECT COUNT(*) FROM t_user_task_exercise WHERE task_id = #{taskId} AND status != 2")
    Integer countUncompletedByTaskId(@Param("taskId") Long taskId);
}
