@echo off
chcp 65001 >nul
echo ========================================
echo    新闻管理系统 - 后端启动脚本
echo ========================================
echo.

REM 检查Java是否安装
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 17 或更高版本
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/4] 检测到 Java 版本:
java -version
echo.

REM 检查Maven是否安装
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到 Maven，请先安装 Maven
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [2/4] 检测到 Maven 版本:
mvn --version | findstr "Apache Maven"
echo.

REM 检查Docker是否运行（MySQL和Redis）
echo [3/4] 检查数据库服务...
docker ps | findstr "news_mysql" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [警告] MySQL容器未运行
    echo 正在尝试启动数据库容器...
    echo.
    docker-compose up -d
    if %ERRORLEVEL% NEQ 0 (
        echo [错误] 数据库启动失败，请检查 Docker 是否正在运行
        echo 请手动执行: docker-compose up -d
        pause
        exit /b 1
    )
    echo [✓] 数据库服务已启动
    echo 等待数据库初始化（10秒）...
    timeout /t 10 /nobreak >nul
    echo.
) else (
    echo [✓] 数据库服务正在运行
    echo.
)

REM 切换到backend目录
cd backend
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 找不到 backend 目录
    pause
    exit /b 1
)

REM 启动后端服务
echo [4/4] 正在启动后端服务...
echo.
echo ========================================
echo  后端服务将在以下地址运行:
echo  http://localhost:8080/api
echo ========================================
echo.
echo 按 Ctrl+C 停止服务器
echo.

mvn spring-boot:run

pause

