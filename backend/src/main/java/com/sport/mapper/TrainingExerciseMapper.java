package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.TrainingExercise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 训练动作Mapper
 */
@Mapper
public interface TrainingExerciseMapper extends BaseMapper<TrainingExercise> {

    /**
     * 根据课程ID查询动作列表
     */
    @Select("SELECT * FROM t_training_exercise WHERE course_id = #{courseId} AND delete_flag = 0 ORDER BY sort_order ASC")
    List<TrainingExercise> selectByCourseId(@Param("courseId") Long courseId);
}
