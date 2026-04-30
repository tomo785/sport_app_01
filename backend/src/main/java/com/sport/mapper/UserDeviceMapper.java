package com.sport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.model.entity.UserDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户设备Mapper
 */
@Mapper
public interface UserDeviceMapper extends BaseMapper<UserDevice> {
}
