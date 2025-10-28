-- V13: 更新审计日志表结构
-- 使审计日志表与AuditLog实体类匹配

-- 1. 添加缺失的列（先允许NULL）
ALTER TABLE audit_logs
ADD COLUMN operator_username VARCHAR(50) AFTER operator_id,
ADD COLUMN operation_details TEXT AFTER target_entity_id,
ADD COLUMN user_agent VARCHAR(500) AFTER ip_address;

-- 2. 更新现有记录的 operator_username（从users表获取）
UPDATE audit_logs al
INNER JOIN users u ON al.operator_id = u.id
SET al.operator_username = u.username;

-- 3. 将 operator_username 改为 NOT NULL（设置默认值'unknown'给任何仍为NULL的记录）
UPDATE audit_logs SET operator_username = 'unknown' WHERE operator_username IS NULL;
ALTER TABLE audit_logs MODIFY COLUMN operator_username VARCHAR(50) NOT NULL;

-- 4. 更新列的注释
ALTER TABLE audit_logs
MODIFY COLUMN operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE, VIEW, LOGIN, LOGOUT, REVIEW, PUBLISH, ARCHIVE',
MODIFY COLUMN target_entity VARCHAR(50) NOT NULL COMMENT '目标实体：NEWS, CATEGORY, USER, COMMENT等',
MODIFY COLUMN target_entity_id BIGINT NOT NULL COMMENT '目标实体ID',
MODIFY COLUMN operator_id BIGINT NOT NULL COMMENT '操作者ID',
MODIFY COLUMN operator_username VARCHAR(50) NOT NULL COMMENT '操作者用户名',
MODIFY COLUMN operation_details TEXT COMMENT '操作详情（JSON格式）',
MODIFY COLUMN ip_address VARCHAR(50) COMMENT 'IP地址',
MODIFY COLUMN user_agent VARCHAR(500) COMMENT 'User-Agent',
MODIFY COLUMN operation_time DATETIME NOT NULL COMMENT '操作时间';

-- 5. 更新索引（如果需要）
-- 已有的索引保持不变

