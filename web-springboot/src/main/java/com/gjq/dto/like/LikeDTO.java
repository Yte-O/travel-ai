package com.gjq.dto.like;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 点赞DTO
 */
@Data
public class LikeDTO {
    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
} 