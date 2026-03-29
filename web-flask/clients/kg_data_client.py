"""
知识图谱数据同步客户端 - 基于springboot_client封装
"""
from clients.springboot_client import springboot_client
from typing import List, Dict, Any
import logging

logger = logging.getLogger(__name__)

class KGDataClient:
    """知识图谱数据客户端"""
    
    def __init__(self):
        self.client = springboot_client
    
    def get_all_users(self) -> List[Dict]:
        """获取所有用户数据"""
        try:
            all_users = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/user/page", params)
                
                if not result or not result.get("records"):
                    break
                
                users = result["records"]
                all_users.extend(users)
                
                # 检查是否还有更多数据
                if len(users) < size:
                    break
                
                current += 1
            
            logger.info(f"获取到 {len(all_users)} 个用户")
            return all_users
            
        except Exception as e:
            logger.error(f"获取用户数据失败: {str(e)}")
            return []
    
    def get_all_categories(self) -> List[Dict]:
        """获取所有类别数据"""
        try:
            all_categories = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/category/page", params)
                
                if not result or not result.get("records"):
                    break
                
                categories = result["records"]
                all_categories.extend(categories)
                
                # 检查是否还有更多数据
                if len(categories) < size:
                    break
                
                current += 1
            
            logger.info(f"获取到 {len(all_categories)} 个类别")
            return all_categories
            
        except Exception as e:
            logger.error(f"获取类别数据失败: {str(e)}")
            return []
    
    def get_all_items(self) -> List[Dict]:
        """获取所有物品数据"""
        try:
            all_items = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/item/page", params)
                
                if not result or not result.get("records"):
                    break
                
                items = result["records"]
                all_items.extend(items)
                
                # 检查是否还有更多数据
                if len(items) < size:
                    break
                
                current += 1
            
            logger.info(f"获取到 {len(all_items)} 个物品")
            return all_items
            
        except Exception as e:
            logger.error(f"获取物品数据失败: {str(e)}")
            return []
    
    def get_all_user_actions(self) -> List[Dict]:
        """获取所有用户行为数据"""
        try:
            all_actions = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/user-action/admin/page", params)
                
                if not result or not result.get("records"):
                    break
                
                actions = result["records"]
                all_actions.extend(actions)
                
                # 检查是否还有更多数据
                if len(actions) < size:
                    break
                
                current += 1
            
            logger.info(f"获取到 {len(all_actions)} 个用户行为")
            return all_actions
            
        except Exception as e:
            logger.error(f"获取用户行为数据失败: {str(e)}")
            return []
    
    def get_all_favorites(self) -> List[Dict]:
        """获取所有收藏数据"""
        try:
            all_favorites = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/favorite/admin/page", params)
                
                if not result or not result.get("records"):
                    break
                
                favorites = result["records"]
                all_favorites.extend(favorites)
                
                # 检查是否还有更多数据
                if len(favorites) < size:
                    break
                
                current += 1
            
            logger.info(f"获取到 {len(all_favorites)} 个收藏")
            return all_favorites
            
        except Exception as e:
            logger.error(f"获取收藏数据失败: {str(e)}")
            return []
    
    def get_all_likes(self) -> List[Dict]:
        """获取所有点赞数据"""
        try:
            all_likes = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/like/admin/page", params)
                
                if not result or not result.get("records"):
                    break
                
                likes = result["records"]
                all_likes.extend(likes)
                
                # 检查是否还有更多数据
                if len(likes) < size:
                    break
                
                current += 1
            
            logger.info(f"获取到 {len(all_likes)} 个点赞")
            return all_likes
            
        except Exception as e:
            logger.error(f"获取点赞数据失败: {str(e)}")
            return []

# 创建全局实例
kg_data_client = KGDataClient() 