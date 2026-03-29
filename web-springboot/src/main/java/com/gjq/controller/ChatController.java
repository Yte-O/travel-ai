package com.gjq.controller;

import com.gjq.common.Result;
import com.gjq.dto.chat.ChatMessageDTO;
import com.gjq.dto.chat.ChatSessionDTO;
import com.gjq.service.ChatService;
import com.gjq.vo.chat.ChatMessageVO;
import com.gjq.vo.chat.ChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "聊天API", description = "聊天相关接口")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping("/sessions")
    @Operation(summary = "创建聊天会话")
    public Result<ChatSessionVO> createSession(@RequestBody ChatSessionDTO dto) {
        return Result.success(chatService.createSession(dto));
    }
    
    @GetMapping("/sessions")
    @Operation(summary = "获取用户的所有聊天会话")
    public Result<List<ChatSessionVO>> getUserSessions() {
        return Result.success(chatService.getUserSessions());
    }
    
    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取聊天会话详情")
    public Result<ChatSessionVO> getSessionById(@PathVariable("id") Long sessionId) {
        return Result.success(chatService.getSessionById(sessionId));
    }
    
    @PutMapping("/sessions/{id}")
    @Operation(summary = "更新聊天会话")
    public Result<ChatSessionVO> updateSession(@PathVariable("id") Long sessionId, 
                                                @RequestBody ChatSessionDTO dto) {
        return Result.success(chatService.updateSession(sessionId, dto));
    }
    
    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除聊天会话")
    public Result<Void> deleteSession(@PathVariable("id") Long sessionId) {
        chatService.deleteSession(sessionId);
        return Result.success();
    }
    
    @GetMapping("/messages")
    @Operation(summary = "获取会话中的所有消息")
    public Result<List<ChatMessageVO>> getSessionMessages(@RequestParam("sessionId") Long sessionId) {
        return Result.success(chatService.getSessionMessages(sessionId));
    }
    
    @PostMapping("/messages")
    @Operation(summary = "发送消息")
    public Result<ChatMessageVO> sendMessage(@RequestBody ChatMessageDTO dto) {
        return Result.success(chatService.sendMessage(dto));
    }
    
    @GetMapping("/messages/{id}")
    @Operation(summary = "获取消息详情")
    public Result<ChatMessageVO> getMessageById(@PathVariable("id") Long messageId) {
        return Result.success(chatService.getMessageById(messageId));
    }
    
    @DeleteMapping("/messages/{id}")
    @Operation(summary = "删除消息")
    public Result<Void> deleteMessage(@PathVariable("id") Long messageId) {
        chatService.deleteMessage(messageId);
        return Result.success();
    }
    
    @DeleteMapping("/sessions/{id}/messages")
    @Operation(summary = "清除会话所有聊天记录")
    public Result<Void> clearSessionMessages(@PathVariable("id") Long sessionId) {
        chatService.clearSessionMessages(sessionId);
        return Result.success();
    }
    
    @PutMapping("/messages/{id}/content")
    @Operation(summary = "更新消息内容")
    public Result<Void> updateMessageContent(@PathVariable("id") Long messageId, @RequestBody Map<String, String> params) {
        String content = params.get("content");
        chatService.updateMessageContent(messageId, content);
        return Result.success();
    }
} 