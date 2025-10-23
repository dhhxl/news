-- V11: 添加多张图片支持
-- 为新闻表添加图片URL数组字段，支持存储多张图片

-- 添加新字段存储多张图片URL（JSON格式）
ALTER TABLE news ADD COLUMN image_urls TEXT COMMENT '图片URL数组(JSON格式)';

-- 为新增字段添加注释
ALTER TABLE news MODIFY COLUMN image_url VARCHAR(500) COMMENT '主图片URL（用于向后兼容和列表显示）';
ALTER TABLE news MODIFY COLUMN image_urls TEXT COMMENT '所有图片URL数组（JSON格式），例如：["url1", "url2", "url3"]';

-- 添加索引以提高查询性能（如果需要根据图片搜索）
-- 注：MySQL 8.0+ 支持JSON字段索引
-- ALTER TABLE news ADD INDEX idx_news_image_urls ((CAST(image_urls AS JSON)));
