"""
标签抽取服务 - 使用LLM从景点描述中抽取标签
"""
import logging
from typing import List, Dict, Any, Optional
from algo.llm.llm_client import LLMClient
from algo.llm.config import DEFAULT_MODEL

logger = logging.getLogger(__name__)

class TagExtractorService:
    """标签抽取服务"""
    
    def __init__(self, model_name: str = DEFAULT_MODEL):
        """
        初始化标签抽取服务
        
        Args:
            model_name: 使用的LLM模型名称
        """
        self.llm_client = LLMClient(model_name)
    
    def extract_tags_from_attraction(self, attraction_title: str, attraction_description: str, 
                                   category_name: str, category_description: str,
                                   original_tags: Optional[str] = None) -> List[str]:
        """
        从景点信息中抽取标签
        
        Args:
            attraction_title: 景点标题
            attraction_description: 景点描述
            category_name: 类别名称
            category_description: 类别描述
            original_tags: 原始标签字符串（逗号分隔）
            
        Returns:
            抽取的标签列表
        """
        try:
            # 构建系统提示
            system_message = {
                "role": "system",
                "content": """你是旅游景点标签抽取专家。从景点信息中抽取5-12个关键标签，用于推荐和分类。

规则：
- 标签简洁（2-4字）
- 优先：地理位置、景点类型、特色、体验
- 避免重复
- 保留有价值的原始标签

只返回标签，用逗号分隔。"""
            }
            
            # 构建用户消息
            user_content = f"""景点信息：
景点名称：{attraction_title}
景点描述：{attraction_description}
所属类别：{category_name}
类别描述：{category_description}"""
            
            if original_tags:
                user_content += f"\n原标签：{original_tags}"
            
            user_content += "\n\n抽取标签："
            
            user_message = {
                "role": "user",
                "content": user_content
            }
            
            # 调用LLM抽取标签
            messages = [system_message, user_message]
            response = self.llm_client.chat(messages)
            
            # 解析标签
            if response and isinstance(response, str):
                tags = [tag.strip() for tag in response.split(',') if tag.strip()]
                # 去重并限制数量
                unique_tags = list(dict.fromkeys(tags))[:12]  # 保持顺序去重，最多12个标签
                
                logger.info(f"从景点 '{attraction_title}' 抽取了 {len(unique_tags)} 个标签: {unique_tags}")
                return unique_tags
            else:
                logger.warning(f"LLM返回空响应，景点: {attraction_title}")
                return []
                
        except Exception as e:
            logger.error(f"标签抽取失败，景点: {attraction_title}, 错误: {str(e)}")
            return []
    
    def extract_tags_batch(self, attractions: List[Dict]) -> Dict[int, List[str]]:
        """
        批量抽取标签
        
        Args:
            attractions: 景点列表，每个元素包含id, title, description, category_name, category_description, tags
            
        Returns:
            景点ID到标签列表的映射
        """
        result = {}
        
        for attraction in attractions:
            attraction_id = attraction.get('id')
            if not attraction_id:
                continue
                
            tags = self.extract_tags_from_attraction(
                attraction_title=attraction.get('title', ''),
                attraction_description=attraction.get('description', ''),
                category_name=attraction.get('category_name', ''),
                category_description=attraction.get('category_description', ''),
                original_tags=attraction.get('tags', '')
            )
            
            if tags:
                result[attraction_id] = tags
        
        logger.info(f"批量抽取完成，成功处理 {len(result)} 个景点")
        return result
    
    def create_tag_nodes_data(self, tag_mappings: Dict[int, List[str]]) -> tuple[List[Dict], List[Dict]]:
        """
        创建标签节点数据和关系数据
        
        Args:
            tag_mappings: 景点ID到标签列表的映射
            
        Returns:
            (标签节点数据列表, 关系数据列表)
        """
        tag_nodes = []
        relationships = []
        tag_id_counter = 1
        tag_name_to_id = {}
        
        for attraction_id, tags in tag_mappings.items():
            for tag_name in tags:
                # 为每个标签创建节点（如果不存在）
                if tag_name not in tag_name_to_id:
                    tag_id = tag_id_counter
                    tag_name_to_id[tag_name] = tag_id
                    tag_id_counter += 1
                    
                    tag_nodes.append({
                        'id': tag_id,
                        'name': tag_name,
                        'type': 'llm_extracted',  # 标记为LLM抽取的标签
                        'createTime': 'datetime()',
                        'updateTime': 'datetime()'
                    })
                else:
                    tag_id = tag_name_to_id[tag_name]
                
                # 创建景点与标签的关系
                relationships.append({
                    'type': 'HAS_TAG',
                    'itemId': attraction_id,
                    'tagId': tag_id,
                    'createTime': 'datetime()'
                })
        
        logger.info(f"创建了 {len(tag_nodes)} 个标签节点和 {len(relationships)} 个关系")
        return tag_nodes, relationships

# 创建全局实例
tag_extractor_service = TagExtractorService()