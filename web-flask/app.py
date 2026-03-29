import os
import uuid
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
from config import PORT
from routes.health import health_bp
from routes.llm import llm_bp
from routes.knowledge_graph import knowledge_graph_bp
from routes.recommendation import recommendation_bp

app = Flask(__name__)
CORS(app)

# 注册路由(注意：url前缀已经在路由文件中设置，无需重复设置)
app.register_blueprint(health_bp)
app.register_blueprint(llm_bp)
app.register_blueprint(knowledge_graph_bp)
app.register_blueprint(recommendation_bp)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=PORT) 