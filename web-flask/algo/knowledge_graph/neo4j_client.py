"""
Neo4j图数据库客户端
"""
from neo4j import GraphDatabase
import logging
from typing import List, Dict, Optional
from algo.knowledge_graph.config import NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD

logger = logging.getLogger(__name__)

class Neo4jClient:
    """Neo4j数据库客户端"""
    
    def __init__(self, uri: str = NEO4J_URI, user: str = NEO4J_USER, password: str = NEO4J_PASSWORD):
        """
        初始化Neo4j客户端
        
        Args:
            uri: Neo4j数据库连接地址
            user: 用户名
            password: 密码
        """
        self.driver = GraphDatabase.driver(uri, auth=(user, password))
        
    def close(self):
        """关闭数据库连接"""
        if self.driver:
            self.driver.close()
            
    def execute_query(self, query: str, parameters: Optional[Dict] = None) -> List[Dict]:
        """
        执行Cypher查询
        
        Args:
            query: Cypher查询语句
            parameters: 查询参数
            
        Returns:
            查询结果列表
        """
        with self.driver.session() as session:
            result = session.run(query, parameters or {})
            return [record.data() for record in result]
    
    def execute_query_raw(self, query: str, parameters: Optional[Dict] = None) -> List:
        """
        执行Cypher查询并返回原始记录
        
        Args:
            query: Cypher查询语句
            parameters: 查询参数
            
        Returns:
            原始记录列表
        """
        with self.driver.session() as session:
            result = session.run(query, parameters or {})
            return list(result)
    
    def clear_database(self):
        """清空数据库"""
        query = "MATCH (n) DETACH DELETE n"
        self.execute_query(query)
        logger.info("数据库已清空")
    
    def create_constraints(self):
        """创建约束和索引"""
        constraints = [
            "CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE",
            "CREATE CONSTRAINT category_id IF NOT EXISTS FOR (c:Category) REQUIRE c.id IS UNIQUE", 
            "CREATE CONSTRAINT item_id IF NOT EXISTS FOR (i:Item) REQUIRE i.id IS UNIQUE",
            "CREATE CONSTRAINT tag_id IF NOT EXISTS FOR (t:Tag) REQUIRE t.id IS UNIQUE",
            "CREATE INDEX user_username IF NOT EXISTS FOR (u:User) ON (u.username)",
            "CREATE INDEX item_title IF NOT EXISTS FOR (i:Item) ON (i.title)",
            "CREATE INDEX category_name IF NOT EXISTS FOR (c:Category) ON (c.name)",
            "CREATE INDEX tag_name IF NOT EXISTS FOR (t:Tag) ON (t.name)"
        ]
        
        for constraint in constraints:
            try:
                self.execute_query(constraint)
            except Exception as e:
                # 约束可能已存在，忽略错误
                logger.debug(f"约束创建失败（可能已存在）: {e}")
        
        logger.info("约束和索引创建完成")
    
    def create_user_nodes(self, users: List[Dict]):
        """创建用户节点"""
        query = """
        UNWIND $users AS user
        MERGE (u:User {id: user.id})
        SET u.username = user.username,
            u.realName = user.realName,
            u.phone = user.phone,
            u.email = user.email,
            u.role = user.role,
            u.status = user.status,
            u.avatarBucket = user.avatarBucket,
            u.avatarObjectKey = user.avatarObjectKey,
            u.createTime = user.createTime,
            u.updateTime = user.updateTime
        """
        self.execute_query(query, {"users": users})
        logger.info(f"创建了 {len(users)} 个用户节点")
    
    def create_category_nodes(self, categories: List[Dict]):
        """创建类别节点"""
        query = """
        UNWIND $categories AS category
        MERGE (c:Category {id: category.id})
        SET c.name = category.name,
            c.description = category.description,
            c.createTime = category.createTime,
            c.updateTime = category.updateTime
        """
        self.execute_query(query, {"categories": categories})
        logger.info(f"创建了 {len(categories)} 个类别节点")
    
    def create_item_nodes(self, items: List[Dict]):
        """创建物品节点"""
        query = """
        UNWIND $items AS item
        MERGE (i:Item {id: item.id})
        SET i.title = item.title,
            i.description = item.description,
            i.tags = item.tags,
            i.coverBucket = item.coverBucket,
            i.coverObjectKey = item.coverObjectKey,
            i.createTime = item.createTime,
            i.updateTime = item.updateTime
        """
        self.execute_query(query, {"items": items})
        logger.info(f"创建了 {len(items)} 个物品节点")
    
    def create_tag_nodes(self, tags: List[Dict]):
        """创建标签节点"""
        query = """
        UNWIND $tags AS tag
        MERGE (t:Tag {id: tag.id})
        SET t.name = tag.name,
            t.type = tag.type,
            t.createTime = datetime(),
            t.updateTime = datetime()
        """
        self.execute_query(query, {"tags": tags})
        logger.info(f"创建了 {len(tags)} 个标签节点")
    
    def create_relationships(self, relationships: List[Dict]):
        """创建关系"""
        # 用户创建物品关系
        user_create_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'CREATED'
        MERGE (u)-[:CREATED {createTime: rel.createTime}]->(i)
        """
        
        # 物品属于类别关系
        item_belongs_category_query = """
        UNWIND $relationships AS rel
        MATCH (i:Item {id: rel.itemId}), (c:Category {id: rel.categoryId})
        WHERE rel.type = 'BELONGS_TO'
        MERGE (i)-[:BELONGS_TO]->(c)
        """
        
        # 用户收藏物品关系
        user_favorite_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'FAVORITED'
        MERGE (u)-[:FAVORITED {createTime: rel.createTime}]->(i)
        """
        
        # 用户点赞物品关系
        user_like_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'LIKED'
        MERGE (u)-[:LIKED {createTime: rel.createTime}]->(i)
        """
        
        # 用户浏览物品关系
        user_view_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'VIEWED'
        MERGE (u)-[:VIEWED {createTime: rel.createTime, extraData: rel.extraData}]->(i)
        """
        
        # 用户购买物品关系
        user_purchase_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'PURCHASED'
        MERGE (u)-[:PURCHASED {createTime: rel.createTime, extraData: rel.extraData}]->(i)
        """
        
        # 物品拥有标签关系
        item_has_tag_query = """
        UNWIND $relationships AS rel
        MATCH (i:Item {id: rel.itemId}), (t:Tag {id: rel.tagId})
        WHERE rel.type = 'HAS_TAG'
        MERGE (i)-[:HAS_TAG {createTime: datetime()}]->(t)
        """
        
        queries = [
            user_create_item_query,
            item_belongs_category_query,
            user_favorite_item_query,
            user_like_item_query,
            user_view_item_query,
            user_purchase_item_query,
            item_has_tag_query
        ]
        
        for query in queries:
            self.execute_query(query, {"relationships": relationships})
        
        logger.info(f"创建了关系")
    
    def get_statistics(self) -> Dict[str, int]:
        """获取图数据库统计信息"""
        stats = {}
        
        # 节点统计
        node_queries = {
            "users": "MATCH (u:User) RETURN count(u) as count",
            "categories": "MATCH (c:Category) RETURN count(c) as count",
            "items": "MATCH (i:Item) RETURN count(i) as count",
            "tags": "MATCH (t:Tag) RETURN count(t) as count"
        }
        
        for key, query in node_queries.items():
            result = self.execute_query(query)
            stats[key] = result[0]["count"] if result else 0
        
        # 关系统计
        relationship_queries = {
            "created": "MATCH ()-[r:CREATED]->() RETURN count(r) as count",
            "belongs_to": "MATCH ()-[r:BELONGS_TO]->() RETURN count(r) as count",
            "favorited": "MATCH ()-[r:FAVORITED]->() RETURN count(r) as count",
            "liked": "MATCH ()-[r:LIKED]->() RETURN count(r) as count",
            "viewed": "MATCH ()-[r:VIEWED]->() RETURN count(r) as count",
            "purchased": "MATCH ()-[r:PURCHASED]->() RETURN count(r) as count",
            "has_tag": "MATCH ()-[r:HAS_TAG]->() RETURN count(r) as count"
        }
        
        for key, query in relationship_queries.items():
            result = self.execute_query(query)
            stats[key] = result[0]["count"] if result else 0
        
        return stats
    
    def search_related_items(self, keywords: List[str], limit: int = 10) -> List[Dict]:
        """
        基于关键词搜索相关景点
        
        Args:
            keywords: 搜索关键词列表
            limit: 返回结果数量限制
            
        Returns:
            匹配的景点列表
        """
        if not keywords:
            return []
        
        # 构建搜索条件，支持标题、描述、标签的模糊匹配
        keyword_conditions = []
        for i, keyword in enumerate(keywords):
            keyword_conditions.append(f"""
                i.title CONTAINS $keyword{i} OR 
                i.description CONTAINS $keyword{i} OR 
                EXISTS {{
                    MATCH (i)-[:HAS_TAG]->(t:Tag)
                    WHERE t.name CONTAINS $keyword{i}
                }} OR
                EXISTS {{
                    MATCH (i)-[:BELONGS_TO]->(c:Category)
                    WHERE c.name CONTAINS $keyword{i}
                }}
            """)
        
        query = f"""
        MATCH (i:Item)
        WHERE {' OR '.join([f'({condition})' for condition in keyword_conditions])}
        OPTIONAL MATCH (i)-[:BELONGS_TO]->(c:Category)
        OPTIONAL MATCH (i)-[:HAS_TAG]->(t:Tag)
        WITH i, c, COLLECT(DISTINCT t.name) as tags
        RETURN DISTINCT i.id as id, 
               i.title as title, 
               i.description as description,
               c.name as categoryName,
               tags,
               i.createTime as createTime
        ORDER BY i.createTime DESC
        LIMIT $limit
        """
        
        # 构建参数字典
        parameters = {"limit": limit}
        for i, keyword in enumerate(keywords):
            parameters[f"keyword{i}"] = keyword
        
        results = self.execute_query(query, parameters)
        
        # 格式化结果
        formatted_results = []
        for result in results:
            formatted_results.append({
                "id": result["id"],
                "title": result["title"],
                "description": result["description"],
                "categoryName": result["categoryName"],
                "tags": result["tags"],
                "createTime": result["createTime"],
                "source": "graph_search"
            })
        
        logger.info(f"基于关键词 {keywords} 搜索到 {len(formatted_results)} 个相关景点")
        return formatted_results

# 创建全局实例
neo4j_client = Neo4jClient() 