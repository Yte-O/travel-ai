package com.gjq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.entity.Like;

import java.util.List;

/**
 * 点赞Service
 */
public interface LikeService extends IService<Like> {

    /**
     * 点赞
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 是否成功
     */
    boolean like(Long userId, Long itemId);

    /**
     * 取消点赞
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 是否成功
     */
    boolean unlike(Long userId, Long itemId);

    /**
     * 查询用户是否点赞
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 是否点赞
     */
    boolean isLiked(Long userId, Long itemId);

    /**
     * 获取物品点赞数
     *
     * @param itemId 物品ID
     * @return 点赞数
     */
    long getLikeCount(Long itemId);

    /**
     * 获取用户点赞的物品ID列表
     *
     * @param userId 用户ID
     * @return 物品ID列表
     */
    List<Long> getUserLikedItemIds(Long userId);
} 