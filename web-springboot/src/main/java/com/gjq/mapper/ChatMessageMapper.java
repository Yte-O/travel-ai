package com.gjq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjq.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 获取会话中的所有消息
     * 
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY message_time ASC")
    List<ChatMessage> selectMessagesBySessionId(@Param("sessionId") Long sessionId);
} 