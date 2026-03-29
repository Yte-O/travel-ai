package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.entity.Like;
import com.gjq.mapper.LikeMapper;
import com.gjq.service.LikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 点赞Service实现
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Override
    @Transactional
    public boolean like(Long userId, Long itemId) {
        // 检查是否已点赞
        if (isLiked(userId, itemId)) {
            return true;
        }

        // 创建点赞记录
        Like like = new Like();
        like.setUserId(userId);
        like.setItemId(itemId);
        return save(like);
    }

    @Override
    @Transactional
    public boolean unlike(Long userId, Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .eq(Like::getItemId, itemId);
        
        // 删除点赞记录
        return remove(queryWrapper);
    }

    @Override
    public boolean isLiked(Long userId, Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .eq(Like::getItemId, itemId);
        
        // 查询是否存在记录
        return count(queryWrapper) > 0;
    }

    @Override
    public long getLikeCount(Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getItemId, itemId);
        
        // 查询点赞数量
        return count(queryWrapper);
    }

    @Override
    public List<Long> getUserLikedItemIds(Long userId) {
        // 构建查询条件
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .select(Like::getItemId);
        
        // 查询用户点赞的物品ID列表
        return list(queryWrapper).stream()
                .map(Like::getItemId)
                .collect(Collectors.toList());
    }
} 