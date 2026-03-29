package com.gjq.vo.like;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞VO
 */
@Data
public class LikeVO {
    /**
     * 点赞ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 