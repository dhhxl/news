@echo off
chcp 65001 > nul
cls
color 0A
echo ========================================
echo    新闻管理系统 - 一键启动
echo ========================================
echo.
echo 开始启动服务，请稍候...
echo.

echo [1/5] 检查 Docker 状态...
docker --version > nul 2>&1
if errorlevel 1 (
    echo   [警告] Docker 未安装或未启动
    echo   请先启动 Docker Desktop
    pause
    exit /b 1
)
echo   [√] Docker 已就绪
echo.

echo [2/5] 启动数据库服务 (MySQL + Redis)...
docker-compose up -d
if errorlevel 1 (
    echo   [×] 数据库启动失败
    pause
    exit /b 1
)
echo   [√] 数据库服务已启动
echo.

echo [3/5] 等待数据库初始化...
echo   正在等待 MySQL 和 Redis 就绪...
timeout /t 5 /nobreak > nul
echo   [√] 数据库已就绪
echo.

echo [4/5] 启动后端服务（新窗口）...
REM 检查是否配置了 ZhipuAI API Key
if "%ZHIPUAI_API_KEY%"=="" (
    echo   [提示] 未配置 ZhipuAI API Key，将使用模拟摘要
    echo   [提示] 如需使用真实AI摘要，请设置环境变量 ZHIPUAI_API_KEY
)
start "新闻管理系统 - 后端服务" cmd /k "cd /d %~dp0backend && echo 正在启动后端服务... && mvn spring-boot:run"
timeout /t 3 /nobreak > nul
echo   [√] 后端服务启动中...
echo.

echo [5/5] 启动前端服务（新窗口）...
start "新闻管理系统 - 前端服务" cmd /k "cd /d %~dp0frontend && echo 正在启动前端服务... && npm run dev"
echo   [√] 前端服务启动中...
echo.

echo ========================================
echo    启动完成！
echo ========================================
echo.
echo [等待时间] 请等待 30-40 秒让所有服务完全启动
echo.
echo ========================================
echo    访问地址
echo ========================================
echo.
echo  前台（用户端）
echo   主页：     http://localhost:5173
echo   新闻详情： http://localhost:5173/news/[id]
echo   分类浏览： http://localhost:5173/category/[id]
echo.
echo  后台（管理端）
echo   主页：     http://localhost:5173/admin
echo   登录页：   http://localhost:5173/login
echo   控制台：   http://localhost:5173/admin
echo   新闻管理： http://localhost:5173/admin/news
echo   采集管理： http://localhost:5173/admin/crawler
echo   分类管理： http://localhost:5173/admin/categories
echo.
echo  后端 API
echo   API地址：  http://localhost:8080/api
echo   健康检查： http://localhost:8080/api/health
echo   新闻列表： http://localhost:8080/api/news
echo   分类列表： http://localhost:8080/api/categories
echo.
echo ========================================
echo    登录信息
echo ========================================
echo.
echo   用户名：admin
echo   密码：  admin123
echo.
echo ========================================
echo    服务验证
echo ========================================
echo.
echo 等待 30 秒后开始检查服务状态...
timeout /t 30 /nobreak > nul
echo.
echo 检查服务运行状态...
echo.

REM 检查 MySQL
netstat -an | findstr ":3306" > nul 2>&1
if errorlevel 1 (
    echo [×] MySQL     端口 3306 - 未运行
) else (
    echo [√] MySQL     端口 3306 - 运行中
)

REM 检查 Redis
netstat -an | findstr ":6379" > nul 2>&1
if errorlevel 1 (
    echo [×] Redis     端口 6379 - 未运行
) else (
    echo [√] Redis     端口 6379 - 运行中
)

REM 检查后端
netstat -an | findstr ":8080" > nul 2>&1
if errorlevel 1 (
    echo [×] 后端服务  端口 8080 - 未运行（可能还在启动中）
    echo     请等待片刻或查看后端窗口日志
) else (
    echo [√] 后端服务  端口 8080 - 运行中
)

REM 检查前端
netstat -an | findstr ":5173" > nul 2>&1
if errorlevel 1 (
    echo [×] 前端服务  端口 5173 - 未运行（可能还在启动中）
    echo     请等待片刻或查看前端窗口日志
) else (
    echo [√] 前端服务  端口 5173 - 运行中
)

echo.
echo ========================================
echo    快速操作
echo ========================================
echo.
echo  1. 查看详细状态：运行 check_status.bat
echo  2. 停止所有服务：运行 stop_system.bat
echo  3. 如需帮助：查看 README.md
echo.
echo ========================================
echo    提示
echo ========================================
echo.
echo  - 如果服务未运行，请查看对应的窗口日志
echo  - 后端启动需要 20-30 秒
echo  - 前端启动需要 5-10 秒
echo  - 可以关闭本窗口，服务将继续运行
echo.
echo ========================================
pause

