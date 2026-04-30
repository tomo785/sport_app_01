package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.LeaderboardHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排行榜历史Mapper
 */
@Mapper
public interface LeaderboardHistoryMapper extends BaseMapper<LeaderboardHistory> {
}
