@echo off
chcp 65001 >nul
echo ========================================
echo    新闻管理系统 - 停止所有服务
echo ========================================
echo.

echo 正在停止服务...
echo.

REM 停止Spring Boot进程
echo [1/3] 停止后端服务...
taskkill /F /FI "WINDOWTITLE eq 新闻系统-后端*" >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [✓] 后端服务已停止
) else (
    echo [提示] 后端服务未在运行
)

REM 停止Node进程
echo [2/3] 停止前端服务...
taskkill /F /FI "WINDOWTITLE eq 新闻系统-前端*" >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [✓] 前端服务已停止
) else (
    echo [提示] 前端服务未在运行
)

REM 停止Docker容器
echo [3/3] 停止数据库服务...
docker-compose down >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [✓] 数据库服务已停止
) else (
    echo [提示] 数据库服务未在运行
)

echo.
echo ========================================
echo  ✓ 所有服务已停止
echo ========================================
echo.

pause

