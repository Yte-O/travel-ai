package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.chat.ChatMessageDTO;
import com.gjq.dto.chat.ChatSessionDTO;
import com.gjq.entity.ChatMessage;
import com.gjq.entity.ChatSession;
import com.gjq.mapper.ChatMessageMapper;
import com.gjq.mapper.ChatSessionMapper;
import com.gjq.service.ChatService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.chat.ChatMessageVO;
import com.gjq.vo.chat.ChatSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    /**
     * 创建聊天会话
     */
    @Override
    @Transactional
    public ChatSessionVO createSession(ChatSessionDTO dto) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionName(dto.getSessionName());
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        
        chatSessionMapper.insert(session);
        
        return convertToSessionVO(session);
    }
    
    /**
     * 获取用户的所有聊天会话
     */
    @Override
    public List<ChatSessionVO> getUserSessions() {
        Long userId = SecurityUtils.getUserId();
        
        List<ChatSession> sessions = chatSessionMapper.selectSessionsWithLatestMessage(userId);
        
        // 使用Map进行去重，以会话ID为key
        Map<Long, ChatSession> sessionMap = new HashMap<>();
        for (ChatSession session : sessions) {
            sessionMap.put(session.getId(), session);
        }
        
        // 转换为有序列表
        return sessionMap.values().stream()
                .map(this::convertToSessionVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取聊天会话详情
     */
    @Override
    public ChatSessionVO getSessionById(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        // 检查权限
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权访问该会话");
        }
        
        return convertToSessionVO(session);
    }
    
    /**
     * 更新聊天会话
     */
    @Override
    @Transactional
    public ChatSessionVO updateSession(Long sessionId, ChatSessionDTO dto) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        // 检查权限
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权修改该会话");
        }
        
        session.setSessionName(dto.getSessionName());
        session.setUpdateTime(LocalDateTime.now());
        
        chatSessionMapper.updateById(session);
        
        return convertToSessionVO(session);
    }
    
    /**
     * 删除聊天会话
     */
    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        // 检查权限
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权删除该会话");
        }
        
        // 删除会话及其关联的所有消息
        chatSessionMapper.deleteById(sessionId);
        
        LambdaQueryWrapper<ChatMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(wrapper);
    }
    
    /**
     * 获取会话中的所有消息
     */
    @Override
    public List<ChatMessageVO> getSessionMessages(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        // 验证会话存在且用户有权限访问
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权访问该会话");
        }
        
        List<ChatMessage> messages = chatMessageMapper.selectMessagesBySessionId(sessionId);
        
        return messages.stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 发送消息
     */
    @Override
    @Transactional
    public ChatMessageVO sendMessage(ChatMessageDTO dto) {
        Long userId = SecurityUtils.getUserId();
        
        // 验证会话存在且用户有权限访问
        ChatSession session = chatSessionMapper.selectById(dto.getSessionId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权访问该会话");
        }
        
        // 创建用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setModel(dto.getModel());
        userMessage.setExtraData(dto.getExtraData());
        userMessage.setMessageTime(LocalDateTime.now());
        
        chatMessageMapper.insert(userMessage);
        
        // 更新会话的最后更新时间
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
        
        // 创建AI助手回复消息（实际回复由前端通过调用算法服务获取）
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(dto.getSessionId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(""); // 内容由前端填充
        aiMessage.setModel(dto.getModel());
        aiMessage.setMessageTime(LocalDateTime.now());
        
        chatMessageMapper.insert(aiMessage);
        
        // 返回AI消息，而不是用户消息，这样前端可以直接获取AI消息ID
        return convertToMessageVO(aiMessage);
    }
    
    /**
     * 获取消息详情
     */
    @Override
    public ChatMessageVO getMessageById(Long messageId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 验证会话存在且用户有权限访问
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权访问该消息");
        }
        
        return convertToMessageVO(message);
    }
    
    /**
     * 删除消息
     */
    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 验证会话存在且用户有权限访问
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权删除该消息");
        }
        
        // 删除消息
        chatMessageMapper.deleteById(messageId);
    }
    
    /**
     * 清除会话所有聊天记录
     */
    @Override
    @Transactional
    public void clearSessionMessages(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        // 验证会话存在且用户有权限访问
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权访问该会话");
        }
        
        // 删除会话中的所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(wrapper);
    }
    
    /**
     * 更新消息内容
     */
    @Override
    public void updateMessageContent(Long messageId, String content) {
        Long userId = SecurityUtils.getUserId();
        
        // 获取消息
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 获取消息所属的会话
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        // 验证用户权限
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权修改该消息");
        }
        
        // 更新消息内容
        message.setContent(content);
        chatMessageMapper.updateById(message);
    }
    
    /**
     * 将实体对象转换为VO
     */
    private ChatSessionVO convertToSessionVO(ChatSession session) {
        if (session == null) {
            return null;
        }
        
        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(session, vo);
        
        return vo;
    }
    
    /**
     * 将实体对象转换为VO
     */
    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        
        return vo;
    }
} 