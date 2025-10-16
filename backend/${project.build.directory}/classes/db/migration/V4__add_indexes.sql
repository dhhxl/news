-- V4__add_indexes.sql
-- 添加性能优化索引

-- 注意：news表的智能排序索引已在V1中创建：idx_status_publish_view

-- 用于按分类和状态筛选
CREATE INDEX idx_news_category_filter ON news(category_id, status, created_by);

-- 优化分类规则查询
CREATE INDEX idx_classification_priority ON classification_rules(is_enabled, priority, rule_type);

-- 优化采集任务查询
CREATE INDEX idx_crawl_task_status ON crawl_tasks(status, target_source, start_time DESC);

-- 优化审计日志查询
CREATE INDEX idx_audit_entity_time ON audit_logs(target_entity, target_entity_id, operation_time DESC);
CREATE INDEX idx_audit_operation ON audit_logs(operation_type, operation_time DESC);

-- 添加分类表的外键索引
CREATE INDEX idx_category_created_by ON categories(created_by);

-- 说明：这些索引将显著提升以下查询性能：
-- 1. 新闻列表的智能排序（时间+热度）
-- 2. 按分类筛选新闻
-- 3. 分类规则的优先级匹配
-- 4. 采集任务的状态查询
-- 5. 审计日志的追踪查询

