package com.gjq.dto.comment;

import com.gjq.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentQueryDTO extends PageDTO {
    /**
     * 物品ID
     */
    private Long itemId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否只查询顶级评论
     */
    private Boolean onlyParent;
} 