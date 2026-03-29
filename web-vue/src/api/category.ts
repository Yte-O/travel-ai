import { request } from './request'
import { fileRequest } from './file_request'
import type { CategoryAddDTO, CategoryQueryDTO, CategoryUpdateDTO, CategoryVO, ItemAddDTO, ItemQueryDTO, ItemUpdateDTO, ItemVO } from '@/types/item'
import type { PageVO } from '@/types/common'

// 类别相关API
export const categoryApi = {
  /**
   * 添加类别
   * @param data 类别添加DTO
   */
  add: (data: CategoryAddDTO) => {
    return request.post<number>('/category', data)
  },

  /**
   * 更新类别
   * @param data 类别更新DTO
   */
  update: (data: CategoryUpdateDTO) => {
    return request.put('/category', data)
  },

  /**
   * 删除类别
   * @param id 类别ID
   */
  delete: (id: number) => {
    return request.delete(`/category/${id}`)
  },

  /**
   * 获取类别详情
   * @param id 类别ID
   */
  getById: (id: number) => {
    return request.get<CategoryVO>(`/category/${id}`)
  },

  /**
   * 获取所有类别
   */
  list: () => {
    return request.get<CategoryVO[]>('/category/list')
  },

  /**
   * 分页查询类别
   * @param params 查询参数
   */
  page: (params: CategoryQueryDTO) => {
    return request.get<PageVO<CategoryVO>>('/category/page', { params })
  }
}