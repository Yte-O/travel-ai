// 收藏相关的类型定义
import type { ItemVO } from './item'

// 收藏实体
export interface Favorite {
  id: number;
  userId: number;
  itemId: number;
  createTime: string;
}

// 收藏DTO
export interface FavoriteDTO {
  itemId: number;
}

// 收藏状态响应
export interface FavoriteStatusResponse {
  isFavorite: boolean;
}

// 收藏VO
export interface FavoriteVO {
  id: number;
  userId: number;
  itemId: number;
  item: ItemVO;
  createTime: string;
} 