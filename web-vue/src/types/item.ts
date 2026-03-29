// 景点相关的类型定义

// 类别实体
export interface Category {
  id: number;
  name: string;
  iconUrl: string;
  iconObjectKey: string;
  iconBucket: string;
  description: string;
  createTime: string;
  updateTime: string;
}

// 类别添加DTO
export interface CategoryAddDTO {
  name: string;
  iconObjectKey: string;
  iconBucket: string;
  description: string;
}

// 类别更新DTO
export interface CategoryUpdateDTO {
  id: number;
  name?: string;
  iconObjectKey?: string;
  iconBucket?: string;
  description?: string;
}

// 类别查询DTO
export interface CategoryQueryDTO {
  name?: string;
  current?: number;
  size?: number;
}

// 类别VO
export interface CategoryVO {
  id: number;
  name: string;
  iconUrl: string;
  iconObjectKey?: string;
  iconBucket?: string;
  description: string;
  createTime: string;
  updateTime: string;
}

// 景点实体
export interface Item {
  id: number;
  title: string;
  description: string;
  coverUrl: string;
  coverObjectKey: string;
  coverBucket: string;
  fileUrl: string;
  fileObjectKey: string;
  fileBucket: string;
  tags: string;
  extraData?: string;
  categoryId: number;
  createTime: string;
  updateTime: string;
}

// 景点添加DTO
export interface ItemAddDTO {
  title: string;
  description: string;
  coverObjectKey: string;
  coverBucket: string;
  fileObjectKey: string;
  fileBucket: string;
  tags: string;
  extraData?: string;
  categoryId: number;
}

// 景点更新DTO
export interface ItemUpdateDTO {
  id: number;
  title?: string;
  description?: string;
  coverObjectKey?: string;
  coverBucket?: string;
  fileObjectKey?: string;
  fileBucket?: string;
  tags?: string;
  extraData?: string;
  categoryId?: number;
}

// 景点查询DTO
export interface ItemQueryDTO {
  title?: string;
  categoryId?: number;
  tag?: string;
  userRealName?: string;
  current?: number;
  size?: number;
}

// 景点VO
export interface ItemVO {
  id: number;
  title: string;
  description: string;
  coverUrl: string;
  coverObjectKey?: string;
  coverBucket?: string;
  fileUrl: string;
  fileObjectKey?: string;
  fileBucket?: string;
  tags: string[];
  extraData?: string;
  category: CategoryVO;
  userId: number;
  userRealName?: string;
  createTime: string;
  updateTime: string;
  // 统计数据(前端组织填充)
  favorites?: number;
  views?: number;
}

// 推荐系统返回的景点VO
export interface RecommendedItemVO extends Partial<ItemVO> {
  id: number;
  title: string;
  description: string;
  tags?: string[];
  // 推荐系统可能返回的额外字段
  categoryName?: string;
  score?: number;
  relationDiversity?: number;
  popularity?: number;
  // 历史交互相关字段
  interactionType?: string;  // 交互类型：VIEWED、PURCHASED、FAVORITED、LIKED
  interactionLabel?: string; // 交互描述：您浏览过、您购买过等
} 