import { request } from '@/api/request'

/**
 * 用户行为数据接口
 */
export interface UserActionData {
  itemId: number;
  actionType: number; // 0:浏览 1:预约
  extraData?: string;
}

/**
 * 用户行为查询参数接口
 */
export interface UserActionQueryParams {
  current: number;
  size: number;
  userId?: number;
  itemId?: number;
  username?: string;
  itemTitle?: string;
  actionType?: number;
}

/**
 * 用户行为返回数据接口
 */
export interface UserActionResponse {
  records: any[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 添加用户行为记录（浏览/预约）
 * @param data 用户行为数据
 * @returns Promise
 */
export function addUserAction(data: UserActionData): Promise<boolean> {
  return request.post('/user-action', data)
}

/**
 * 分页查询当前用户的行为记录
 * @param params 查询参数
 * @returns Promise
 */
export function pageMyActions(params: UserActionQueryParams): Promise<UserActionResponse> {
  return request.get('/user-action/page', { params })
}

/**
 * 分页查询所有用户的行为记录（管理员接口）
 * @param params 查询参数
 * @returns Promise
 */
export function pageAllActions(params: UserActionQueryParams): Promise<UserActionResponse> {
  return request.get('/user-action/admin/page', { params })
}

/**
 * 批量删除用户行为记录（管理员接口）
 * @param ids 记录ID列表
 * @returns Promise
 */
export function batchDeleteActions(ids: number[]): Promise<boolean> {
  return request.delete('/user-action/batch', { data: ids })
}

/**
 * 批量删除当前用户的行为记录
 * @param ids 记录ID列表
 * @returns Promise
 */
export function batchDeleteMyActions(ids: number[]): Promise<boolean> {
  return request.delete('/user-action/my/batch', { data: ids })
}

/**
 * 获取景点浏览数
 * @param itemId 景点ID
 * @returns Promise
 */
export function getItemViewCount(itemId: number): Promise<number> {
  return request.get(`/user-action/view/count/${itemId}`)
}

/**
 * 获取景点预约数
 * @param itemId 景点ID
 * @returns Promise
 */
export function getItemReservationCount(itemId: number): Promise<number> {
  return request.get(`/user-action/reservation/count/${itemId}`)
} 