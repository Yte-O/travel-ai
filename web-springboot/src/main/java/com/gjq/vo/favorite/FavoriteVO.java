package com.gjq.vo.favorite;

import com.gjq.vo.item.ItemVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏VO
 */
@Data
public class FavoriteVO {
    /**
     * 收藏ID
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
     * 物品信息
     */
    private ItemVO item;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 