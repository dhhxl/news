-- V7__add_summary_fields.sql
-- 为summaries表添加缺失的字段

-- 添加model_version字段
ALTER TABLE summaries ADD COLUMN model_version VARCHAR(50) DEFAULT 'glm-4';

-- 添加status字段
ALTER TABLE summaries ADD COLUMN status VARCHAR(20) DEFAULT 'SUCCESS';

-- 为现有数据设置默认值
UPDATE summaries SET model_version = 'glm-4' WHERE model_version IS NULL;
UPDATE summaries SET status = 'SUCCESS' WHERE status IS NULL;

