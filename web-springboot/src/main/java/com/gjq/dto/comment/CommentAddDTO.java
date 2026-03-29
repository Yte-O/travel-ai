package com.gjq.dto.comment;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 评论添加DTO
 */
@Data
public class CommentAddDTO {
    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID（为null表示是顶级评论，否则表示回复）
     */
    private Long parentId;
    
    /**
     * 被回复的评论ID
     */
    private Long replyToCommentId;
    
    /**
     * 被回复的用户ID
     */
    private Long replyToUserId;
} 