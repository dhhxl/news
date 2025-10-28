@echo off
chcp 65001 >nul
echo ========================================
echo    新闻管理系统 - 前端启动脚本
echo ========================================
echo.

REM 检查Node.js是否安装
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

echo [1/3] 检测到 Node.js 版本:
node --version
echo.

REM 切换到frontend目录
cd frontend
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 找不到 frontend 目录
    pause
    exit /b 1
)

REM 检查是否已安装依赖
if not exist "node_modules" (
    echo [2/3] 首次运行，正在安装依赖...
    echo 这可能需要几分钟时间，请耐心等待...
    echo.
    call npm install
    if %ERRORLEVEL% NEQ 0 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
    echo.
    echo [✓] 依赖安装完成
    echo.
) else (
    echo [2/3] 依赖已安装，跳过安装步骤
    echo.
)

REM 启动前端开发服务器
echo [3/3] 正在启动前端开发服务器...
echo.
echo ========================================
echo  前端服务将在以下地址运行:
echo  http://localhost:5173
echo ========================================
echo.
echo 按 Ctrl+C 停止服务器
echo.

npm run dev

pause

