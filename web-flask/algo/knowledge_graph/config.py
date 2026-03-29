"""
知识图谱配置
"""
import os

# Neo4j数据库配置
NEO4J_URI = os.getenv('NEO4J_URI', 'bolt://localhost:7687')
NEO4J_USER = os.getenv('NEO4J_USER', 'neo4j')
NEO4J_PASSWORD = os.getenv('NEO4J_PASSWORD', 'neo4j123')

# 图数据库节点标签
NODE_LABELS = {
    'USER': 'User',
    'CATEGORY': 'Category', 
    'ITEM': 'Item'
}

# 图数据库关系类型
RELATIONSHIP_TYPES = {
    'CREATED': 'CREATED',           # 用户创建物品
    'BELONGS_TO': 'BELONGS_TO',     # 物品属于类别
    'FAVORITED': 'FAVORITED',       # 用户收藏物品
    'LIKED': 'LIKED',               # 用户点赞物品
    'VIEWED': 'VIEWED',             # 用户浏览物品
    'PURCHASED': 'PURCHASED'        # 用户购买物品
} 