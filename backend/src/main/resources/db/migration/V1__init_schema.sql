-- V1__init_schema.sql
-- 创建所有核心数据表

-- 1. 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 新闻分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    INDEX idx_name (name),
    INDEX idx_is_default (is_default),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 分类规则表
CREATE TABLE classification_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type VARCHAR(20) NOT NULL,
    source_pattern VARCHAR(255),
    keywords TEXT,
    target_category_id BIGINT NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    INDEX idx_rule_priority (priority, is_enabled),
    INDEX idx_target_category (target_category_id),
    FOREIGN KEY (target_category_id) REFERENCES categories(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 新闻表
CREATE TABLE news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    content LONGTEXT NOT NULL,
    source_website VARCHAR(100) NOT NULL,
    original_url VARCHAR(500) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    publish_time DATETIME NOT NULL,
    crawl_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    classification_method VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    view_count BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_title (title),
    INDEX idx_original_url (original_url),
    INDEX idx_category_status_publish (category_id, status, publish_time DESC),
    INDEX idx_status_publish_view (status, publish_time DESC, view_count DESC),
    INDEX idx_source_website (source_website),
    FULLTEXT INDEX idx_fulltext_title (title),
    FULLTEXT INDEX idx_fulltext_content (content),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. AI摘要表
CREATE TABLE summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_id BIGINT NOT NULL UNIQUE,
    summary_content TEXT NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_news_id (news_id),
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 采集任务表
CREATE TABLE crawl_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_source VARCHAR(100) NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    status VARCHAR(20) NOT NULL,
    success_count INT NOT NULL DEFAULT 0,
    fail_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_start_time (status, start_time DESC),
    INDEX idx_target_source (target_source),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 审计日志表
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    target_entity VARCHAR(50) NOT NULL,
    target_entity_id BIGINT NOT NULL,
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_value_snapshot JSON,
    new_value_snapshot JSON,
    ip_address VARCHAR(45),
    INDEX idx_operator_time (operator_id, operation_time DESC),
    INDEX idx_entity_id (target_entity, target_entity_id),
    INDEX idx_operation_time (operation_time DESC),
    FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

