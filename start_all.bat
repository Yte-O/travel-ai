@echo off
chcp 65001
echo ==============================
echo    一键启动旅行AI项目
echo ==============================

:: 启动前端 Vue
cd /d C:\Users\P\travel-ai\web-vue
start "前端服务" cmd /k "npm run dev"

:: 启动后端 SpringBoot
cd /d C:\Users\P\travel-ai\web-springboot
start "后端服务" cmd /k "mvn spring-boot:run"

:: 启动算法 Flask
cd /d C:\Users\P\travel-ai\web-flask
start "算法服务" cmd /k "conda activate py12 && python app.py"

:: 启动文件服务
cd /d C:\Users\P\travel-ai\web-file
start "文件服务" cmd /k "web-file-service.exe"

:: 启动 Neo4j
start "Neo4j数据库" cmd /k "neo4j console"

echo.
echo ✅ 所有服务已开始启动！
echo ✅ 前端地址：http://localhost:5173
echo.
pause