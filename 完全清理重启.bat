@echo off
chcp 65001 >nul
echo ========================================
echo    🧹 完全清理并重启后端
echo ========================================
echo.

echo [步骤1/5] 停止所有Java进程...
taskkill /F /IM java.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo     ✓ 已停止Java进程
) else (
    echo     ℹ️ 没有运行中的Java进程
)
timeout /t 2 /nobreak >nul

echo.
echo [步骤2/5] 清理编译缓存...
cd /d "%~dp0backend"
if exist "target" (
    rmdir /s /q target >nul 2>&1
    echo     ✓ 已删除 target 目录
) else (
    echo     ℹ️ target 目录不存在
)

echo.
echo [步骤3/5] 检查配置文件...
findstr /C:"implements WebMvcConfigurer" "src\main\java\com\news\config\FileUploadConfig.java" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo     ❌ 警告：FileUploadConfig 仍然实现 WebMvcConfigurer
    echo     请确认代码已保存！
) else (
    echo     ✓ FileUploadConfig 配置正确
)

echo.
echo [步骤4/5] 重新编译项目...
echo     这可能需要1-2分钟...
call mvn clean compile -DskipTests -q
if %ERRORLEVEL% EQU 0 (
    echo     ✓ 编译成功
) else (
    echo     ❌ 编译失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo [步骤5/5] 启动后端服务...
start "新后端服务" cmd /k "mvn spring-boot:run"

echo.
echo ========================================
echo ✅ 完全清理重启完成！
echo ========================================
echo.
echo 📌 请等待30秒让后端启动
echo 📌 在新窗口中看到 "Started NewsManagementApplication" 表示成功
echo 📌 如果还是有问题，请复制后端窗口中的错误信息
echo.
pause

