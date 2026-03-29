"""
知识图谱路由
"""
from flask import Blueprint, request
from algo.knowledge_graph.data_sync import data_sync_service
from algo.knowledge_graph.neo4j_client import neo4j_client
from utils.response import success, error

# url前缀统一需要添加/api
knowledge_graph_bp = Blueprint('knowledge_graph', __name__, url_prefix='/api/knowledge-graph')

@knowledge_graph_bp.route('/sync', methods=['POST'])
def sync_data():
    """
    同步数据到图数据库
    
    Returns:
        同步结果
    """
    try:
        result = data_sync_service.sync_all_data()
        
        if result.get("success"):
            return success(
                data=result.get("statistics", {}),
                msg=result.get("message", "数据同步成功")
            )
        else:
            return error(
                msg=result.get("message", "数据同步失败"),
                code=500
            )
            
    except Exception as e:
        return error(f'数据同步失败: {str(e)}', code=500)

@knowledge_graph_bp.route('/stats', methods=['GET'])
def get_stats():
    """
    获取图数据库统计信息
    
    Returns:
        统计信息
    """
    try:
        stats = neo4j_client.get_statistics()
        
        # 添加统计数据的组织处理
        result = {
            "nodes": {},
            "relationships": {}
        }
        
        # 处理节点统计
        if "users" in stats:
            result["nodes"]["User"] = stats["users"]
        if "categories" in stats:
            result["nodes"]["Category"] = stats["categories"]
        if "items" in stats:
            result["nodes"]["Item"] = stats["items"]
        if "tags" in stats:
            result["nodes"]["Tag"] = stats["tags"]
            
        # 处理关系统计
        if "created" in stats:
            result["relationships"]["CREATED"] = stats["created"]
        if "belongs_to" in stats:
            result["relationships"]["BELONGS_TO"] = stats["belongs_to"]
        if "favorited" in stats:
            result["relationships"]["FAVORITED"] = stats["favorited"]
        if "liked" in stats:
            result["relationships"]["LIKED"] = stats["liked"]
        if "viewed" in stats:
            result["relationships"]["VIEWED"] = stats["viewed"]
        if "purchased" in stats:
            result["relationships"]["PURCHASED"] = stats["purchased"]
        if "has_tag" in stats:
            result["relationships"]["HAS_TAG"] = stats["has_tag"]
            
        return success(result, msg='获取统计信息成功')
    except Exception as e:
        return error(f'获取统计信息失败: {str(e)}', code=500)

def serialize_neo4j_data(obj):
    """
    递归序列化Neo4j数据，将DateTime等特殊对象转换为字符串
    
    Args:
        obj: 需要序列化的对象
        
    Returns:
        序列化后的对象
    """
    # 处理基本类型
    if isinstance(obj, (str, int, float, bool, type(None))):
        return obj
    
    # 处理DateTime对象
    if hasattr(obj, 'isoformat'):
        return obj.isoformat()
    
    # 处理字典
    if isinstance(obj, dict):
        return {key: serialize_neo4j_data(value) for key, value in obj.items()}
    
    # 处理列表
    if isinstance(obj, list):
        return [serialize_neo4j_data(item) for item in obj]
    
    # 处理其他可迭代对象
    if hasattr(obj, '__iter__'):
        try:
            return str(obj)
        except:
            return obj
    
    # 其他情况直接转换为字符串
    return str(obj)

