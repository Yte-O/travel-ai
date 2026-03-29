import { request } from './request'
import type { CommentAddDTO, CommentQueryDTO, CommentVO } from '@/types/comment'
import type { PageVO } from '@/types/common'

// 评论相关API
export const commentApi = {
  /**
   * 添加评论
   * @param data 评论数据
   * @returns 评论ID
   */
  add: (data: CommentAddDTO) => {
    return request.post<number>('/comment', data)
  },

  /**
   * 删除评论
   * @param commentId 评论ID
   * @returns 是否成功
   */
  delete: (commentId: number) => {
    return request.delete<boolean>(`/comment/${commentId}`)
  },

  /**
   * 获取评论详情
   * @param commentId 评论ID
   * @returns 评论详情
   */
  get: (commentId: number) => {
    return request.get<CommentVO>(`/comment/${commentId}`)
  },

  /**
   * 分页查询评论
   * @param params 查询参数
   * @returns 评论分页
   */
  page: (params: CommentQueryDTO) => {
    return request.get<PageVO<CommentVO>>('/comment/page', { params })
  },

  /**
   * 获取景点评论树
   * @param itemId 景点ID
   * @returns 评论树
   */
  getTree: (itemId: number) => {
    return request.get<CommentVO[]>(`/comment/tree/${itemId}`)
  }
} 