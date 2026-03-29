package com.gjq.vo.comment;

import com.gjq.vo.user.UserInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 */
@Data
public class CommentVO {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID
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
     * 被回复的用户信息
     */
    private UserInfo replyToUserInfo;

    /**
     * 回复列表
     */
    private List<CommentVO> replies;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 