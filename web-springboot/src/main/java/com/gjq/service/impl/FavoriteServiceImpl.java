package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.entity.Favorite;
import com.gjq.entity.Item;
import com.gjq.mapper.FavoriteMapper;
import com.gjq.service.FavoriteService;
import com.gjq.service.ItemService;
import com.gjq.vo.favorite.FavoriteVO;
import com.gjq.vo.item.ItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏Service实现
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private ItemService itemService;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long itemId) {
        // 检查是否已收藏
        if (isFavorite(userId, itemId)) {
            return true;
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        return save(favorite);
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);
        
        // 删除收藏记录
        return remove(queryWrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);
        
        // 查询是否存在记录
        return count(queryWrapper) > 0;
    }

    @Override
    public List<Long> getUserFavoriteItemIds(Long userId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .select(Favorite::getItemId);
        
        // 查询用户收藏的物品ID列表
        return list(queryWrapper).stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FavoriteVO> getUserFavorites(Long userId, Page<Favorite> page) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        
        // 查询收藏记录
        Page<Favorite> favoriteList = page(page, queryWrapper);
        
        // 提取物品ID列表
        List<Long> itemIds = favoriteList.getRecords().stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toList());
        
        // 如果没有收藏记录，直接返回空页
        if (itemIds.isEmpty()) {
            return new Page<FavoriteVO>()
                    .setRecords(new ArrayList<>())
                    .setCurrent(favoriteList.getCurrent())
                    .setSize(favoriteList.getSize())
                    .setTotal(favoriteList.getTotal());
        }
        
        // 批量查询物品信息
        List<ItemVO> itemList = new ArrayList<>();
        for (Long itemId : itemIds) {
            ItemVO itemVO = itemService.getById(itemId);
            if (itemVO != null) {
                itemList.add(itemVO);
            }
        }
        
        // 构建物品映射
        Map<Long, ItemVO> itemMap = itemList.stream()
                .collect(Collectors.toMap(ItemVO::getId, item -> item));
        
        // 构建VO
        List<FavoriteVO> voList = favoriteList.getRecords().stream()
                .map(favorite -> {
                    FavoriteVO vo = new FavoriteVO();
                    BeanUtils.copyProperties(favorite, vo);
                    vo.setItem(itemMap.get(favorite.getItemId()));
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 构建分页结果
        Page<FavoriteVO> voPage = new Page<>();
        BeanUtils.copyProperties(favoriteList, voPage, "records");
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public long getFavoriteCount(Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getItemId, itemId);
        
        // 查询并返回收藏数量
        return count(queryWrapper);
    }
} 