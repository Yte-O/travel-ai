"""
数据同步服务 - 从MySQL同步数据到Neo4j
"""
import logging
from typing import List, Dict, Any
from clients.kg_data_client import kg_data_client
from algo.knowledge_graph.neo4j_client import neo4j_client
from algo.knowledge_graph.tag_extractor import tag_extractor_service

logger = logging.getLogger(__name__)

class DataSyncService:
    """数据同步服务"""
    
    def __init__(self):
        self.data_client = kg_data_client
        self.neo4j_client = neo4j_client
    
    def sync_all_data(self) -> Dict[str, Any]:
        """
        同步所有数据到图数据库
        
        Returns:
            同步结果统计
        """
        try:
            logger.info("开始同步数据到图数据库")
            
            # 1. 清空图数据库
            self.neo4j_client.clear_database()
            
            # 2. 创建约束和索引
            self.neo4j_client.create_constraints()
            
            # 3. 获取并同步用户数据
            users = self.data_client.get_all_users()
            if users:
                self.neo4j_client.create_user_nodes(users)
            
            # 4. 获取并同步类别数据
            categories = self.data_client.get_all_categories()
            if categories:
                self.neo4j_client.create_category_nodes(categories)
            
            # 5. 获取并同步物品数据
            items = self.data_client.get_all_items()
            if items:
                self.neo4j_client.create_item_nodes(items)
            
            # 6. 创建标签和关系
            tags, tag_relationships = self._build_tags_and_relationships(items, categories)
            if tags:
                self.neo4j_client.create_tag_nodes(tags)
            
            # 7. 创建关系
            relationships = self._build_relationships(users, categories, items)
            if tag_relationships:
                relationships.extend(tag_relationships)
            if relationships:
                self.neo4j_client.create_relationships(relationships)
            
            # 8. 获取统计信息
            stats = self.neo4j_client.get_statistics()
            
            logger.info("数据同步完成")
            return {
                "success": True,
                "message": f"同步完成！用户: {stats.get('users', 0)}, 类别: {stats.get('categories', 0)}, 物品: {stats.get('items', 0)}, 标签: {stats.get('tags', 0)}",
                "statistics": stats
            }
            
        except Exception as e:
            logger.error(f"数据同步失败: {str(e)}")
            return {
                "success": False,
                "message": f"同步失败: {str(e)}",
                "statistics": {}
            }
    
    def _build_relationships(self, users: List[Dict], categories: List[Dict], items: List[Dict]) -> List[Dict]:
        """构建关系数据"""
        relationships = []
        
        try:
            # 1. 用户创建物品关系
            for item in items:
                relationships.append({
                    "type": "CREATED",
                    "userId": item.get("userId"),
                    "itemId": item.get("id"),
                    "createTime": item.get("createTime")
                })
            
            # 2. 物品属于类别关系
            for item in items:
                relationships.append({
                    "type": "BELONGS_TO",
                    "itemId": item.get("id"),
                    "categoryId": item.get("categoryId")
                })
            
            # 3. 获取用户行为关系
            user_actions = self.data_client.get_all_user_actions()
            for action in user_actions:
                action_type = action.get("actionType")
                if action_type == 0:  # 浏览
                    relationships.append({
                        "type": "VIEWED",
                        "userId": action.get("userId"),
                        "itemId": action.get("itemId"),
                        "createTime": action.get("createTime"),
                        "extraData": action.get("extraData")
                    })
                elif action_type == 1:  # 购买
                    relationships.append({
                        "type": "PURCHASED",
                        "userId": action.get("userId"),
                        "itemId": action.get("itemId"),
                        "createTime": action.get("createTime"),
                        "extraData": action.get("extraData")
                    })
            
            # 4. 获取收藏关系
            favorites = self.data_client.get_all_favorites()
            for favorite in favorites:
                relationships.append({
                    "type": "FAVORITED",
                    "userId": favorite.get("userId"),
                    "itemId": favorite.get("itemId"),
                    "createTime": favorite.get("createTime")
                })
            
            # 5. 获取点赞关系
            likes = self.data_client.get_all_likes()
            for like in likes:
                relationships.append({
                    "type": "LIKED",
                    "userId": like.get("userId"),
                    "itemId": like.get("itemId"),
                    "createTime": like.get("createTime")
                })
            
            logger.info(f"构建了 {len(relationships)} 个关系")
            return relationships
            
        except Exception as e:
            logger.error(f"构建关系失败: {str(e)}")
            return []
    
    def _build_tags_and_relationships(self, items: List[Dict], categories: List[Dict]) -> tuple[List[Dict], List[Dict]]:
        """构建标签数据和关系"""
        try:
            # 创建类别ID到类别信息的映射
            category_map = {cat['id']: cat for cat in categories}
            
            # 准备景点数据用于标签抽取
            attractions_for_extraction = []
            for item in items:
                category = category_map.get(item.get('categoryId'))
                if category:
                    attractions_for_extraction.append({
                        'id': item.get('id'),
                        'title': item.get('title', ''),
                        'description': item.get('description', ''),
                        'category_name': category.get('name', ''),
                        'category_description': category.get('description', ''),
                        'tags': item.get('tags', '')
                    })
            
            # 使用LLM批量抽取标签
            logger.info(f"开始为 {len(attractions_for_extraction)} 个景点抽取标签")
            tag_mappings = tag_extractor_service.extract_tags_batch(attractions_for_extraction)
            
            # 创建标签节点和关系数据
            tag_nodes, tag_relationships = tag_extractor_service.create_tag_nodes_data(tag_mappings)
            
            logger.info(f"成功创建 {len(tag_nodes)} 个标签节点和 {len(tag_relationships)} 个标签关系")
            return tag_nodes, tag_relationships
            
        except Exception as e:
            logger.error(f"构建标签和关系失败: {str(e)}")
            return [], []

# 创建全局实例
data_sync_service = DataSyncService() 