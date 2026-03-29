package com.gjq.vo.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天会话视图对象
 */
@Data
public class ChatSessionVO {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话名称
     */
    private String sessionName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 最新消息预览
     */
    private String latestMessage;
} 