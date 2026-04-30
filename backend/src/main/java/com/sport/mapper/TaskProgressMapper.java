package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.TaskProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务进度Mapper
 */
@Mapper
public interface TaskProgressMapper extends BaseMapper<TaskProgress> {
}
