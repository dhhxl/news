@echo off
chcp 65001 >nul
echo ========================================
echo    ⏳ 等待后端启动...
echo ========================================
echo.

set MAX_ATTEMPTS=30
set ATTEMPT=0

:CHECK_LOOP
set /a ATTEMPT+=1
echo [尝试 %ATTEMPT%/%MAX_ATTEMPTS%] 检查后端是否就绪...

curl -s http://localhost:8080/api/public/test >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 后端启动成功！
    echo ========================================
    echo.
    echo 后端地址: http://localhost:8080
    echo 前端地址: http://localhost:5173
    echo.
    echo 现在可以测试了！
    echo.
    pause
    exit /b 0
)

if %ATTEMPT% LSS %MAX_ATTEMPTS% (
    timeout /t 2 /nobreak >nul
    goto CHECK_LOOP
)

echo.
echo ========================================
echo ⚠️ 后端启动超时
echo ========================================
echo.
echo 请检查后端窗口中的错误信息
echo.
pause
exit /b 1

