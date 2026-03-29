import type { UserInfo } from './user'

/**
 * 评论对象类型
 */
export interface CommentVO {
  id: number
  userId: number
  userInfo: UserInfo
  itemId: number
  content: string
  parentId?: number
  replyToCommentId?: number
  replyToUserId?: number
  replyToUserInfo?: UserInfo
  replies?: CommentVO[]
  createTime: string
  updateTime: string
}

/**
 * 评论添加DTO
 */
export interface CommentAddDTO {
  itemId: number
  content: string
  parentId?: number
  replyToCommentId?: number
  replyToUserId?: number
}

/**
 * 评论查询DTO
 */
export interface CommentQueryDTO {
  itemId?: number
  userId?: number
  onlyParent?: boolean
  pageNo?: number
  pageSize?: number
} 