@echo off
chcp 65001 >nul
echo ========================================
echo    🔄 重启后端服务
echo ========================================
echo.

echo [1/2] 停止现有后端进程...
taskkill /F /FI "WINDOWTITLE eq 后端服务*" >nul 2>&1
timeout /t 2 /nobreak >nul

echo [2/2] 启动后端服务...
start "后端服务" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo.
echo ✅ 后端服务已启动！
echo.
pause

