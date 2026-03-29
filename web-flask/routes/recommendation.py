"""
推荐系统路由
"""
from flask import Blueprint, request
from algo.knowledge_graph.recommender import kg_recommender
from utils.response import success, error

# url前缀统一需要添加/api
recommendation_bp = Blueprint('recommendation', __name__, url_prefix='/api/recommendation')

@recommendation_bp.route('/user/<int:user_id>', methods=['GET'])
def recommend_for_user(user_id: int):
    """
    为用户推荐景点（基于用户）
    
    Args:
        user_id: 用户ID
        
    Returns:
        推荐景点列表
    """
    try:
        # 获取请求参数
        limit = request.args.get('limit', default=10, type=int)
        
        # 调用推荐算法
        items = kg_recommender.recommend_by_user_id(user_id, limit)
        
        return success(data=items, msg=f'为用户{user_id}生成了{len(items)}个协同过滤推荐')
    except Exception as e:
        return error(msg=f'推荐生成失败: {str(e)}', code=500)

@recommendation_bp.route('/user/<int:user_id>/content', methods=['GET'])
def recommend_content_for_user(user_id: int):
    """
    为用户推荐景点（基于内容）
    
    Args:
        user_id: 用户ID
        
    Returns:
        推荐景点列表
    """
    try:
        # 获取请求参数
        limit = request.args.get('limit', default=10, type=int)
        
        # 调用推荐算法
        items = kg_recommender.recommend_by_content(user_id, limit)
        
        return success(data=items, msg=f'为用户{user_id}生成了{len(items)}个基于内容的推荐')
    except Exception as e:
        return error(msg=f'推荐生成失败: {str(e)}', code=500)
