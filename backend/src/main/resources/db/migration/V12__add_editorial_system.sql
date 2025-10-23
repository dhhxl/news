-- V12: 添加编辑权限和审核系统
-- 扩展新闻状态，添加审核记录表，支持图片上传

-- 1. 扩展用户角色枚举（ADMIN, USER, EDITOR）
-- 注：User实体中的role字段已经是VARCHAR，可以直接使用

-- 2. 扩展新闻状态
-- 原有状态: PUBLISHED, DRAFT, ARCHIVED
-- 新增状态: PENDING(待审核), REJECTED(已退回), REVIEWING(审核中)

-- 3. 创建审核记录表
CREATE TABLE news_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL COMMENT '审核动作: SUBMIT, APPROVE, REJECT, REQUEST_CHANGES',
    status VARCHAR(20) NOT NULL COMMENT '审核后状态: PENDING, APPROVED, REJECTED',
    review_comment TEXT COMMENT '审核意见',
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_news_reviews_news_id (news_id),
    INDEX idx_news_reviews_reviewer_id (reviewer_id),
    INDEX idx_news_reviews_status (status),
    INDEX idx_news_reviews_reviewed_at (reviewed_at)
) COMMENT '新闻审核记录表';

-- 4. 创建上传图片表
CREATE TABLE uploaded_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    uploaded_by BIGINT NOT NULL COMMENT '上传用户ID',
    news_id BIGINT NULL COMMENT '关联的新闻ID(可为空)',
    upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_used BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已被使用',
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE SET NULL,
    
    INDEX idx_uploaded_images_uploaded_by (uploaded_by),
    INDEX idx_uploaded_images_news_id (news_id),
    INDEX idx_uploaded_images_upload_time (upload_time),
    INDEX idx_uploaded_images_is_used (is_used)
) COMMENT '上传图片记录表';

-- 5. 首先添加用户表缺少的字段
ALTER TABLE users 
ADD COLUMN full_name VARCHAR(100) COMMENT '全名',
ADD COLUMN is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 6. 添加编辑员用户（用于测试）
INSERT INTO users (username, password_hash, email, role, full_name, is_enabled, created_at, updated_at) 
VALUES ('editor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM.lbESZbOeo7TaioxfG', 'editor@example.com', 'EDITOR', '编辑员', TRUE, NOW(), NOW());
-- 密码是: editor123

-- 6. 为现有新闻状态添加注释说明
ALTER TABLE news MODIFY COLUMN status VARCHAR(20) NOT NULL COMMENT '状态：PUBLISHED-已发布, DRAFT-草稿, ARCHIVED-已归档, PENDING-待审核, REJECTED-已退回, REVIEWING-审核中';

-- 7. 添加新字段到新闻表
ALTER TABLE news 
ADD COLUMN submitted_at TIMESTAMP NULL COMMENT '提交审核时间',
ADD COLUMN submitted_by BIGINT NULL COMMENT '提交人ID',
ADD COLUMN current_reviewer BIGINT NULL COMMENT '当前审核人ID',
ADD COLUMN review_deadline TIMESTAMP NULL COMMENT '审核截止时间';

-- 添加外键约束
ALTER TABLE news 
ADD FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE SET NULL,
ADD FOREIGN KEY (current_reviewer) REFERENCES users(id) ON DELETE SET NULL;

-- 添加索引
ALTER TABLE news 
ADD INDEX idx_news_status_submitted_at (status, submitted_at),
ADD INDEX idx_news_submitted_by (submitted_by),
ADD INDEX idx_news_current_reviewer (current_reviewer);
