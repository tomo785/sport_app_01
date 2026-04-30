package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.WorkoutTrajectory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 运动轨迹Mapper
 */
@Mapper
public interface WorkoutTrajectoryMapper extends BaseMapper<WorkoutTrajectory> {

    /**
     * 根据运动记录ID查询轨迹点列表
     * @param recordId 运动记录ID
     * @return 轨迹点列表
     */
    @Select("SELECT * FROM t_workout_trajectory WHERE record_id = #{recordId} ORDER BY sequence ASC")
    List<WorkoutTrajectory> selectByRecordId(@Param("recordId") Long recordId);
}
