@echo off
chcp 65001 > nul
echo ===================================
echo   修复 Flyway 迁移失败
echo ===================================
echo.

echo 正在连接到数据库并清理失败的V13迁移记录...
echo.

mysql -u newsadmin -p123456 -D news_management_db -e "DELETE FROM flyway_schema_history WHERE version = '13' AND success = 0; SELECT * FROM flyway_schema_history ORDER BY installed_rank;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 失败的迁移记录已清理完成！
    echo.
    echo 现在可以重新启动后端服务了：
    echo   cd backend
    echo   mvn spring-boot:run
) else (
    echo.
    echo ❌ 清理失败！请检查数据库连接。
    echo.
    echo 如果密码不是 123456，请手动执行：
    echo   mysql -u newsadmin -p -D news_management_db
    echo   DELETE FROM flyway_schema_history WHERE version = '13' AND success = 0;
)

echo.
pause

