# Data Model: 新闻管理系统

**Date**: 2025-10-10  
**Database**: MySQL 8.0  
**ORM**: Spring Data JPA + MyBatis-Plus

## Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────────┐         ┌───────────────┐
│    User      │         │       News       │         │   Category    │
├──────────────┤         ├──────────────────┤         ├───────────────┤
│ id (PK)      │         │ id (PK)          │◄───────┤ id (PK)       │
│ username     │─────┐   │ title            │         │ name          │
│ password     │     │   │ content          │         │ description   │
│ role         │     │   │ source_name      │         │ is_default    │
│ created_at   │     │   │ source_url       │         │ created_at    │
│ last_login   │     │   │ category_id (FK) │         │ created_by    │
└──────────────┘     │   │ publish_time     │         └───────────────┘
                     │   │ crawled_at       │                │
                     │   │ created_by (FK)  │                │
┌──────────────┐     │   │ status           │                │
│  AuditLog    │     │   │ classification   │                │
├──────────────┤     │   │ view_count       │                │
│ id (PK)      │     └──►│ created_at       │                │
│ user_id (FK) │─────────┤ updated_at       │                │
│ action       │         └──────────────────┘                │
│ target_type  │                  │                          │
│ target_id    │                  │                          │
│ old_value    │         ┌────────▼──────────┐               │
│ new_value    │         │     Summary       │               │
│ created_at   │         ├───────────────────┤               │
└──────────────┘         │ id (PK)           │               │
                         │ news_id (FK)      │               │
                         │ content           │               │
┌──────────────────┐     │ generated_at      │               │
│ ClassificationRule│     └───────────────────┘               │
├──────────────────┤                                         │
│ id (PK)          │                                         │
│ rule_type        │     ┌───────────────────┐               │
│ source_pattern   │     │    CrawlTask      │               │
│ keywords         │     ├───────────────────┤               │
│ category_id (FK) │─────┤ id (PK)           │               │
│ priority         │     │ source_name       │               │
│ enabled          │     │ start_time        │               │
│ created_at       │     │ end_time          │               │
│ created_by       │     │ status            │               │
└──────────────────┘     │ news_count        │               │
                         │ error_message     │               │
                         └───────────────────┘               │
```

---

## Table Definitions

### 1. users (用户表)

**Purpose**: 存储管理员和普通用户信息

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| role | ENUM('ADMIN', 'USER') | NOT NULL, DEFAULT 'USER' | 角色 |
| email | VARCHAR(100) | UNIQUE | 邮箱（可选） |
| avatar_url | VARCHAR(500) | NULL | 头像URL（可选） |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NULL, ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| last_login_at | TIMESTAMP | NULL | 最后登录时间 |

**Indexes**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_username` (`username`)
- INDEX `idx_role` (`role`)

**Validation Rules**:
- `username`: 4-50字符，字母数字下划线
- `password`: 存储前必须BCrypt加密，至少8字符
- `role`: 只能是ADMIN或USER

**Initial Data**:
```sql
-- 默认管理员账户（密码: admin123，实际应使用BCrypt）
INSERT INTO users (username, password, role) 
VALUES ('admin', '$2a$10$...bcrypt_hash...', 'ADMIN');
```

---

### 2. categories (新闻分类表)

**Purpose**: 存储新闻分类信息

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 分类ID |
| name | VARCHAR(50) | UNIQUE, NOT NULL | 分类名称 |
| description | VARCHAR(200) | NULL | 分类描述 |
| is_default | TINYINT(1) | NOT NULL, DEFAULT 0 | 是否为系统默认分类 |
| display_order | INT | NOT NULL, DEFAULT 0 | 显示顺序 |
| icon | VARCHAR(100) | NULL | 图标标识（可选） |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| created_by | BIGINT | FOREIGN KEY → users(id) | 创建人 |
| updated_at | TIMESTAMP | NULL, ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**Indexes**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_name` (`name`)
- INDEX `idx_is_default` (`is_default`)
- INDEX `idx_created_by` (`created_by`)

**Validation Rules**:
- `name`: 2-50字符，不能为空
- `is_default`: 默认分类不能被删除

**Initial Data**:
```sql
-- 系统预置的6个默认分类
INSERT INTO categories (name, description, is_default, display_order) VALUES
('政治', '政治新闻', 1, 1),
('经济', '经济财经新闻', 1, 2),
('体育', '体育赛事新闻', 1, 3),
('娱乐', '娱乐明星新闻', 1, 4),
('科技', '科技创新新闻', 1, 5),
('社会', '社会民生新闻', 1, 6);
```

---

### 3. news (新闻表)

**Purpose**: 存储新闻内容主体信息

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 新闻ID |
| title | VARCHAR(200) | NOT NULL | 新闻标题 |
| content | TEXT | NOT NULL | 新闻正文 |
| summary | TEXT | NULL | 新闻摘要（可选，用于列表显示） |
| source_name | VARCHAR(50) | NOT NULL | 来源网站名称 |
| source_url | VARCHAR(500) | NOT NULL | 原始URL |
| image_url | VARCHAR(500) | NULL | 封面图片URL |
| category_id | BIGINT | FOREIGN KEY → categories(id), NOT NULL | 分类ID |
| publish_time | TIMESTAMP | NOT NULL | 发布时间 |
| crawled_at | TIMESTAMP | NULL | 采集时间（NULL表示手动创建） |
| created_by | BIGINT | FOREIGN KEY → users(id) | 创建人 |
| status | ENUM('DRAFT', 'PUBLISHED') | NOT NULL, DEFAULT 'PUBLISHED' | 状态 |
| classification_method | ENUM('MANUAL', 'AUTO') | NOT NULL, DEFAULT 'MANUAL' | 分类方式 |
| view_count | BIGINT | NOT NULL, DEFAULT 0 | 浏览量 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NULL, ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**Indexes**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_title` (`title`) -- 用于重复检测
- INDEX `idx_category` (`category_id`)
- INDEX `idx_status_publish_view` (`status`, `publish_time` DESC, `view_count` DESC) -- 智能排序组合索引
- INDEX `idx_source_name` (`source_name`)
- INDEX `idx_created_by` (`created_by`)
- FULLTEXT INDEX `ft_title_content` (`title`, `content`) -- 全文搜索（可选）