@knowledge_graph_bp.route('/visualization', methods=['GET'])
def get_visualization_data():
    """
    获取知识图谱可视化数据
    
    Returns:
        节点和边的数据，适用于图形可视化
    """
    try:
        # 获取请求参数
        limit = request.args.get('limit', default=300, type=int)
        
        print(f"开始获取可视化数据，限制节点数: {limit}")
        
        # 获取所有节点
        nodes_query = f"""
        MATCH (n)
        RETURN n
        LIMIT {limit}
        """
        
        # 获取所有关系
        relationships_query = f"""
        MATCH (n)-[r]->(m)
        RETURN n, r, m, type(r) as relType
        LIMIT {limit * 2}
        """
        
        # 执行查询
        node_results = neo4j_client.execute_query_raw(nodes_query)
        rel_results = neo4j_client.execute_query_raw(relationships_query)
        
        print(f"获取到 {len(node_results)} 个节点，{len(rel_results)} 个关系")
        
        # 处理节点数据
        nodes = {}
        for record in node_results:
            node = record['n']
            node_labels = list(node.labels)
            if not node_labels:
                continue
                
            node_type = node_labels[0]
            node_id = f"{node_type}-{node['id']}"
            
            # 确定节点显示名称
            display_name = ""
            if node_type == 'User':
                display_name = node.get('username', f"用户{node['id']}")
            elif node_type == 'Item':
                display_name = node.get('title', f"景点{node['id']}")
            elif node_type == 'Category':
                display_name = node.get('name', f"分类{node['id']}")
            elif node_type == 'Tag':
                display_name = node.get('name', f"标签{node['id']}")
            else:
                display_name = f"{node_type}{node['id']}"
            
            nodes[node_id] = {
                'id': node_id,
                'name': display_name,
                'type': node_type,
                'properties': serialize_neo4j_data(dict(node))
            }
        
        # 处理关系数据 - 按节点对分组并合并同类型边
        edge_groups = {}  # key: (source, target), value: list of edges
        
        for record in rel_results:
            source_node = record['n']
            target_node = record['m']
            rel_type = record['relType']
            
            source_labels = list(source_node.labels)
            target_labels = list(target_node.labels)
            
            if not source_labels or not target_labels:
                continue
                
            source_id = f"{source_labels[0]}-{source_node['id']}"
            target_id = f"{target_labels[0]}-{target_node['id']}"
            
            # 确保source和target节点都存在
            if source_id not in nodes or target_id not in nodes:
                continue
            
            # 创建节点对的键
            node_pair_key = (source_id, target_id)
            
            if node_pair_key not in edge_groups:
                edge_groups[node_pair_key] = {}
            
            # 按关系类型分组
            if rel_type not in edge_groups[node_pair_key]:
                edge_groups[node_pair_key][rel_type] = 0
            edge_groups[node_pair_key][rel_type] += 1
        
        # 生成最终的边数据 - 每个节点对只有一条边
        edges = []
        for (source_id, target_id), rel_types in edge_groups.items():
            # 计算这个节点对总共有多少条边
            total_count = sum(rel_types.values())
            
            # 调试信息：输出节点对的边信息
            print(f"节点对 {source_id} -> {target_id}: 总边数={total_count}, 关系类型={list(rel_types.keys())}")
            
            # 合并所有关系类型为一个标签 - 使用简洁美观的格式
            if len(rel_types) == 1:
                # 只有一种关系类型时，显示简洁格式
                rel_type, count = list(rel_types.items())[0]
                if rel_type == 'CREATED':
                    chinese_label = "创建"
                elif rel_type == 'BELONGS_TO':
                    chinese_label = "属于"
                elif rel_type == 'VIEWED':
                    chinese_label = "浏览"
                elif rel_type == 'PURCHASED':
                    chinese_label = "预约"
                elif rel_type == 'FAVORITED':
                    chinese_label = "收藏"
                elif rel_type == 'LIKED':
                    chinese_label = "点赞"
                elif rel_type == 'HAS_TAG':
                    chinese_label = "标签"
                else:
                    chinese_label = rel_type
                
                if count > 1:
                    combined_label = f"{chinese_label} ×{count}"
                else:
                    combined_label = chinese_label
            else:
                # 多种关系类型时，使用简洁的组合格式
                type_labels = []
                for rel_type, count in rel_types.items():
                    if rel_type == 'CREATED':
                        type_name = "创建"
                    elif rel_type == 'BELONGS_TO':
                        type_name = "属于"
                    elif rel_type == 'VIEWED':
                        type_name = "浏览"
                    elif rel_type == 'PURCHASED':
                        type_name = "预约"
                    elif rel_type == 'FAVORITED':
                        type_name = "收藏"
                    elif rel_type == 'LIKED':
                        type_name = "点赞"
                    elif rel_type == 'HAS_TAG':
                        type_name = "标签"
                    else:
                        type_name = rel_type
                    
                    if count > 1:
                        type_labels.append(f"{type_name}×{count}")
                    else:
                        type_labels.append(type_name)
                
                # 使用中文逗号分隔，更美观
                combined_label = "、".join(type_labels)
            
            print(f"  合并边: {combined_label}")
            
            # 创建单一边，包含所有关系信息
            edges.append({
                'id': f"edge-{source_id}-{target_id}",
                'source': source_id,
                'target': target_id,
                'label': combined_label,
                'types': list(rel_types.keys()),  # 包含的所有关系类型
                'type_counts': rel_types,         # 每种类型的数量
                'total_count': total_count,       # 总数量
                'group_key': f"{source_id}-{target_id}"
            })
        
        # 构建返回数据
        visualization_data = {
            'nodes': list(nodes.values()),
            'edges': edges
        }
        
        print(f"处理完成 - 节点: {len(nodes)}, 边: {len(edges)}")
        
        return success(visualization_data, msg='获取可视化数据成功')
        
    except Exception as e:
        print(f"获取可视化数据失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return error(f'获取可视化数据失败: {str(e)}', code=500)

@knowledge_graph_bp.route('/user-profile/<int:user_id>', methods=['GET'])
def get_user_profile(user_id):
    """
    获取用户画像信息
    
    Args:
        user_id: 用户ID
        
    Returns:
        用户画像信息
    """
    try:
        # 获取用户偏好的类别
        category_query = """
        MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)-[:BELONGS_TO]->(c:Category)
        WITH c, COUNT(r) AS interactionCount, COLLECT(DISTINCT TYPE(r)) AS interactionTypes
        RETURN c.id AS id, c.name AS name, interactionCount AS count,
               interactionTypes AS types,
               SIZE(interactionTypes) AS diversityScore
        ORDER BY interactionCount DESC, diversityScore DESC
        LIMIT 5
        """
        
        # 获取用户交互最多的标签
        tag_query = """
        MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)
        WHERE i.tags IS NOT NULL
        UNWIND split(i.tags, ',') AS tag
        WITH TRIM(tag) AS cleanTag, COUNT(r) AS tagCount
        WHERE cleanTag <> ''
        RETURN cleanTag AS tag, tagCount AS count
        ORDER BY count DESC
        LIMIT 10
        """
        
        # 获取用户活跃度统计
        activity_query = """
        MATCH (u:User {id: $userId})-[r]->(i:Item)
        WITH TYPE(r) AS actionType, COUNT(r) AS actionCount
        RETURN actionType, actionCount
        ORDER BY actionCount DESC
        """
        
        # 获取用户的社交网络
        social_query = """
        MATCH (u1:User {id: $userId})-[r1:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)<-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]-(u2:User)
        WHERE u1 <> u2
        WITH u2, COUNT(DISTINCT i) AS commonItems
        RETURN u2.id AS userId, u2.username AS username, 
               commonItems,
               u2.avatarBucket AS avatarBucket,
               u2.avatarObjectKey AS avatarObjectKey
        ORDER BY commonItems DESC
        LIMIT 5
        """
        
        # 执行查询
        categories = neo4j_client.execute_query(category_query, {"userId": user_id})
        tags = neo4j_client.execute_query(tag_query, {"userId": user_id})
        activities = neo4j_client.execute_query(activity_query, {"userId": user_id})
        similar_users = neo4j_client.execute_query(social_query, {"userId": user_id})
        
        # 按活动类型整理活动统计
        activity_stats = {}
        for activity in activities:
            activity_stats[activity['actionType']] = activity['actionCount']
        
        # 构建用户画像数据
        user_profile = {
            "preferred_categories": categories,
            "preferred_tags": tags,
            "activity_stats": activity_stats,
            "similar_users": similar_users
        }
        
        return success(user_profile, msg='获取用户画像成功')
    except Exception as e:
        return error(f'获取用户画像失败: {str(e)}', code=500) 