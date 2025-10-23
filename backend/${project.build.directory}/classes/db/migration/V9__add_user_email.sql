-- V9__add_user_email.sql
-- 为用户表添加邮箱字段

ALTER TABLE users ADD COLUMN email VARCHAR(100) UNIQUE COMMENT '用户邮箱';

