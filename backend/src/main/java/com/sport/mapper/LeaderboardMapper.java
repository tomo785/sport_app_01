package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.Leaderboard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排行榜Mapper
 */
@Mapper
public interface LeaderboardMapper extends BaseMapper<Leaderboard> {
}
