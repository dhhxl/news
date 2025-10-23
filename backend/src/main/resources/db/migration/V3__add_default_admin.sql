-- V3__add_default_admin.sql
-- 插入默认管理员用户
-- username: admin
-- password: admin123 (BCrypt加密后)

INSERT INTO users (username, password_hash, role, created_at) VALUES
('admin', '$2a$10$D0CcxYAaJuwzkVr2JDdo9OBqifK/gM7i5MvZu4BVFd4NCESLuQqPa', 'ADMIN', CURRENT_TIMESTAMP);

-- 说明：
-- 默认管理员账户：
--   用户名: admin
--   密码: admin123
-- 注意：生产环境部署后应立即修改默认密码！

