package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系Mapper
 */
@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {
}
