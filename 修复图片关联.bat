@echo off
chcp 65001 >nul
echo ========================================
echo    修复图片关联问题
echo ========================================
echo.

echo [1/2] 清理未正确关联的图片记录...
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "UPDATE uploaded_images SET is_used = 0, news_id = NULL WHERE is_used = 1 AND news_id IS NULL;"

echo.
echo [✓] 已清理未正确关联的图片
echo.

echo [2/2] 检查当前状态...
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT COUNT(*) as '已使用但未关联的图片' FROM uploaded_images WHERE is_used = 1 AND news_id IS NULL;"

echo.
echo ========================================
echo  修复完成！
echo ========================================
echo.
echo 下一步：
echo 1. 重启后端服务
echo 2. 重新提交新闻（会自动重新关联图片）
echo.

pause

