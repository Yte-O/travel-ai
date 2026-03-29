// 聊天模型定义
export interface ChatModel {
  key: string;
  name: string;
}

// 聊天角色定义
export type ChatRole = 'user' | 'assistant';

// 聊天消息定义
export interface ChatMessage {
  id?: number;
  sessionId?: number;
  role: ChatRole;
  content: string;
  model?: string;
  extraData?: string; // JSON 格式的额外数据
  messageTime?: string;
}

// 聊天会话定义
export interface ChatSession {
  id: number;
  userId: number;
  sessionName: string;
  createTime: string;
  updateTime: string;
  latestMessage?: string; // 最新消息的预览
}

// 创建会话请求参数
export interface ChatSessionCreateDTO {
  sessionName: string;
}

// 更新会话请求参数
export interface ChatSessionUpdateDTO {
  id: number;
  sessionName: string;
}

// 发送消息请求参数
export interface ChatMessageSendDTO {
  sessionId: number;
  content: string;
  model: string;
  extraData?: string;
}

// 查询消息请求参数
export interface ChatMessageQueryDTO {
  sessionId: number;
} 