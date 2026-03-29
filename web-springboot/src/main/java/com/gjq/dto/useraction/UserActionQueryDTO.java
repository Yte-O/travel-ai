package com.gjq.dto.useraction;

import com.gjq.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户行为查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户行为查询DTO")
public class UserActionQueryDTO extends PageDTO {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;
    
    /**
     * 景点ID
     */
    @Schema(description = "景点ID")
    private Long itemId;
    
    /**
     * 景点名称
     */
    @Schema(description = "景点名称")
    private String itemTitle;
    
    /**
     * 行为类型(0:浏览 1:预约)
     */
    @Schema(description = "行为类型：0-浏览，1-预约")
    private Integer actionType;
} 