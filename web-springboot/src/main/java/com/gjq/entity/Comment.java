package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
public class Comment {
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
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
     * 评论内容
     */
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

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 