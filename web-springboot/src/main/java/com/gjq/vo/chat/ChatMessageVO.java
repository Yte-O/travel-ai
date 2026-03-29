package com.gjq.vo.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息视图对象
 */
@Data
public class ChatMessageVO {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 角色（user/assistant）
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 使用的模型名称
     */
    private String model;
    
    /**
     * 额外数据，JSON格式，用于存储文件信息等
     */
    private String extraData;
    
    /**
     * 消息时间
     */
    private LocalDateTime messageTime;
} 