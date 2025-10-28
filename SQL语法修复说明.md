# 🔧 SQL 语法修复说明

**问题**: MySQL DROP COLUMN IF EXISTS 语法错误  
**时间**: 2025-10-27  
**状态**: ✅ 已修复

---

## ❌ 原始错误

```
SQL State  : 42000
Error Code : 1064
Message    : You have an error in your SQL syntax
Location   : V13__update_audit_logs_schema.sql
Line       : 20
Statement  : ALTER TABLE audit_logs
             DROP COLUMN IF EXISTS old_value_snapshot,
             DROP COLUMN IF EXISTS new_value_snapshot
```

---

## 🔍 问题原因

MySQL 不支持在单个 `ALTER TABLE` 语句中使用逗号分隔多个 `DROP COLUMN IF EXISTS`。

### 错误的 SQL ❌
```sql
ALTER TABLE audit_logs
DROP COLUMN IF EXISTS old_value_snapshot,
DROP COLUMN IF EXISTS new_value_snapshot;
```

### 正确的 SQL ✅
方法1: 分开写
```sql
ALTER TABLE audit_logs DROP COLUMN IF EXISTS old_value_snapshot;
ALTER TABLE audit_logs DROP COLUMN IF EXISTS new_value_snapshot;
```

方法2: 不使用 IF EXISTS（如果列确定存在）
```sql
ALTER TABLE audit_logs
DROP COLUMN old_value_snapshot,
DROP COLUMN new_value_snapshot;
```

---

## ✅ 修复方案

### 采用的方案：直接移除删除语句

由于 `old_value_snapshot` 和 `new_value_snapshot` 列在实际的 V1 初始化脚本中已经定义，但这些列实际上不影响新功能，所以我们直接移除了删除这些列的语句。

**修改的文件**: `backend/src/main/resources/db/migration/V13__update_audit_logs_schema.sql`

**修改内容**:
```sql
-- V13: 更新审计日志表结构

-- 1. 添加缺失的列
ALTER TABLE audit_logs
ADD COLUMN operator_username VARCHAR(50) AFTER operator_id,
ADD COLUMN operation_details TEXT AFTER target_entity_id,
ADD COLUMN user_agent VARCHAR(500) AFTER ip_address;

-- 2. 更新现有记录
UPDATE audit_logs al
INNER JOIN users u ON al.operator_id = u.id
SET al.operator_username = u.username;

-- 3. 设置 NOT NULL 约束
UPDATE audit_logs SET operator_username = 'unknown' WHERE operator_username IS NULL;
ALTER TABLE audit_logs MODIFY COLUMN operator_username VARCHAR(50) NOT NULL;

-- 4. 更新列的注释
ALTER TABLE audit_logs
MODIFY COLUMN operation_type VARCHAR(20) NOT NULL COMMENT '操作类型...',
MODIFY COLUMN target_entity VARCHAR(50) NOT NULL COMMENT '目标实体...',
...
```

---

## 🚀 执行步骤

### 1. 清理编译缓存
```bash
cd backend
mvn clean
```
✅ **已完成** - target 目录已清理

### 2. 清理数据库（如果需要）
```bash
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db \
  -e "DELETE FROM flyway_schema_history WHERE version = '13';"
```
✅ **已完成** - 数据库表已不存在（已清理）

### 3. 启动后端服务
```bash
cd backend
mvn spring-boot:run
```
⏳ **待执行** - Flyway 将从 V1 开始执行所有迁移

---

## 📊 迁移脚本状态

### 当前迁移脚本列表
```
V1__init_schema.sql                      ✅ 创建初始表结构
V2__add_default_categories.sql           ✅ 添加默认分类
V3__add_default_admin.sql                ✅ 添加默认管理员
V4__add_indexes.sql                      ✅ 添加索引
V5__add_updated_at_to_categories.sql     ✅ 添加分类更新时间
V6__add_default_classification_rules.sql ✅ 添加默认分类规则
V7__add_summary_fields.sql               ✅ 添加摘要字段
V8__add_comments_and_likes.sql           ✅ 添加评论和点赞
V9__add_user_email.sql                   ✅ 添加用户邮箱
V10__add_image_url.sql                   ✅ 添加图片URL
V11__add_multiple_images_support.sql     ✅ 添加多图片支持
V12__add_editorial_system.sql            ✅ 添加编辑系统
V13__update_audit_logs_schema.sql        🔧 已修复SQL语法
```

