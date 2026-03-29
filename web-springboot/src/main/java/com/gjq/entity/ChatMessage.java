package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
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