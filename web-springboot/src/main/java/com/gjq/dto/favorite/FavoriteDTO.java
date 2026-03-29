package com.gjq.dto.favorite;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 收藏DTO
 */
@Data
public class FavoriteDTO {
    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
} 