"""
基于知识图谱的推荐算法
"""
import logging
from typing import List, Dict
from algo.knowledge_graph.neo4j_client import neo4j_client

logger = logging.getLogger(__name__)

class KGRecommender:
    """基于知识图谱的推荐算法"""
    
    def __init__(self):
        self.neo4j_client = neo4j_client
    
    def recommend_by_user_id(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        基于用户ID推荐景点（协同过滤）
        
        Args:
            user_id: 用户ID
            limit: 推荐结果数量限制
            
        Returns:
            推荐景点列表
        """
        try:
            # 基于协同过滤的推荐查询
            # 查找与当前用户有相似行为的用户喜欢的景点
            query = """
            MATCH (u:User {id: $userId})-[r1:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)
            MATCH (i1)<-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]-(u2:User)
            WHERE u.id <> u2.id
            MATCH (u2)-[r3:VIEWED|PURCHASED|FAVORITED|LIKED]->(i2:Item)
            MATCH (creator:User)-[:CREATED]->(i2)
            // 获取景点的标签信息
            OPTIONAL MATCH (i2)-[:HAS_TAG]->(tag:Tag)
            WITH i2, creator, COUNT(DISTINCT u2) AS userCount, 
                 COLLECT(DISTINCT TYPE(r3)) AS relationTypes,
                 COLLECT(DISTINCT tag.name) AS tagNames
            RETURN i2.id AS id, i2.title AS title, i2.description AS description, 
                   i2.tags AS tags, i2.coverBucket AS coverBucket, i2.coverObjectKey AS coverObjectKey,
                   creator.username AS username, creator.realName AS userRealName,
                   userCount AS score, size(relationTypes) AS relationDiversity,
                   tagNames
            ORDER BY score DESC, relationDiversity DESC
            LIMIT $limit
            """
            
            # 执行协同过滤查询
            result = self.neo4j_client.execute_query(query, {"userId": user_id, "limit": limit})
            
            # 处理标签字段，合并原始标签和Tag节点标签
            for item in result:
                original_tags = []
                if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                    original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
                
                # 合并原始标签和Tag节点标签
                tag_names = item.get("tagNames", [])
                all_tags = list(set(original_tags + [tag for tag in tag_names if tag]))
                item["tags"] = all_tags
                
                # 清理tagNames字段
                if "tagNames" in item:
                    del item["tagNames"]
            
            logger.info(f"为用户 {user_id} 生成了 {len(result)} 个协同过滤推荐")
            return result
            
        except Exception as e:
            logger.error(f"为用户 {user_id} 生成协同过滤推荐失败: {str(e)}")
            return []
    
    def recommend_by_content(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        基于用户喜好的内容推荐（基于类别和标签）
        
        Args:
            user_id: 用户ID
            limit: 推荐结果数量限制
            
        Returns:
            推荐景点列表
        """
        try:
            # 基于内容的推荐查询，利用类别和标签信息
            content_query = """
            // 获取用户喜欢的物品所属的类别和标签
            MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)
            MATCH (i1)-[:BELONGS_TO]->(c:Category)
            OPTIONAL MATCH (i1)-[:HAS_TAG]->(t:Tag)
            
            // 基于类别找相似物品
            MATCH (i2:Item)-[:BELONGS_TO]->(c)
            WHERE i1.id <> i2.id
            MATCH (creator:User)-[:CREATED]->(i2)
            OPTIONAL MATCH (i2)-[:HAS_TAG]->(tag:Tag)
            
            // 计算相似度：类别匹配 + 标签匹配
            WITH i2, creator, c.name AS categoryName,
                 COUNT(DISTINCT c) AS categoryMatch,
                 SIZE([(i2)<-[:VIEWED|PURCHASED|FAVORITED|LIKED]-() | true]) AS popularity,
                 COLLECT(DISTINCT tag.name) AS tagNames
            
            // 基于标签的额外匹配
            OPTIONAL MATCH (u:User {id: $userId})-[:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)-[:HAS_TAG]->(ut:Tag)
            WITH i2, creator, categoryName, categoryMatch, popularity, tagNames,
                 COLLECT(DISTINCT ut.name) AS userTags
            
            // 计算标签匹配度
            WITH i2, creator, categoryName, categoryMatch, popularity, tagNames,
                 SIZE([tag IN tagNames WHERE tag IN userTags]) AS tagMatch
            
            // 计算综合得分
            WITH i2, creator, categoryName, popularity, tagNames,
                 (categoryMatch * 3.0 + tagMatch * 2.0 + popularity * 0.1) AS score
            
            RETURN DISTINCT i2.id AS id, i2.title AS title, i2.description AS description, 
                   i2.tags AS tags, i2.coverBucket AS coverBucket, i2.coverObjectKey AS coverObjectKey,
                   creator.username AS username, creator.realName AS userRealName,
                   categoryName, score, tagNames
            ORDER BY score DESC
            LIMIT $limit
            """
            
            # 执行基于内容的推荐查询
            result = self.neo4j_client.execute_query(content_query, {"userId": user_id, "limit": limit})
            
            # 处理标签字段，合并原始标签和Tag节点标签
            for item in result:
                original_tags = []
                if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                    original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
                
                # 合并原始标签和Tag节点标签
                tag_names = item.get("tagNames", [])
                all_tags = list(set(original_tags + [tag for tag in tag_names if tag]))
                item["tags"] = all_tags
                
                # 清理tagNames字段
                if "tagNames" in item:
                    del item["tagNames"]
            
            logger.info(f"为用户 {user_id} 生成了 {len(result)} 个基于内容的推荐")
            return result
            
        except Exception as e:
            logger.error(f"为用户 {user_id} 生成基于内容的推荐失败: {str(e)}")
            return []

# 创建全局实例
kg_recommender = KGRecommender()