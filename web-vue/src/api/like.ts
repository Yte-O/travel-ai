import { request } from './request'
import type { LikeDTO, LikeStatusResponse, BatchLikeStatusResponse } from '@/types/like'

// 点赞相关API
export const likeApi = {
  /**
   * 点赞
   * @param data 点赞DTO
   */
  like: (data: LikeDTO) => {
    return request.post('/like', data)
  },

  /**
   * 取消点赞
   * @param itemId 景点ID
   */
  unlike: (itemId: number) => {
    return request.delete(`/like/${itemId}`)
  },

  /**
   * 查询点赞状态
   * @param itemId 景点ID
   */
  status: (itemId: number) => {
    return request.get<boolean>(`/like/status/${itemId}`)
  },

  /**
   * 查询景点点赞数
   * @param itemId 景点ID
   */
  count: (itemId: number) => {
    return request.get<number>(`/like/count/${itemId}`)
  },

  /**
   * 批量查询景点点赞状态和点赞数
   * @param itemIds 景点ID列表
   */
  batchStatus: (itemIds: number[]) => {
    return request.get<BatchLikeStatusResponse>('/like/batch', {
      params: { itemIds }
    })
  },

  /**
   * 获取用户点赞的景点ID列表
   */
  userLikedItems: () => {
    return request.get<number[]>('/like/user/items')
  }
} 