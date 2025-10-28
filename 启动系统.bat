@echo off
chcp 65001 >nul
title 新闻管理系统 - 启动

echo ========================================
echo     新闻管理系统 - 完整启动
echo ========================================
echo.

echo [提示] 请确保已安装:
echo   - Java 17
echo   - Node.js 18+
echo   - MySQL 8.0
echo   - Redis (通过Docker)
echo.

echo [1/4] 检查Docker服务...
docker ps >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker已运行

echo.
echo [2/4] 启动MySQL和Redis...
docker-compose up -d
timeout /t 5 /nobreak >nul

echo.
echo [3/4] 启动后端...
start "后端服务" cmd /k "cd backend && mvn spring-boot:run"
echo [提示] 等待后端启动...
timeout /t 20 /nobreak

echo.
echo [4/4] 启动前端...
start "前端服务" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo     启动完成！
echo ========================================
echo.
echo 前端地址: http://localhost:5173
echo 后端地址: http://localhost:8080
echo.
echo 默认账号:
echo   管理员: admin / admin123
echo   编辑员: editor / editor123
echo.
echo [提示] 关闭此窗口不会停止服务
echo        使用 "停止所有服务.bat" 停止服务
echo.

pause

