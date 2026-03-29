package com.gjq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjq.entity.Item;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品Mapper接口
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
} 