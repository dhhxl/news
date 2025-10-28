@echo off
chcp 65001 >nul
echo ========================================
echo    新闻管理系统 - 一键启动
echo ========================================
echo.

REM 检查必要的软件
echo [检查系统环境...]
echo.

REM 检查Java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [✗] 未检测到 Java
    set MISSING=1
) else (
    echo [✓] Java 已安装
)

REM 检查Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [✗] 未检测到 Maven
    set MISSING=1
) else (
    echo [✓] Maven 已安装
)

REM 检查Node.js
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [✗] 未检测到 Node.js
    set MISSING=1
) else (
    echo [✓] Node.js 已安装
)

REM 检查Docker
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [✗] 未检测到 Docker
    set MISSING=1
) else (
    echo [✓] Docker 已安装
)

if defined MISSING (
    echo.
    echo [错误] 缺少必要的软件，请先安装后再运行
    pause
    exit /b 1
)

echo.
echo ========================================
echo  启动顺序:
echo  1. 数据库服务 (Docker)
echo  2. 后端服务 (Spring Boot)
echo  3. 前端服务 (Vite)
echo ========================================
echo.

REM 启动数据库
echo [1/3] 启动数据库服务...
docker-compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 数据库启动失败
    pause
    exit /b 1
)
echo [✓] 数据库服务已启动
echo 等待数据库初始化（15秒）...
timeout /t 15 /nobreak >nul
echo.

REM 启动后端（在新窗口）
echo [2/3] 启动后端服务...
start "新闻系统-后端" cmd /k "cd backend && mvn spring-boot:run"
echo [✓] 后端服务正在启动（新窗口）
echo 等待后端启动（20秒）...
timeout /t 20 /nobreak >nul
echo.

REM 启动前端（在新窗口）
echo [3/3] 启动前端服务...
start "新闻系统-前端" cmd /k "cd frontend && npm run dev"
echo [✓] 前端服务正在启动（新窗口）
echo.

echo ========================================
echo  ✓ 全部服务启动完成！
echo ========================================
echo.
echo  访问地址:
echo  • 前端: http://localhost:5173
echo  • 后端: http://localhost:8080/api
echo.
echo  默认账号:
echo  • 管理员: admin / admin123
echo  • 编辑员: editor / editor123
echo.
echo  关闭所有窗口即可停止服务
echo ========================================
echo.

pause

