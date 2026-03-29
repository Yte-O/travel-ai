package com.gjq.dto.chat;

import lombok.Data;

/**
 * 聊天消息数据传输对象
 */
@Data
public class ChatMessageDTO {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
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
} 