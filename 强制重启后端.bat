@echo off
chcp 65001 >nul
echo ========================================
echo    🔥 强制重启后端服务
echo ========================================
echo.

echo [1/3] 停止所有Java进程（包括后端）...
taskkill /F /IM java.exe >nul 2>&1
echo     ✓ 已停止所有Java进程

echo.
echo [2/3] 等待端口释放...
timeout /t 3 /nobreak >nul
echo     ✓ 端口已释放

echo.
echo [3/3] 编译并启动后端...
cd /d "%~dp0backend"
start "后端服务 - 新进程" cmd /k "mvn clean compile spring-boot:run"

echo.
echo ========================================
echo ✅ 后端服务已强制重启！
echo.
echo 📌 请等待30秒让后端完全启动
echo 📌 看到 "Started NewsManagementApplication" 表示启动成功
echo ========================================
echo.
pause