**Validation Rules**:
- `title`: 5-200字符，不能为空，不能重复
- `content`: 至少50字符，不能为空
- `source_url`: 合法URL格式
- `status`: 自动采集的新闻默认为PUBLISHED，手动创建可为DRAFT

**Lifecycle**:
```
DRAFT (草稿) → PUBLISHED (已发布)
               ↓
            (软删除：status='DELETED')
```

---

### 4. summaries (AI摘要表)

**Purpose**: 存储AI生成的新闻摘要

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 摘要ID |
| news_id | BIGINT | FOREIGN KEY → news(id), UNIQUE | 新闻ID |
| content | TEXT | NOT NULL | 摘要内容 |
| generated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 生成时间 |
| model_version | VARCHAR(50) | NULL | AI模型版本（如glm-4） |
| token_count | INT | NULL | 消耗的Token数 |

**Indexes**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_news_id` (`news_id`) -- 一条新闻只有一个摘要

**Validation Rules**:
- `content`: 10-500字符
- `news_id`: 必须是有效的新闻ID

**Cascade Delete**: 当新闻被删除时，级联删除对应摘要

---

### 5. classification_rules (分类规则表)

**Purpose**: 存储自动分类规则配置

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 规则ID |
| rule_type | ENUM('SOURCE', 'KEYWORD') | NOT NULL | 规则类型 |
| source_pattern | VARCHAR(200) | NULL | 来源URL模式（如'cctv.com/politics'） |
| keywords | TEXT | NULL | 关键词列表（JSON格式，如["经济","财经"]） |
| category_id | BIGINT | FOREIGN KEY → categories(id), NOT NULL | 目标分类ID |
| priority | INT | NOT NULL, DEFAULT 0 | 优先级（越大越优先） |
| enabled | TINYINT(1) | NOT NULL, DEFAULT 1 | 是否启用 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| created_by | BIGINT | FOREIGN KEY → users(id) | 创建人 |
| updated_at | TIMESTAMP | NULL, ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**Indexes**:
- PRIMARY KEY (`id`)
- INDEX `idx_type_enabled_priority` (`rule_type`, `enabled`, `priority` DESC) -- 规则查询优化
- INDEX `idx_category` (`category_id`)

**Validation Rules**:
- `rule_type` = 'SOURCE' 时，`source_pattern` 不能为空
- `rule_type` = 'KEYWORD' 时，`keywords` 不能为空
- `keywords`: 存储为JSON数组，如 `["关键词1", "关键词2"]`

**Business Logic**:
```
优先级: SOURCE规则 > KEYWORD规则
匹配策略: 
- SOURCE: source_url LIKE CONCAT('%', source_pattern, '%')
- KEYWORD: title或content包含任一关键词
```

---

### 6. crawl_tasks (采集任务表)

**Purpose**: 记录新闻采集任务的执行历史

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 任务ID |
| task_uuid | VARCHAR(36) | UNIQUE, NOT NULL | 任务唯一标识（UUID） |
| source_name | VARCHAR(50) | NOT NULL | 来源名称（新浪/央视/网易） |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NULL | 结束时间 |
| status | ENUM('RUNNING', 'SUCCESS', 'FAILED') | NOT NULL | 任务状态 |
| news_count | INT | NOT NULL, DEFAULT 0 | 成功采集新闻数 |
| error_message | TEXT | NULL | 错误信息 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**Indexes**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_task_uuid` (`task_uuid`)
- INDEX `idx_source_status` (`source_name`, `status`)
- INDEX `idx_created_at` (`created_at` DESC) -- 按时间查询任务历史

