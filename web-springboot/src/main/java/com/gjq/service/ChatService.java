package com.gjq.service;

import com.gjq.dto.chat.ChatMessageDTO;
import com.gjq.dto.chat.ChatSessionDTO;
import com.gjq.vo.chat.ChatMessageVO;
import com.gjq.vo.chat.ChatSessionVO;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 创建聊天会话
     * 
     * @param dto 聊天会话信息
     * @return 创建的会话
     */
    ChatSessionVO createSession(ChatSessionDTO dto);
    
    /**
     * 获取用户的所有聊天会话
     * 
     * @return 聊天会话列表
     */
    List<ChatSessionVO> getUserSessions();
    
    /**
     * 获取聊天会话详情
     * 
     * @param sessionId 会话ID
     * @return 会话详情
     */
    ChatSessionVO getSessionById(Long sessionId);
    
    /**
     * 更新聊天会话
     * 
     * @param sessionId 会话ID
     * @param dto 会话信息
     * @return 更新后的会话
     */
    ChatSessionVO updateSession(Long sessionId, ChatSessionDTO dto);
    
    /**
     * 删除聊天会话
     * 
     * @param sessionId 会话ID
     */
    void deleteSession(Long sessionId);
    
    /**
     * 获取会话中的所有消息
     * 
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessageVO> getSessionMessages(Long sessionId);
    
    /**
     * 发送消息
     * 
     * @param dto 消息信息
     * @return 发送的消息
     */
    ChatMessageVO sendMessage(ChatMessageDTO dto);
    
    /**
     * 获取消息详情
     * 
     * @param messageId 消息ID
     * @return 消息详情
     */
    ChatMessageVO getMessageById(Long messageId);
    
    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     */
    void deleteMessage(Long messageId);
    
    /**
     * 清除会话所有聊天记录
     * 
     * @param sessionId 会话ID
     */
    void clearSessionMessages(Long sessionId);
    
    /**
     * 更新消息内容
     * 
     * @param messageId 消息ID
     * @param content 更新的内容
     */
    void updateMessageContent(Long messageId, String content);
} 