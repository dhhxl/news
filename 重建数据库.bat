@echo off
chcp 65001 > nul
echo ===================================
echo   清理并重建数据库
echo ===================================
echo.
echo ⚠️  警告：此操作将删除所有数据！
echo.
pause

echo.
echo 步骤 1: 停止后端服务（如果正在运行）...
echo.

echo 步骤 2: 删除数据库...
docker exec news_mysql mysql -u root -proot123 -e "DROP DATABASE IF EXISTS news_management_db;"
if %ERRORLEVEL% EQU 0 (
    echo ✅ 数据库已删除
) else (
    echo ❌ 删除失败
    pause
    exit /b 1
)

echo.
echo 步骤 3: 重新创建数据库...
docker exec news_mysql mysql -u root -proot123 -e "CREATE DATABASE news_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %ERRORLEVEL% EQU 0 (
    echo ✅ 数据库已创建
) else (
    echo ❌ 创建失败
    pause
    exit /b 1
)

echo.
echo 步骤 4: 授予用户权限...
docker exec news_mysql mysql -u root -proot123 -e "GRANT ALL PRIVILEGES ON news_management_db.* TO 'newsadmin'@'%%' IDENTIFIED BY 'newspass123';"
docker exec news_mysql mysql -u root -proot123 -e "FLUSH PRIVILEGES;"
if %ERRORLEVEL% EQU 0 (
    echo ✅ 权限已设置
) else (
    echo ❌ 权限设置失败
    pause
    exit /b 1
)

echo.
echo 步骤 5: 清理编译缓存...
cd backend
call mvn clean
if %ERRORLEVEL% EQU 0 (
    echo ✅ 缓存已清理
) else (
    echo ❌ 清理失败
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ===================================
echo   ✅ 数据库重建完成！
echo ===================================
echo.
echo 现在可以启动后端服务了：
echo   cd backend
echo   mvn spring-boot:run
echo.
echo Flyway 将自动创建所有表结构和初始数据。
echo.
pause

