-- V10__add_image_url.sql
-- 为新闻表添加图片URL字段

ALTER TABLE news ADD COLUMN image_url VARCHAR(500) COMMENT '新闻配图URL';

-- 创建索引
CREATE INDEX idx_news_image_url ON news(image_url);

