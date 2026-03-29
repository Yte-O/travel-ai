import { request } from './request'
import type { FavoriteDTO, FavoriteVO } from '@/types/favorite'
import type { PageVO } from '@/types/common'

// 收藏相关API
export const favoriteApi = {
  /**
   * 添加收藏
   * @param data 收藏DTO
   */
  add: (data: FavoriteDTO) => {
    return request.post('/favorite', data)
  },

  /**
   * 取消收藏
   * @param itemId 景点ID
   */
  remove: (itemId: number) => {
    return request.delete(`/favorite/${itemId}`)
  },

  /**
   * 查询收藏状态
   * @param itemId 景点ID
   */
  status: (itemId: number) => {
    return request.get<boolean>(`/favorite/status/${itemId}`)
  },

  /**
   * 获取用户收藏的景点ID列表
   */
  getUserFavoriteItemIds: () => {
    return request.get<number[]>('/favorite/user/items')
  },

  /**
   * 分页获取用户收藏列表
   * @param current 当前页
   * @param size 每页大小
   */
  page: (current: number = 1, size: number = 10) => {
    return request.get<PageVO<FavoriteVO>>('/favorite/user/page', {
      params: { current, size }
    })
  },

  /**
   * 获取景点收藏数
   * @param itemId 景点ID
   */
  getItemFavoriteCount: (itemId: number) => {
    return request.get<number>(`/favorite/count/${itemId}`)
  }
} 