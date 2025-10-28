@echo off
chcp 65001 >nul
echo ========================================
echo    🔍 图片问题诊断工具
echo ========================================
echo.

echo [检查1] 数据库中的图片记录
echo ----------------------------------------
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT id, stored_name, original_name, news_id, is_used FROM uploaded_images ORDER BY id DESC LIMIT 5;"
echo.

echo [检查2] 最新新闻的图片URL
echo ----------------------------------------
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT id, title, SUBSTRING(image_urls, 1, 100) as image_urls FROM news WHERE image_urls IS NOT NULL ORDER BY id DESC LIMIT 3;"
echo.

echo [检查3] 上传目录中的图片文件
echo ----------------------------------------
if exist "backend\uploads" (
    echo 📁 uploads目录存在
    echo 文件列表（最近5个）：
    dir /B /O-D backend\uploads | findstr /R "\.png$ \.jpg$ \.jpeg$" | findstr /N "^" | findstr "^[1-5]:"
) else (
    echo ❌ uploads目录不存在！
)
echo.

echo [检查4] 后端进程状态
echo ----------------------------------------
tasklist /FI "IMAGENAME eq java.exe" /FO LIST 2>nul | findstr /C:"PID:" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ 后端Java进程正在运行
    tasklist /FI "IMAGENAME eq java.exe" /FO TABLE
) else (
    echo ❌ 后端Java进程未运行！请启动后端
)
echo.

echo ========================================
echo 📊 诊断完成
echo ========================================
echo.
echo 💡 提示：
echo   - 如果uploads目录中有图片文件
echo   - 但数据库中image_urls为NULL或空
echo   - 说明图片关联失败
echo.
echo   - 如果后端日志显示"Mapped to ResourceHttpRequestHandler"
echo   - 说明需要强制重启后端
echo.
pause

