-- V5__add_updated_at_to_categories.sql
-- 为categories表添加updated_at字段

ALTER TABLE categories 
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 说明：此字段用于跟踪分类的最后更新时间

