package com.gjq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjq.entity.UserAction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为Mapper接口
 */
@Mapper
public interface UserActionMapper extends BaseMapper<UserAction> {
} 