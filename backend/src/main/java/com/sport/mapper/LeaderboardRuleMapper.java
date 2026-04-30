package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.LeaderboardRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排行榜规则Mapper
 */
@Mapper
public interface LeaderboardRuleMapper extends BaseMapper<LeaderboardRule> {
}
