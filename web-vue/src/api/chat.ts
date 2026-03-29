import { request } from './request'
import { algoRequest } from './algo_request'
import type { ChatSession, ChatMessage, ChatSessionCreateDTO, ChatSessionUpdateDTO, ChatMessageSendDTO, ChatMessageQueryDTO, ChatModel } from '@/types/chat'

// 后端API - 会话相关
export const chatApi = {
  // 获取用户的所有会话
  getSessions() {
    return request.get<ChatSession[]>('/chat/sessions')
  },

  // 获取会话详情
  getSession(id: number) {
    return request.get<ChatSession>(`/chat/sessions/${id}`)
  },

  // 创建新会话
  createSession(data: ChatSessionCreateDTO) {
    return request.post<ChatSession>('/chat/sessions', data)
  },

  // 更新会话
  updateSession(data: ChatSessionUpdateDTO) {
    return request.put<ChatSession>(`/chat/sessions/${data.id}`, data)
  },

  // 删除会话
  deleteSession(id: number) {
    return request.delete(`/chat/sessions/${id}`)
  },

  // 获取会话的所有消息
  getMessages(params: ChatMessageQueryDTO) {
    return request.get<ChatMessage[]>('/chat/messages', { params })
  },

  // 发送消息并获取回复
  sendMessage(data: ChatMessageSendDTO) {
    return request.post<ChatMessage>('/chat/messages', data)
  },
  
  // 清除会话聊天记录
  clearSessionMessages(sessionId: number) {
    return request.delete(`/chat/sessions/${sessionId}/messages`)
  },
  
  // 删除单条消息
  deleteMessage(messageId: number) {
    return request.delete(`/chat/messages/${messageId}`)
  },
  
  // 更新消息内容
  updateMessageContent(messageId: number, content: string) {
    return request.put(`/chat/messages/${messageId}/content`, { content })
  }
}

// 算法端API - LLM相关
export const llmApi = {
  // 获取可用模型列表
  getModels() {
    return algoRequest.get<ChatModel[]>('/llm/models')
  },

  // 非流式聊天
  chat(model: string, messages: ChatMessage[]) {
    return algoRequest.post<string>('/llm/chat', {
      model,
      messages: messages.map(msg => ({
        role: msg.role,
        content: msg.content
      }))
    })
  },

  // GraphRAG增强聊天
  chatWithGraphRAG(model: string, messages: ChatMessage[]) {
    return algoRequest.post<{
      response: string;
      searchInfo: {
        keywords: string[];
        found_items: number;
        search_status: string;
        message: string;
      }
    }>('/llm/chat-with-graph-rag', {
      model,
      messages: messages.map(msg => ({
        role: msg.role,
        content: msg.content
      }))
    })
  }
} 