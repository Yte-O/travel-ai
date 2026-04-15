"""
LLM路由
"""
import os
import math
import re
import time
import threading
from datetime import datetime, timedelta
from flask import Blueprint, request, jsonify, Response, stream_with_context
from typing import List, Dict, Any, Tuple
from algo.llm.llm_client import LLMClient
from algo.llm.config import AVAILABLE_MODELS, DEFAULT_MODEL
from utils.response import success, error
from algo.knowledge_graph.neo4j_client import neo4j_client
import requests


llm_bp = Blueprint('llm', __name__, url_prefix='/api/llm')

AMAP_WEB_BASE_URL = 'https://restapi.amap.com/v3'
# 默认使用高德 Web 服务 Key（用于 restapi.amap.com）
DEFAULT_AMAP_WEB_KEY = '188564ed22c138582ba0ff36924cc054'


class AMapWebClient:
    """高德Web API轻量封装"""

    _rate_lock = threading.Lock()
    _last_request_ts = 0.0

    def __init__(self):
        self.api_key = os.getenv('AMAP_WEB_KEY') or os.getenv('AMAP_API_KEY') or DEFAULT_AMAP_WEB_KEY

    def _request(self, path: str, params: Dict[str, Any]) -> Dict[str, Any]:
        query = dict(params or {})
        query['key'] = self.api_key

        max_retries = 5
        for attempt in range(max_retries + 1):
            # 全局请求节流：QPS<=3，预留一点安全余量
            with self._rate_lock:
                now = time.time()
                min_interval = 0.55
                wait_sec = min_interval - (now - self._last_request_ts)
                if wait_sec > 0:
                    time.sleep(wait_sec)
                self._last_request_ts = time.time()

            resp = requests.get(f'{AMAP_WEB_BASE_URL}{path}', params=query, timeout=12)
            resp.raise_for_status()
            data = resp.json()

            if data.get('status') == '1':
                return data

            infocode = str(data.get('infocode', 'unknown'))
            info = data.get('info', '高德接口错误')

            # 10021: QPS超限，指数退避后重试
            if infocode == '10021' and attempt < max_retries:
                backoff = 0.4 * (2 ** attempt)
                time.sleep(backoff)
                continue

            raise ValueError(f'{info}({infocode})')

        raise ValueError('高德接口重试后仍失败(10021)')

    @staticmethod
    def _parse_location(location: str) -> Tuple[float, float]:
        lng_str, lat_str = location.split(',')
        return float(lng_str), float(lat_str)

    @staticmethod
    def _parse_polyline(polyline: str) -> List[List[float]]:
        points: List[List[float]] = []
        if not polyline:
            return points
        for pair in polyline.split(';'):
            if ',' not in pair:
                continue
            lng, lat = pair.split(',')
            try:
                points.append([float(lng), float(lat)])
            except Exception:
                continue
        return points

    def search_poi(self, keyword: str, city: str = '', page_size: int = 6) -> List[Dict[str, Any]]:
        data = self._request('/place/text', {
            'keywords': keyword,
            'city': city,
            'citylimit': 'true' if city else 'false',
            'offset': page_size,
            'extensions': 'all'
        })
        pois = data.get('pois', []) or []
        result: List[Dict[str, Any]] = []
        for poi in pois:
            location = poi.get('location')
            if not location:
                continue
            try:
                lng, lat = self._parse_location(location)
            except Exception:
                continue
            result.append({
                'name': poi.get('name', '未知景点'),
                'address': poi.get('address', ''),
                'adname': poi.get('adname', ''),
                'location': [lng, lat]
            })
        return result

    def plan_route(self, origin: List[float], destination: List[float], mode: str = 'walking') -> Dict[str, Any]:
        mode_path_map = {
            'walking': '/direction/walking',
            'cycling': '/direction/bicycling',
            'driving': '/direction/driving'
        }
        path = mode_path_map.get(mode, '/direction/walking')
        data = self._request(path, {
            'origin': f'{origin[0]},{origin[1]}',
            'destination': f'{destination[0]},{destination[1]}',
            'extensions': 'base'
        })

        route_obj = (data.get('route', {}) or {})
        paths = route_obj.get('paths', []) or []
        if not paths:
            raise ValueError('高德路线规划未返回有效路径')

        first_path = paths[0]
        distance_km = round(float(first_path.get('distance', 0)) / 1000, 2)
        duration_min = round(float(first_path.get('duration', 0)) / 60)

        polyline: List[List[float]] = []
        for step in first_path.get('steps', []) or []:
            points = self._parse_polyline(step.get('polyline', ''))
            if not points:
                continue
            if polyline and polyline[-1] == points[0]:
                polyline.extend(points[1:])
            else:
                polyline.extend(points)

        if not polyline:
            raise ValueError('高德路线规划未返回有效折线')

        return {
            'mode': mode,
            'distanceKm': distance_km,
            'durationMinutes': duration_min,
            'polyline': polyline
        }


