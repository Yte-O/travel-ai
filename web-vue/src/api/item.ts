import { request } from './request'
import type { CategoryAddDTO, CategoryQueryDTO, CategoryUpdateDTO, CategoryVO, ItemAddDTO, ItemQueryDTO, ItemUpdateDTO, ItemVO } from '@/types/item'
import type { PageVO } from '@/types/common'

// 景点相关API
export const itemApi = {
  /**
   * 添加景点
   * @param data 景点添加DTO
   */
  add: (data: ItemAddDTO) => {
    return request.post<number>('/item', data)
  },

  /**
   * 更新景点
   * @param data 景点更新DTO
   */
  update: (data: ItemUpdateDTO) => {
    return request.put('/item', data)
  },

  /**
   * 删除景点
   * @param id 景点ID
   */
  delete: (id: number) => {
    return request.delete(`/item/${id}`)
  },

  /**
   * 获取景点详情
   * @param id 景点ID
   */
  getById: (id: number) => {
    return request.get<ItemVO>(`/item/${id}`)
  },

  /**
   * 分页查询景点
   * @param params 查询参数
   */
  page: (params: ItemQueryDTO) => {
    return request.get<PageVO<ItemVO>>('/item/page', { params })
  },

  /**
   * 根据类别ID获取景点列表
   * @param categoryId 类别ID
   */
  listByCategoryId: (categoryId: number) => {
    return request.get<ItemVO[]>(`/item/list/category/${categoryId}`)
  },

  /**
   * 根据标签获取景点列表
   * @param tag 标签
   */
  listByTag: (tag: string) => {
    return request.get<ItemVO[]>(`/item/list/tag/${tag}`)
  }
} 