package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.WorkoutGoal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运动目标Mapper
 */
@Mapper
public interface WorkoutGoalMapper extends BaseMapper<WorkoutGoal> {
}