**Validation Rules**:
- `source_name`: 只能是'新浪'/'央视'/'网易'
- `status`: RUNNING → SUCCESS/FAILED
- `news_count`: >= 0

**Retention Policy**: 保留最近30天的任务记录，自动清理历史数据

---

### 7. audit_logs (审计日志表)

**Purpose**: 记录管理员的所有操作行为

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 日志ID |
| user_id | BIGINT | FOREIGN KEY → users(id) | 操作人ID |
| username | VARCHAR(50) | NOT NULL | 操作人用户名（冗余字段） |
| action | ENUM('CREATE', 'UPDATE', 'DELETE') | NOT NULL | 操作类型 |
| target_type | VARCHAR(50) | NOT NULL | 目标对象类型（News/Category/etc） |
| target_id | BIGINT | NOT NULL | 目标对象ID |
| old_value | JSON | NULL | 操作前的数据快照 |
| new_value | JSON | NULL | 操作后的数据快照 |
| ip_address | VARCHAR(50) | NULL | 操作IP地址 |
| user_agent | VARCHAR(500) | NULL | 浏览器User-Agent |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 操作时间 |

**Indexes**:
- PRIMARY KEY (`id`)
- INDEX `idx_user_id` (`user_id`)
- INDEX `idx_target` (`target_type`, `target_id`)
- INDEX `idx_created_at` (`created_at` DESC)

**Validation Rules**:
- `target_type`: 必须是合法的实体类型
- `old_value`/`new_value`: 存储为JSON格式，便于比对

**Retention Policy**: 永久保留审计日志，用于合规审查

---

## Database Schema SQL

完整的数据库建表SQL脚本位于：
`backend/src/main/resources/db/migration/V1__init_schema.sql`

---

## Data Migration Strategy

使用 **Flyway** 管理数据库版本：

```
db/migration/
├── V1__init_schema.sql              # 初始化表结构
├── V2__add_default_categories.sql   # 插入默认分类
├── V3__add_default_admin.sql        # 插入默认管理员
└── V4__add_indexes.sql              # 性能优化索引
```

**Migration Naming Convention**:
- `V{version}__{description}.sql`
- Version使用递增整数
- Description使用小写英文，单词间下划线

---

## Query Optimization Guidelines

### 1. 智能排序查询优化

```sql
-- 使用组合索引 idx_status_publish_view
SELECT id, title, summary, view_count, publish_time
FROM news
WHERE status = 'PUBLISHED'
ORDER BY (view_count * 0.3 + EXP(-0.1 * TIMESTAMPDIFF(HOUR, publish_time, NOW()))) DESC
LIMIT 20;

-- 避免SELECT *，只查询需要的字段
-- 使用LIMIT限制结果集大小
```

### 2. 分类新闻查询

```sql
-- 使用category_id索引
SELECT n.*, c.name as category_name
FROM news n
INNER JOIN categories c ON n.category_id = c.id
WHERE n.status = 'PUBLISHED' AND n.category_id = ?
ORDER BY n.publish_time DESC
LIMIT ? OFFSET ?;
```

### 3. 重复检测查询

```sql
-- 使用uk_title唯一索引
SELECT COUNT(*) FROM news WHERE title = ?;

-- 批量检测
SELECT title FROM news WHERE title IN (?, ?, ?);
```

### 4. 全文搜索（可选）

```sql
-- 使用全文索引 ft_title_content
SELECT * FROM news
WHERE MATCH(title, content) AGAINST(? IN NATURAL LANGUAGE MODE)
AND status = 'PUBLISHED'
LIMIT 20;
```

---

## Backup Strategy

1. **Daily Full Backup**: 每天凌晨2点执行完整数据库备份
2. **Binlog Archive**: 保留最近7天的Binlog，支持PITR（Point-In-Time Recovery）
3. **Backup Retention**: 每日备份保留30天，每月备份保留1年
4. **Disaster Recovery**: 异地备份到云存储（如OSS/S3）

---

## Performance Benchmarks

**Expected Performance** (基于100万条新闻数据):

| Operation | Expected Response Time |
|-----------|------------------------|
| 首页新闻列表查询 | < 50ms（带缓存）/ < 150ms（无缓存） |
| 新闻详情查询 | < 20ms |
| 新闻创建 | < 100ms |
| 新闻更新 | < 100ms |
| 全文搜索 | < 200ms |
| 分类规则匹配 | < 10ms（内存操作） |

---

## Next Steps

数据模型设计完成，下一步：
1. 定义RESTful API契约（contracts/）
2. 编写数据库迁移脚本
3. 实现JPA Entity类和Repository接口

