package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.PageDTO;
import com.gjq.dto.like.LikeDTO;
import com.gjq.entity.Like;
import com.gjq.service.LikeService;
import com.gjq.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * 点赞
     *
     * @param likeDTO 点赞DTO
     * @return 结果
     */
    @PostMapping
    public Result<?> like(@RequestBody LikeDTO likeDTO) {
        Long userId = SecurityUtils.getUserId();
        boolean success = likeService.like(userId, likeDTO.getItemId());
        return success ? Result.success() : Result.error("点赞失败");
    }

    /**
     * 取消点赞
     *
     * @param itemId 物品ID
     * @return 结果
     */
    @DeleteMapping("/{itemId}")
    public Result<?> unlike(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean success = likeService.unlike(userId, itemId);
        return success ? Result.success() : Result.error("取消点赞失败");
    }

    /**
     * 查询点赞状态
     *
     * @param itemId 物品ID
     * @return 点赞状态
     */
    @GetMapping("/status/{itemId}")
    public Result<Boolean> status(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean isLiked = likeService.isLiked(userId, itemId);
        return Result.success(isLiked);
    }

    /**
     * 查询物品点赞数
     *
     * @param itemId 物品ID
     * @return 点赞数
     */
    @GetMapping("/count/{itemId}")
    public Result<Long> count(@PathVariable Long itemId) {
        long count = likeService.getLikeCount(itemId);
        return Result.success(count);
    }

    /**
     * 批量查询物品点赞状态和点赞数
     *
     * @param itemIds 物品ID列表
     * @return 点赞状态和点赞数
     */
    @GetMapping("/batch")
    public Result<Map<Long, Map<String, Object>>> batchStatus(@RequestParam List<Long> itemIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        Long userId = SecurityUtils.getUserId();

        for (Long itemId : itemIds) {
            Map<String, Object> itemResult = new HashMap<>();
            itemResult.put("isLiked", likeService.isLiked(userId, itemId));
            itemResult.put("count", likeService.getLikeCount(itemId));
            result.put(itemId, itemResult);
        }

        return Result.success(result);
    }

    /**
     * 获取用户点赞的物品ID列表
     *
     * @return 物品ID列表
     */
    @GetMapping("/user/items")
    public Result<List<Long>> userLikedItems() {
        Long userId = SecurityUtils.getUserId();
        List<Long> itemIds = likeService.getUserLikedItemIds(userId);
        return Result.success(itemIds);
    }
    
    /**
     * 管理员分页获取所有点赞数据
     * 
     * @param pageDTO 分页参数
     * @return 点赞数据列表
     */
    @GetMapping("/admin/page")
    public Result<Page<Like>> adminLikePage(PageDTO pageDTO) {
        Page<Like> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<Like> resultPage = likeService.page(page);
        return Result.success(resultPage);
    }
} 