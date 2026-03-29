// 点赞相关的类型定义

// 点赞实体
export interface Like {
  id: number;
  userId: number;
  itemId: number;
  createTime: string;
}

// 点赞DTO
export interface LikeDTO {
  itemId: number;
}

// 点赞状态响应
export interface LikeStatusResponse {
  isLiked: boolean;
  count: number;
}

// 批量点赞状态响应
export interface BatchLikeStatusResponse {
  [itemId: number]: {
    isLiked: boolean;
    count: number;
  };
} 