package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.WorkoutRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运动记录Mapper
 */
@Mapper
public interface WorkoutRecordMapper extends BaseMapper<WorkoutRecord> {
}
