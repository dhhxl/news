@echo off
chcp 65001 >nul
echo ========================================
echo    检查图片和新闻状态
echo ========================================
echo.

echo [1] 新闻统计
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT COUNT(*) as '总新闻数', SUM(CASE WHEN image_urls IS NOT NULL THEN 1 ELSE 0 END) as '有图片的新闻' FROM news;"

echo.
echo [2] 图片统计
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT COUNT(*) as '总图片数', SUM(CASE WHEN is_used = 1 THEN 1 ELSE 0 END) as '已使用图片', SUM(CASE WHEN is_used = 1 AND news_id IS NOT NULL THEN 1 ELSE 0 END) as '已正确关联' FROM uploaded_images;"

echo.
echo [3] 最新上传的图片
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT id, LEFT(original_name, 30) as original_name, is_used, news_id FROM uploaded_images ORDER BY upload_time DESC LIMIT 5;"

echo.
echo [4] 最新的新闻（前3条）
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db -e "SELECT id, LEFT(title, 40) as title, CASE WHEN image_urls IS NULL THEN 'No' ELSE 'Yes' END as has_images FROM news ORDER BY id DESC LIMIT 3;"

echo.
echo [5] 检查图片文件
echo 本地uploads目录中的图片文件：
dir /b backend\uploads\*.png 2>nul | find /c ".png"
echo 个图片文件
echo.

pause