def _split_points_for_days(points: List[Dict[str, Any]], days: int) -> List[List[Dict[str, Any]]]:
    if days <= 1:
        return [points]

    result: List[List[Dict[str, Any]]] = [[] for _ in range(days)]
    for idx, point in enumerate(points):
        result[idx % days].append(point)
    return [group for group in result if group]


def _pace_to_stay_minutes(pace: str) -> int:
    if pace == 'relaxed':
        return 150
    if pace == 'intensive':
        return 90
    return 120


def _normalize_poi_name(name: str) -> str:
    """景点名称归一化，用于语义判重。"""
    if not name:
        return ''
    text = name.strip().lower()
    text = re.sub(r'[（(【\[].*?[）)】\]]', '', text)
    text = re.sub(r'\s+', '', text)

    removable_tokens = [
        '游客中心', '游客服务中心', '旅游服务中心', '服务中心', '服务点',
        '售票处', '购票点', '检票口', '入口', '入口处', '出口',
        '停车场', '停车区', '咨询处', '换乘中心', '码头游客中心'
    ]
    for token in removable_tokens:
        text = text.replace(token, '')

    text = re.sub(r'[·•\-—_]', '', text)
    return text


def _is_low_priority_service_poi(poi: Dict[str, Any]) -> bool:
    """识别游客中心/售票处等服务设施点位，作为低优先级。"""
    name = str(poi.get('name', '') or '')
    address = str(poi.get('address', '') or '')
    merged = f'{name}{address}'
    low_tokens = [
        '游客中心', '服务中心', '售票处', '停车场', '咨询处', '检票口', '入口处', '出口处'
    ]
    return any(token in merged for token in low_tokens)


def _distance_km(a: List[float], b: List[float]) -> float:
    """两经纬度点近似球面距离（km）。"""
    lng1, lat1 = float(a[0]), float(a[1])
    lng2, lat2 = float(b[0]), float(b[1])
    rad = math.pi / 180.0
    x = (lng2 - lng1) * rad * math.cos((lat1 + lat2) * rad / 2)
    y = (lat2 - lat1) * rad
    return 6371.0 * math.sqrt(x * x + y * y)


def _is_same_place(a: Dict[str, Any], b: Dict[str, Any]) -> bool:
    """语义+空间综合判重。"""
    na = _normalize_poi_name(str(a.get('name', '') or ''))
    nb = _normalize_poi_name(str(b.get('name', '') or ''))
    if not na or not nb:
        return False

    la = a.get('location') or []
    lb = b.get('location') or []
    if not (isinstance(la, list) and isinstance(lb, list) and len(la) >= 2 and len(lb) >= 2):
        return na == nb

    dist = _distance_km(la, lb)

    # 规则1：归一化名称完全一致，且距离较近
    if na == nb and dist <= 1.2:
        return True

    # 规则2：名称包含关系，且距离很近（如“西湖” vs “西湖游客中心”）
    if (na in nb or nb in na) and dist <= 1.0:
        return True

    # 规则3：极近距离且行政区一致，也视为同点簇
    ad_a = str(a.get('adname', '') or '')
    ad_b = str(b.get('adname', '') or '')
    if dist <= 0.25 and ad_a and ad_a == ad_b:
        return True

    return False


def _poi_priority_score(poi: Dict[str, Any]) -> float:
    """点位优先级分数：主景点 > 服务设施；名称更精炼优先。"""
    name = str(poi.get('name', '') or '')
    score = 0.0
    if not _is_low_priority_service_poi(poi):
        score += 10.0
    score -= min(len(name), 60) / 100.0
    return score


