package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.TrainingPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练计划Mapper
 */
@Mapper
public interface TrainingPlanMapper extends BaseMapper<TrainingPlan> {
}
