package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.PageDTO;
import com.gjq.dto.favorite.FavoriteDTO;
import com.gjq.entity.Favorite;
import com.gjq.service.FavoriteService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.favorite.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     *
     * @param favoriteDTO 收藏DTO
     * @return 结果
     */
    @PostMapping
    public Result<?> addFavorite(@RequestBody FavoriteDTO favoriteDTO) {
        Long userId = SecurityUtils.getUserId();
        boolean success = favoriteService.addFavorite(userId, favoriteDTO.getItemId());
        return success ? Result.success() : Result.error("收藏失败");
    }

    /**
     * 取消收藏
     *
     * @param itemId 物品ID
     * @return 结果
     */
    @DeleteMapping("/{itemId}")
    public Result<?> removeFavorite(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean success = favoriteService.removeFavorite(userId, itemId);
        return success ? Result.success() : Result.error("取消收藏失败");
    }

    /**
     * 查询收藏状态
     *
     * @param itemId 物品ID
     * @return 收藏状态
     */
    @GetMapping("/status/{itemId}")
    public Result<Boolean> status(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, itemId);
        return Result.success(isFavorite);
    }

    /**
     * 获取用户收藏的物品ID列表
     *
     * @return 物品ID列表
     */
    @GetMapping("/user/items")
    public Result<List<Long>> userFavoriteItems() {
        Long userId = SecurityUtils.getUserId();
        List<Long> itemIds = favoriteService.getUserFavoriteItemIds(userId);
        return Result.success(itemIds);
    }

    /**
     * 分页获取用户收藏列表
     *
     * @param pageDTO 分页参数
     * @return 收藏列表
     */
    @GetMapping("/user/page")
    public Result<Page<FavoriteVO>> userFavoritePage(PageDTO pageDTO) {
        Long userId = SecurityUtils.getUserId();
        Page<Favorite> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<FavoriteVO> voPage = favoriteService.getUserFavorites(userId, page);
        return Result.success(voPage);
    }

    /**
     * 获取物品收藏数
     *
     * @param itemId 物品ID
     * @return 收藏数
     */
    @GetMapping("/count/{itemId}")
    public Result<Long> getFavoriteCount(@PathVariable Long itemId) {
        long count = favoriteService.getFavoriteCount(itemId);
        return Result.success(count);
    }
    
    /**
     * 管理员分页获取所有收藏数据
     * 
     * @param pageDTO 分页参数
     * @return 收藏数据列表
     */
    @GetMapping("/admin/page")
    public Result<Page<Favorite>> adminFavoritePage(PageDTO pageDTO) {
        Page<Favorite> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<Favorite> resultPage = favoriteService.page(page);
        return Result.success(resultPage);
    }
} 