def _deduplicate_pois(pois: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """对POI列表执行判重，保留更高质量点位。"""
    deduped: List[Dict[str, Any]] = []
    for poi in pois:
        replaced = False
        for idx, kept in enumerate(deduped):
            if _is_same_place(kept, poi):
                if _poi_priority_score(poi) > _poi_priority_score(kept):
                    deduped[idx] = poi
                replaced = True
                break
        if not replaced:
            deduped.append(poi)
    return deduped

@llm_bp.route('/models', methods=['GET'])
def get_models():
    """获取可用模型列表
    
    Returns:
        模型列表
    """
    return success(AVAILABLE_MODELS)

@llm_bp.route('/chat', methods=['POST'])
def chat():
    """普通对话接口
    
    请求体:
        model: 模型名称，可选，默认为DEFAULT_MODEL
        messages: 消息列表，每个消息包含role和content
        
    Returns:
        模型返回的文本内容
    """
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        
        if not messages:
            return error('消息列表不能为空')
        
        client = LLMClient(model_name)
        response = client.chat(messages)
        return success(response)
    except Exception as e:
        return error(f'聊天失败: {str(e)}')


@llm_bp.route('/recommend', methods=['POST'])
def recommend_from_chat():
    """基于聊天内容推荐景点
    
    请求体:
        model: 模型名称，可选，默认为DEFAULT_MODEL
        messages: 消息列表，每个消息包含role和content
        userId: 用户ID，必传
        limit: 返回的推荐数量，默认10
        
    Returns:
        推荐景点列表和提取的关键词
    """
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        user_id = data.get('userId')
        limit = data.get('limit', 10)
        
        if not messages:
            return error('消息列表不能为空')
        
        if user_id is None:
            return error('用户ID不能为空')
        
        # 提取关键词
        client = LLMClient(model_name)
        keywords = client.extract_keywords(messages)
        
        # 如果关键词为空，返回用户历史交互景点
        if not keywords or (isinstance(keywords, list) and keywords[0] == '空字符串'):
            history_items = get_user_history_items(user_id, limit)
            return success({
                "items": history_items,
                "keywords": [],
                "message": "未能从对话中提取到有效的景点关键词，为您推荐您可能感兴趣的景点",
                "isHistoryItems": True  # 标识这是历史交互景点
            })
        
        # 基于关键词构建Neo4j查询，利用Tag节点进行更精确的匹配
        query = """
        // 基于关键词匹配景点
        MATCH (i:Item)
        MATCH (creator:User)-[:CREATED]->(i)
        
        // 关联景点所属类别和标签
        MATCH (i)-[:BELONGS_TO]->(c:Category)
        OPTIONAL MATCH (i)-[:HAS_TAG]->(t:Tag)
        
        // 收集标签信息
        WITH i, creator, c, COLLECT(DISTINCT t.name) AS tagNames
        
        // 进行关键词匹配
        WITH i, creator, c, tagNames,
             // 匹配标题、描述、原始标签或Tag节点中包含关键词的景点
             (
                 ANY(keyword IN $keywords WHERE toLower(i.title) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE toLower(i.description) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE toLower(coalesce(i.tags, '')) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE ANY(tag IN tagNames WHERE toLower(tag) CONTAINS toLower(keyword)))
             ) AS keywordMatch
        
        // 只返回匹配关键词的景点
        WHERE keywordMatch
        
        // 计算热门度和匹配度
        WITH i, creator, c, tagNames,
             SIZE([(i)<-[:VIEWED|PURCHASED|FAVORITED|LIKED]-() | true]) AS popularity,
             // 计算关键词匹配度得分
             (
                 SIZE([keyword IN $keywords WHERE toLower(i.title) CONTAINS toLower(keyword)]) * 3.0 +
                 SIZE([keyword IN $keywords WHERE toLower(i.description) CONTAINS toLower(keyword)]) * 2.0 +
                 SIZE([keyword IN $keywords WHERE toLower(coalesce(i.tags, '')) CONTAINS toLower(keyword)]) * 2.0 +
                 SIZE([keyword IN $keywords WHERE ANY(tag IN tagNames WHERE toLower(tag) CONTAINS toLower(keyword))]) * 2.5
             ) AS matchScore
        
        // 计算综合得分
        WITH i, creator, c, tagNames, (matchScore * 10 + popularity * 0.1) AS totalScore
        
        // 返回匹配结果
        RETURN i.id AS id, i.title AS title, i.description AS description, 
               i.tags AS tags, i.coverBucket AS coverBucket, i.coverObjectKey AS coverObjectKey,
               creator.username AS username, creator.realName AS userRealName,
               c.name AS categoryName, tagNames, totalScore AS score
        ORDER BY score DESC
        LIMIT $limit
        """
        
        # 执行查询
        params = {
            "keywords": keywords,
            "userId": user_id,
            "limit": limit
        }
        
        items = neo4j_client.execute_query(query, params)
        
        # 如果没有找到匹配的景点，返回用户历史交互景点
        if not items:
            history_items = get_user_history_items(user_id, limit)
            return success({
                "items": history_items,
                "keywords": keywords,
                "message": f"没有找到与 {', '.join(keywords)} 相关的新景点，为您推荐了一些您可能感兴趣的景点",
                "isHistoryItems": True  # 标识这是历史交互景点
            })
        
        # 处理标签字段，合并原始标签和Tag节点标签
        for item in items:
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
        
        return success({
            "items": items,
            "keywords": keywords,
            "message": "根据您的偏好，为您推荐以下景点",
            "isHistoryItems": False  # 标识这是关键词匹配景点
        })
    except Exception as e:
        return error(f'基于对话生成景点推荐失败: {str(e)}')

def get_user_history_items(user_id: int, limit: int = 10) -> list:
    """获取用户历史交互的景点
    
    Args:
        user_id: 用户ID
        limit: 返回数量限制
        
    Returns:
        用户交互过的景点列表，按景点ID去重，合并交互类型
    """
    # 查询用户交互过的景点，按景点ID分组，合并交互类型
    query = """
    // 查找用户交互过的景点
    MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)-[:BELONGS_TO]->(c:Category)
    MATCH (creator:User)-[:CREATED]->(i)
    
    // 获取Tag节点信息
    OPTIONAL MATCH (i)-[:HAS_TAG]->(tag:Tag)
    
    WITH i, creator, c, r, TYPE(r) AS interactionType,
         CASE TYPE(r)
           WHEN 'PURCHASED' THEN 4  // 预约权重最高
           WHEN 'FAVORITED' THEN 3  // 收藏次之
           WHEN 'LIKED' THEN 2      // 点赞再次
           WHEN 'VIEWED' THEN 1     // 浏览权重最低
           ELSE 0
         END AS typeWeight,
         r.createTime AS interactionTime,
         COLLECT(DISTINCT tag.name) AS tagNames
    
    // 按景点ID分组，收集所有交互类型
    WITH i.id AS itemId, i, creator, c, tagNames,
         COLLECT({
           type: interactionType,
           weight: typeWeight,
           time: interactionTime,
           label: CASE interactionType
             WHEN 'PURCHASED' THEN '您预约过'
             WHEN 'FAVORITED' THEN '您收藏过'
             WHEN 'LIKED' THEN '您点赞过'
             WHEN 'VIEWED' THEN '您浏览过'
             ELSE ''
           END
         }) AS interactions
    
    // 计算最高权重和最新时间
    WITH itemId, i, creator, c, tagNames, interactions,
         REDUCE(maxWeight = 0, interaction IN interactions | 
           CASE WHEN interaction.weight > maxWeight THEN interaction.weight ELSE maxWeight END) AS maxWeight,
         REDUCE(latestTime = datetime(), interaction IN interactions | 
           CASE WHEN interaction.time > latestTime THEN interaction.time ELSE latestTime END) AS latestTime
    
    // 按最高权重和最新时间排序
    ORDER BY maxWeight DESC, latestTime DESC
    
    // 返回结果，合并交互类型信息
    RETURN itemId AS id, i.title AS title, i.description AS description, 
           i.tags AS tags, i.coverBucket AS coverBucket, i.coverObjectKey AS coverObjectKey,
           creator.username AS username, creator.realName AS userRealName,
           c.name AS categoryName, tagNames,
           interactions AS allInteractions,
           // 生成合并的交互标签
           REDUCE(labels = [], interaction IN interactions | 
             CASE WHEN interaction.label IN labels THEN labels ELSE labels + [interaction.label] END
           ) AS interactionLabels
    LIMIT $limit
    """
    
    params = {"userId": user_id, "limit": limit}
    
    try:
        items = neo4j_client.execute_query(query, params)
        
        # 处理标签字段，合并原始标签和Tag节点标签
        for item in items:
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
            
            # 处理交互类型信息
            all_interactions = item.get("allInteractions", [])
            interaction_labels = item.get("interactionLabels", [])
            
            # 设置主要交互类型（权重最高的）
            if all_interactions:
                primary_interaction = max(all_interactions, key=lambda x: x.get("weight", 0))
                item["interactionType"] = primary_interaction.get("type", "")
                item["interactionLabel"] = primary_interaction.get("label", "")
            else:
                item["interactionType"] = ""
                item["interactionLabel"] = ""
            
            # 生成合并的交互描述
            if len(interaction_labels) == 1:
                item["interactionDescription"] = interaction_labels[0]
            elif len(interaction_labels) > 1:
                # 将多个交互类型合并为一个描述
                item["interactionDescription"] = f"您{'、'.join([label.replace('您', '').replace('过', '') for label in interaction_labels])}过"
            else:
                item["interactionDescription"] = "您交互过"
            
            # 清理临时字段
            if "allInteractions" in item:
                del item["allInteractions"]
            if "interactionLabels" in item:
                del item["interactionLabels"]
            
            # 添加一个score字段，保持与推荐结果格式一致
            item["score"] = 0
        
        return items
    except Exception as e:
        print(f"获取用户历史交互景点失败: {str(e)}")
        return []


@llm_bp.route('/chat-with-graph-rag', methods=['POST'])
def chat_with_graph_rag():
    """使用GraphRAG增强的对话接口
    
    请求体:
        model: 模型名称，可选，默认为DEFAULT_MODEL
        messages: 消息列表，每个消息包含role和content
        
    Returns:
        基于图数据库上下文增强的模型回复和搜索信息
    """
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        
        if not messages:
            return error('消息列表不能为空')
        
        # 初始化LLM客户端
        client = LLMClient(model_name)
        
        # 从历史对话中提取关键词
        keywords = client.extract_keywords(messages)
        
        # 构建图数据库上下文并获取搜索结果信息
        graph_context, search_info = _build_graph_context_with_info(keywords)
        
        # 使用GraphRAG增强对话
        response = client.chat_with_graph_rag(messages, graph_context)
        
        return success({
            'response': response,
            'searchInfo': search_info
        })
    
    except Exception as e:
        return error(f'GraphRAG对话失败: {str(e)}')


@llm_bp.route('/itinerary-plan', methods=['POST'])
def itinerary_plan():
    """从对话生成结构化路书，包含每日行程和地图路线段"""
    try:
        payload = request.json or {}
        model_name = payload.get('model', DEFAULT_MODEL)
        messages = payload.get('messages', []) or []
        if not messages:
            return error('消息列表不能为空')

        manual_city = (payload.get('city') or '').strip()
        manual_days = payload.get('days')

        llm_client = LLMClient(model_name)
        amap_client = AMapWebClient()

        intent = llm_client.extract_trip_intent(messages)
        city = manual_city or intent.get('city', '')
        days = intent.get('days', 2)
        if manual_days is not None:
            try:
                days = int(manual_days)
            except Exception:
                pass
        days = max(1, min(10, int(days)))

        keywords = intent.get('keywords', []) or []
        if not keywords:
            fallback_keywords = llm_client.extract_keywords(messages)
            keywords = fallback_keywords if fallback_keywords else ['热门景点', '博物馆', '地标建筑']

        mode = intent.get('travel_mode', 'walking')
        pace = intent.get('pace', 'normal')

        poi_pool: List[Dict[str, Any]] = []
        seen = set()
        for kw in keywords[:4]:
            found = amap_client.search_poi(kw, city=city, page_size=4)
            for poi in found:
                key = f"{poi['name']}|{poi['address']}"
                if key in seen:
                    continue
                seen.add(key)
                poi_pool.append(poi)

        if len(poi_pool) < 4:
            more = amap_client.search_poi('热门景点', city=city, page_size=8)
            for poi in more:
                key = f"{poi['name']}|{poi['address']}"
                if key in seen:
                    continue
                seen.add(key)
                poi_pool.append(poi)

        # 语义+空间去重，优先保留主景点，减少“西湖/西湖游客中心”这类重复
        poi_pool = _deduplicate_pois(poi_pool)
        non_service_pois = [poi for poi in poi_pool if not _is_low_priority_service_poi(poi)]
        # 当非服务设施点位足够时，移除服务设施点位，提升路书质量
        if len(non_service_pois) >= max(4, days * 2):
            poi_pool = non_service_pois

        if not poi_pool:
            return error('未找到可用的景点数据，请尝试补充城市或偏好关键词', code=400)

        total_points = min(max(days * 3, 4), 15, len(poi_pool))
        selected_points = poi_pool[:total_points]
        day_groups = _split_points_for_days(selected_points, days)

        day_themes = ['城市地标', '人文历史', '美食漫游', '自然休闲', '深度探索']
        stay_minutes = _pace_to_stay_minutes(pace)

        result_days: List[Dict[str, Any]] = []
        all_route_segments: List[Dict[str, Any]] = []

        for day_idx, group in enumerate(day_groups):
            current_time = datetime.strptime('09:00', '%H:%M')
            items: List[Dict[str, Any]] = []

            for idx, poi in enumerate(group):
                start_time = current_time.strftime('%H:%M')
                end_time = (current_time + timedelta(minutes=stay_minutes)).strftime('%H:%M')

                transport = None
                if idx > 0:
                    prev = group[idx - 1]
                    transport = amap_client.plan_route(prev['location'], poi['location'], mode=mode)
                    all_route_segments.append({
                        'day': day_idx + 1,
                        'from': prev['name'],
                        'to': poi['name'],
                        **transport
                    })
                    current_time = current_time + timedelta(minutes=max(transport.get('durationMinutes', 0), 20))
                    start_time = current_time.strftime('%H:%M')
                    end_time = (current_time + timedelta(minutes=stay_minutes)).strftime('%H:%M')

                item = {
                    'name': poi['name'],
                    'address': poi.get('address', ''),
                    'adname': poi.get('adname', ''),
                    'location': poi['location'],
                    'startTime': start_time,
                    'endTime': end_time,
                    'stayMinutes': stay_minutes,
                    'tips': '建议提前查看营业时间，避开高峰时段。'
                }
                if transport:
                    item['transportFromPrev'] = transport

                items.append(item)
                current_time = current_time + timedelta(minutes=stay_minutes)

            result_days.append({
                'day': day_idx + 1,
                'theme': day_themes[day_idx % len(day_themes)],
                'summary': f'安排 {len(items)} 个点位，节奏 {pace}',
                'items': items
            })

        data = {
            'title': f"{city or '城市'}{len(result_days)}日行程",
            'city': city,
            'days': result_days,
            'keywords': keywords,
            'travelMode': mode,
            'pace': pace,
            'routeSegments': all_route_segments,
            'generatedAt': datetime.now().isoformat()
        }
        return success(data)

    except Exception as e:
        return error(f'生成路书失败: {str(e)}')



def _build_graph_context_with_info(keywords: List[str]) -> tuple[str, dict]:
    """构建图数据库上下文信息并返回搜索详情
    
    Args:
        keywords: 提取的关键词列表
        
    Returns:
        (格式化的上下文字符串, 搜索信息字典)
    """
    context_parts = []
    search_info = {
        'keywords': keywords,
        'found_items': 0,
        'search_status': 'no_keywords',
        'message': ''
    }
    
    # 基于关键词搜索相关景点
    if keywords:
        related_items = neo4j_client.search_related_items(keywords, limit=8)
        search_info['found_items'] = len(related_items) if related_items else 0
        
        if related_items:
            search_info['search_status'] = 'found'
            search_info['message'] = f"找到 {len(related_items)} 个相关景点"
            
            context_parts.append("### 相关景点信息 ###")
            for i, item in enumerate(related_items, 1):
                context_parts.append(f"{i}. {item['title']}")
                context_parts.append(f"   类别: {item.get('categoryName', '未分类')}")
                context_parts.append(f"   描述: {item.get('description', '暂无描述')[:100]}...")
                if item.get('tags'):
                    context_parts.append(f"   标签: {', '.join(item['tags'][:5])}")
                context_parts.append("")
        else:
            search_info['search_status'] = 'not_found'
            search_info['message'] = f"未找到与关键词 [{', '.join(keywords)}] 相关的景点"
            
            context_parts.append("### 搜索结果 ###")
            context_parts.append(f"未找到与关键词 [{', '.join(keywords)}] 直接相关的景点信息。")
            context_parts.append("")
    else:
        search_info['message'] = "未能从对话中提取到有效的旅游关键词"
    
    # 添加搜索关键词信息
    if keywords:
        context_parts.append("### 本次搜索关键词 ###")
        context_parts.append(f"提取的关键词: {', '.join(keywords)}")
        context_parts.append("")
    
    # 如果没有任何上下文信息
    if not context_parts:
        context = "未提取到有效关键词，无法从数据库中搜索相关景点信息。"
    else:
        context = "\n".join(context_parts)
    
    return context, search_info 