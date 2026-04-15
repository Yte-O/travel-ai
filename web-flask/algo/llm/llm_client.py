"""
LLM客户端
"""
import json
import re
import requests
from typing import Dict, Any, List, Optional
from algo.llm.config import MODEL_CONFIGS, DEFAULT_MODEL


class LLMClient:
    """LLM客户端"""
    def __init__(self, model_name: str = DEFAULT_MODEL):
        """初始化LLM客户端
        
        Args:
            model_name: 模型名称，默认为配置中的默认模型
        """
        self.model_name = model_name
        self.config = MODEL_CONFIGS[model_name]
    
    def _make_headers(self) -> Dict:
        """生成请求头
        
        Returns:
            请求头字典
        """
        return {
            'Authorization': f'Bearer {self.config["api_key"]}',
            'Content-Type': 'application/json'
        }

    def _chat_completion(self, messages: List[Dict], max_tokens: Optional[int] = None, temperature: Optional[float] = None) -> str:
        """统一的对话请求封装"""
        data = {
            'model': self.config['model'],
            'messages': messages,
            'max_tokens': max_tokens if max_tokens is not None else self.config['max_tokens'],
            'temperature': temperature if temperature is not None else self.config['temperature'],
        }

        response = requests.post(self.config['api_url'], json=data, headers=self._make_headers())
        result = response.json()
        return result['choices'][0]['message']['content']
    
    def chat(self, messages: List[Dict]) -> str:
        """非流式聊天接口
        
        Args:
            messages: 消息列表，每个消息是包含role和content的字典
            
        Returns:
            模型返回的文本内容
        """
        # 添加系统提示，限制只回答旅游相关问题
        system_message = {
            "role": "system",
            "content": """你是一个专业的旅游咨询助手。请注意以下要求：

1. 只能回答与旅游、景点、旅行攻略、住宿、交通、美食等旅游相关的问题
2. 对于非旅游相关的问题（如医疗建议、法律咨询、技术问题、学习辅导等），请礼貌地拒绝并引导用户咨询相关专业人士
3. 必须拒绝回答任何政治敏感、色情、暴力、违法等有害内容
4. 回答要专业、友好，符合旅游咨询的语境
5. 如果用户问的是旅游相关但你不确定的信息，可以提供一般性建议并建议用户查询最新信息

请始终保持专业的旅游咨询助手身份，只在旅游领域内提供帮助。"""
        }
        
        # 将系统消息添加到消息列表的开头
        full_messages = [system_message] + messages
        
        return self._chat_completion(full_messages)
        
    def extract_keywords(self, messages: List[Dict], query: str = "给我推荐一些") -> List[str]:
        """从对话历史中提取关键词
        
        Args:
            messages: 历史消息列表
            query: 用户的推荐请求
            
        Returns:
            关键词列表
        """
        # 构建系统提示，指导LLM提取关键词，并添加拒答前置提示
        system_message = {
            "role": "system",
            "content": """你是一个旅游景点推荐系统的关键词提取助手。请从用户的对话历史中提取关键词，这些关键词将用于景点推荐。
            
请注意以下限制：
1. 只能提取与旅游、景点、用户喜好相关的关键词
2. 必须拒绝提取任何政治敏感、色情、暴力、歧视等有害内容的关键词
3. 不要提取与非法活动相关的关键词
4. 如果用户询问非景点推荐的内容（如医疗建议、法律咨询等），请返回空字符串

请只返回关键词(不要带书名号)，用逗号分隔，不要有其他内容。如果没有找到合适的关键词或用户意图不适合景点推荐，请返回空字符串。"""
        }
        
        # 构建用户消息，包含历史对话和提取指令
        user_content = f"以下是我和旅游助手的对话历史，请从中提取3-10个最重要的关键词，用于景点推荐系统查询：\n\n"
        
        # 智能过滤：只过滤掉纯推荐请求，保留包含具体需求的消息
        filtered_messages = []
        for msg in messages:
            content = msg.get('content', '')
            # 如果消息包含查询词，但同时包含其他有用信息，则保留
            if query in content:
                # 检查是否只是纯推荐请求（去掉查询词后内容很少）
                content_without_query = content.replace(query, '').strip()
                # 如果去掉查询词后还有实质内容（超过2个字符），则保留
                if len(content_without_query) > 2:
                    filtered_messages.append(msg)
            else:
                # 不包含查询词的消息直接保留
                filtered_messages.append(msg)
        
        # 添加历史消息内容
        for msg in filtered_messages:
            user_content += f"{msg['role']}: {msg['content']}\n"
            
        user_content += "\n请提取与景点推荐相关的关键词，只返回关键词(不要带书名号)，用逗号分隔，不要有其他内容。如果没有找到合适的景点相关关键词，请返回空字符串。"
        
        user_message = {
            "role": "user",
            "content": user_content
        }
        
        # 调用LLM提取关键词
        keywords_text = self._chat_completion([system_message, user_message], max_tokens=100, temperature=0.2)
        
        # 处理关键词，去除多余内容
        if not keywords_text or keywords_text.strip() == "":
            return []
        
        # 异常捕获
        try:
            keywords = [keyword.strip() for keyword in keywords_text.split(',') if keyword.strip()]
        except Exception as e:
            print(f"提取关键词失败: {e}")
            return []
        
        return keywords
    
    def chat_with_graph_rag(self, messages: List[Dict], graph_context: str) -> str:
        """使用图数据库上下文进行增强对话
        
        Args:
            messages: 对话历史消息列表
            graph_context: 从图数据库搜索到的上下文信息
            
        Returns:
            增强后的模型回复
        """
        # 构建系统提示，指导LLM使用图数据库上下文
        system_message = {
            "role": "system",
            "content": """你是一个专业的旅游咨询助手。你可以访问一个包含丰富旅游景点信息的知识图谱数据库。

请注意以下要求：
1. 只能使用提供的图数据库上下文信息来回答用户问题
2. 如果图数据库中没有找到相关信息，请明确告诉用户"很抱歉，我在数据库中没有找到与您问题相关的景点信息，请尝试其他关键词或换个问法"
3. 绝对不能使用你的通用知识来补充回答旅游景点相关的问题
4. 回答要自然、友好，符合旅游咨询的语境
5. 必须拒绝回答任何政治敏感、色情、暴力、违法等有害内容
6. 专注于旅游相关内容，对于非旅游问题请引导用户咨询相关专业人士

以下是从知识图谱中搜索到的相关信息：
{graph_context}

请严格基于这些信息回答用户问题。如果没有找到相关信息，请告知用户数据库中暂无相关内容。"""
        }
        
        # 格式化上下文信息
        formatted_context = self._format_graph_context(graph_context)
        system_message["content"] = system_message["content"].format(graph_context=formatted_context)
        
        # 构建完整的消息列表
        full_messages = [system_message] + messages
        
        # 调用LLM生成回复
        return self._chat_completion(full_messages)

    def extract_trip_intent(self, messages: List[Dict]) -> Dict[str, Any]:
        """从对话中提取行程意图，返回稳定JSON结构"""
        system_message = {
            "role": "system",
            "content": """你是旅行意图提取器。请从对话中提取行程规划参数，只输出JSON，不要输出任何额外文字。

JSON结构必须是：
{
  "city": "城市名，无法确定时为空字符串",
  "days": 1,
  "keywords": ["景点或偏好关键词"],
  "travel_mode": "driving|walking|cycling",
  "pace": "relaxed|normal|intensive"
}

规则：
1. days必须在1到10之间，无法判断默认2。
2. keywords提取3到8个，和旅游相关。
3. travel_mode无法判断默认walking。
4. pace无法判断默认normal。"""
        }

        user_lines = []
        for msg in messages[-20:]:
            role = msg.get('role', 'user')
            content = msg.get('content', '')
            user_lines.append(f"{role}: {content}")

        user_message = {
            "role": "user",
            "content": "请提取以下对话中的行程参数：\n" + "\n".join(user_lines)
        }

        raw = self._chat_completion([system_message, user_message], max_tokens=300, temperature=0.1)

        default_result = {
            "city": "",
            "days": 2,
            "keywords": [],
            "travel_mode": "walking",
            "pace": "normal"
        }

        try:
            start = raw.find('{')
            end = raw.rfind('}')
            if start != -1 and end != -1 and end > start:
                payload = raw[start:end + 1]
            else:
                payload = raw

            parsed = json.loads(payload)

            city = str(parsed.get('city', '')).strip()
            days = parsed.get('days', 2)
            try:
                days = int(days)
            except Exception:
                days = 2
            days = max(1, min(10, days))

            keywords = parsed.get('keywords', [])
            if isinstance(keywords, str):
                keywords = [k.strip() for k in re.split(r'[,，、\\s]+', keywords) if k.strip()]
            if not isinstance(keywords, list):
                keywords = []
            keywords = [str(k).strip() for k in keywords if str(k).strip()]

            travel_mode = str(parsed.get('travel_mode', 'walking')).strip().lower()
            if travel_mode not in ('driving', 'walking', 'cycling'):
                travel_mode = 'walking'

            pace = str(parsed.get('pace', 'normal')).strip().lower()
            if pace not in ('relaxed', 'normal', 'intensive'):
                pace = 'normal'

            return {
                "city": city,
                "days": days,
                "keywords": keywords[:8],
                "travel_mode": travel_mode,
                "pace": pace
            }
        except Exception:
            return default_result
    
    def _format_graph_context(self, graph_context: str) -> str:
        """格式化图数据库上下文信息
        
        Args:
            graph_context: 原始图数据库上下文
            
        Returns:
            格式化后的上下文字符串
        """
        if not graph_context or "未找到与关键词" in graph_context:
            return "【数据库搜索结果】：未找到相关的景点信息。"
        
        return f"""
=== 相关景点信息 ===
{graph_context}
""" 