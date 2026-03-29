import { algoRequest } from './algo_request'
import type { ItemVO, RecommendedItemVO } from '@/types/item'
import type { ChatMessage } from '@/types/chat'

// 推荐系统API
export const recommendationApi = {
  /**
   * 为用户推荐景点（基于协同过滤）
   * @param userId 用户ID
   * @param limit 推荐数量
   */
  getForUser: (userId: number, limit: number = 10) => {
    return algoRequest.get<ItemVO[]>(`/recommendation/user/${userId}`, { 
      params: { limit } 
    })
  },

  /**
   * 为用户推荐景点（基于内容）
   * @param userId 用户ID
   * @param limit 推荐数量
   */
  getContentForUser: (userId: number, limit: number = 10) => {
    return algoRequest.get<ItemVO[]>(`/recommendation/user/${userId}/content`, { 
      params: { limit } 
    })
  },

  /**
   * 基于聊天内容推荐景点
   * @param messages 聊天消息历史
   * @param userId 用户ID（可选）
   * @param model LLM模型名称（可选）
   * @param limit 推荐数量
   */
  getFromChat: (messages: ChatMessage[], userId?: number, model?: string, limit: number = 10) => {
    return algoRequest.post<{
      items: RecommendedItemVO[], 
      keywords: string[],
      message?: string,
      isHistoryItems?: boolean
    }>('/llm/recommend', { 
      messages,
      userId,
      model,
      limit
    })
  }
} 