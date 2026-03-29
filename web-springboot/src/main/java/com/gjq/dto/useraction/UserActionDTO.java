package com.gjq.dto.useraction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 用户行为DTO
 */
@Data
@Schema(description = "用户行为DTO")
public class UserActionDTO {
    
    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    @Schema(description = "物品ID", required = true)
    private Long itemId;
    
    /**
     * 行为类型(0:浏览 1:购买)
     */
    @NotNull(message = "行为类型不能为空")
    @Schema(description = "行为类型：0-浏览，1-购买", required = true)
    private Integer actionType;
    
    /**
     * 扩展数据(JSON格式)
     */
    @Schema(description = "扩展数据(JSON格式)，用于存储不同场景的特定数据")
    private String extraData;
} 