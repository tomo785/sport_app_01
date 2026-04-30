package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练记录Mapper
 */
@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {
}
