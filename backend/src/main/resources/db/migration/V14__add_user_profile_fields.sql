-- V14__add_user_profile_fields.sql
-- 为用户表添加个人资料相关字段

-- 添加头像和电话字段
ALTER TABLE users 
ADD COLUMN avatar_url VARCHAR(500) COMMENT '用户头像URL',
ADD COLUMN phone VARCHAR(20) COMMENT '联系电话';

-- 为现有管理员添加默认信息
UPDATE users SET 
    full_name = '系统管理员',
    avatar_url = NULL
WHERE username = 'admin' AND full_name IS NULL;

UPDATE users SET 
    full_name = '系统编辑员',
    avatar_url = NULL
WHERE username = 'editor' AND full_name IS NULL;

