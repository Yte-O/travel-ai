package com.gjq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjq.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天会话Mapper接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    
    /**
     * 查询用户的所有聊天会话，并附带最新的一条消息作为预览
     * 
     * @param userId 用户ID
     * @return 聊天会话列表
     */
    @Select("SELECT s.*, m.content as latest_message FROM chat_session s " +
            "LEFT JOIN (SELECT cm.session_id, cm.content " +
            "           FROM chat_message cm " +
            "           INNER JOIN (SELECT session_id, MAX(message_time) as max_time " +
            "                       FROM chat_message " +
            "                       GROUP BY session_id) latest " +
            "           ON cm.session_id = latest.session_id AND cm.message_time = latest.max_time) m " +
            "ON s.id = m.session_id " +
            "WHERE s.user_id = #{userId} " +
            "ORDER BY s.update_time DESC")
    List<ChatSession> selectSessionsWithLatestMessage(@Param("userId") Long userId);
} 