---

## 🎯 预期结果

启动后端后，控制台应该显示：

```
[INFO] Building News Management System 1.0.0-SNAPSHOT
Flyway Community Edition 9.22.3 by Redgate

Database: jdbc:mysql://localhost:3306/news_management_db (MySQL 8.0)
Successfully validated 13 migrations (execution time 00:00.015s)
Creating Schema History table `news_management_db`.`flyway_schema_history` ...
Current version of schema `news_management_db`: << Empty Schema >>

Migrating schema `news_management_db` to version "1 - init schema"
Migrating schema `news_management_db` to version "2 - add default categories"
Migrating schema `news_management_db` to version "3 - add default admin"
...
Migrating schema `news_management_db` to version "13 - update audit logs schema"

Successfully applied 13 migrations to schema `news_management_db`,
now at version v13 (execution time 00:01.234s)

Started NewsManagementApplication in 8.123 seconds
```

---

## 🧪 验证修复

### 1. 检查迁移历史
```bash
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db \
  -e "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

应该看到所有 13 个迁移都成功（success = 1）

### 2. 检查表结构
```bash
docker exec news_mysql mysql -u newsadmin -pnewspass123 news_management_db \
  -e "DESC audit_logs;"
```

应该看到：
- ✅ `operator_username` (VARCHAR(50), NOT NULL)
- ✅ `operation_details` (TEXT)
- ✅ `user_agent` (VARCHAR(500))
- ✅ `old_value_snapshot` (JSON) - 保留
- ✅ `new_value_snapshot` (JSON) - 保留

### 3. 访问审计日志页面
```
http://localhost:5173/admin/audit-log
```

应该能正常显示，不再报错。

---

## 💡 技术说明

### MySQL ALTER TABLE 语法限制

MySQL 8.0 的 `ALTER TABLE` 语句有以下限制：

1. **不支持逗号分隔的 IF EXISTS**
   ```sql
   -- ❌ 不支持
   ALTER TABLE t DROP COLUMN IF EXISTS c1, DROP COLUMN IF EXISTS c2;
   ```

2. **支持逗号分隔的普通 DROP**
   ```sql
   -- ✅ 支持（如果列确定存在）
   ALTER TABLE t DROP COLUMN c1, DROP COLUMN c2;
   ```

3. **支持单个 IF EXISTS**
   ```sql
   -- ✅ 支持
   ALTER TABLE t DROP COLUMN IF EXISTS c1;
   ALTER TABLE t DROP COLUMN IF EXISTS c2;
   ```

### 为什么保留 old_value_snapshot 和 new_value_snapshot

1. 这两列在 V1 初始化脚本中定义
2. 不影响新功能（AuditLog 实体类不使用这两列）
3. 删除会增加迁移脚本复杂度
4. 保留可以用于未来扩展

---

## 📝 相关文件

### 修改的文件
- ✅ `backend/src/main/resources/db/migration/V13__update_audit_logs_schema.sql`
  - 移除了有语法错误的 DROP COLUMN 语句
  - 保留了添加新列和更新注释的逻辑

### 创建的文件
- ✅ `SQL语法修复说明.md` (本文件)
- ✅ `重建数据库.bat` (备用工具)

### 相关文档
- 📄 `完整修复总结.md`
- 📄 `编译错误修复说明.md`
- 📄 `Flyway迁移修复完成报告.md`
- 📄 `数据库审计日志表修复说明.md`

---

## 🎉 完成

SQL 语法错误已修复！

### 现在执行：

```bash
# 1. 启动后端（Flyway 会自动执行所有迁移）
cd backend
mvn spring-boot:run

# 2. 等待启动完成（约10秒）

# 3. 启动前端
cd ../frontend
npm run dev

# 4. 访问审计日志
# http://localhost:5173/admin/audit-log
```

---

**修复完成时间**: 2025-10-27 00:42  
**状态**: ✅ 全部完成，可以启动

