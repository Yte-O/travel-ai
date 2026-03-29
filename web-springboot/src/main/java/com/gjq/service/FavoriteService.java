package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.entity.Favorite;
import com.gjq.vo.favorite.FavoriteVO;

import java.util.List;

/**
 * 收藏Service
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long itemId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long itemId);

    /**
     * 查询用户是否收藏
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 是否收藏
     */
    boolean isFavorite(Long userId, Long itemId);

    /**
     * 获取用户收藏的物品ID列表
     *
     * @param userId 用户ID
     * @return 物品ID列表
     */
    List<Long> getUserFavoriteItemIds(Long userId);

    /**
     * 分页获取用户收藏列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 收藏列表
     */
    Page<FavoriteVO> getUserFavorites(Long userId, Page<Favorite> page);

    /**
     * 获取物品收藏数
     *
     * @param itemId 物品ID
     * @return 收藏数
     */
    long getFavoriteCount(Long itemId);
} 