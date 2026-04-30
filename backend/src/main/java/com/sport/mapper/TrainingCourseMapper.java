package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练课程Mapper
 */
@Mapper
public interface TrainingCourseMapper extends BaseMapper<TrainingCourse> {
}
