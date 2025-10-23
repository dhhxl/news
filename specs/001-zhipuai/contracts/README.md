# API Contracts Overview

本目录包含新闻管理系统所有RESTful API的OpenAPI 3.0规范文档。

## 契约文件列表

### 1. auth-api.yaml - 认证授权API ✅ 已完成
**Endpoints**:
- `POST /api/auth/login` - 管理员登录
- `POST /api/auth/logout` - 登出
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/auth/refresh` - 刷新Token

**核心功能**: JWT Token认证、会话管理

---

### 2. news-api.yaml - 新闻管理API ✅ 已完成
**Endpoints**:
- `GET /api/news` - 获取新闻列表（用户端，智能排序）
- `GET /api/news/{id}` - 获取新闻详情（自动增加浏览量）
- `GET /api/admin/news` - 获取新闻列表（管理后台，支持高级筛选）
- `POST /api/admin/news` - 创建新闻
- `PUT /api/admin/news/{id}` - 更新新闻
- `DELETE /api/admin/news/{id}` - 删除新闻
- `POST /api/admin/news/{id}/publish` - 发布新闻
- `POST /api/admin/news/batch-delete` - 批量删除

**核心功能**: 新闻CRUD、智能排序、搜索筛选

---

### 3. category-api.yaml - 分类管理API（规划中）
**Endpoints**:
- `GET /api/categories` - 获取所有分类
- `POST /api/admin/categories` - 创建分类
- `PUT /api/admin/categories/{id}` - 更新分类
- `DELETE /api/admin/categories/{id}` - 删除分类

**核心功能**: 分类管理、默认分类保护

**快速实现**:
```yaml
# category-api.yaml
paths:
  /categories:
    get:
      summary: 获取分类列表
      responses:
        '200':
          content:
            application/json:
              schema:
                type: object
                properties:
                  code: {type: integer}
                  data:
                    type: array
                    items:
                      type: object
                      properties:
                        id: {type: integer}
                        name: {type: string}
                        description: {type: string}
                        isDefault: {type: boolean}
```

---

### 4. crawler-api.yaml - 采集任务API（规划中）
**Endpoints**:
- `GET /api/admin/crawl-tasks` - 获取采集任务历史
- `POST /api/admin/crawl-tasks/trigger` - 手动触发采集任务
- `GET /api/admin/crawl-tasks/{id}` - 获取任务详情
- `PUT /api/admin/settings/crawler` - 配置采集频率

**核心功能**: 任务触发、历史查询、频率配置

---

### 5. classification-api.yaml - 分类规则API（规划中）
**Endpoints**:
- `GET /api/admin/classification-rules` - 获取分类规则列表
- `POST /api/admin/classification-rules` - 创建分类规则
- `PUT /api/admin/classification-rules/{id}` - 更新规则
- `DELETE /api/admin/classification-rules/{id}` - 删除规则
- `PUT /api/admin/classification-rules/{id}/priority` - 调整优先级
- `POST /api/admin/classification-rules/{id}/toggle` - 启用/禁用规则

**核心功能**: 来源规则、关键词规则、优先级管理

---

### 6. ai-api.yaml - AI摘要API（规划中）
**Endpoints**:
- `POST /api/admin/ai/summary/generate` - 生成单条新闻摘要
- `POST /api/admin/ai/summary/batch-generate` - 批量生成摘要
- `GET /api/admin/ai/summary/{newsId}` - 获取摘要
- `DELETE /api/admin/ai/summary/{newsId}` - 删除摘要

**核心功能**: AI摘要生成、批量处理

**请求示例**:
```json
POST /api/admin/ai/summary/generate
{
  "newsId": 123,
  "regenerate": false  // 如果已有摘要是否重新生成
}
```

---

### 7. audit-api.yaml - 审计日志API（规划中）
**Endpoints**:
- `GET /api/admin/audit-logs` - 获取审计日志列表
- `GET /api/admin/audit-logs/{id}` - 获取日志详情
- `GET /api/admin/audit-logs/export` - 导出审计日志

**核心功能**: 操作审计、日志查询、数据导出

---

## API设计原则

### 1. RESTful规范
- 使用标准HTTP方法：GET（查询）、POST（创建）、PUT（更新）、DELETE（删除）
- URL设计：资源名词复数形式，如`/api/news`而非`/api/getNews`
- 状态码：200（成功）、201（创建成功）、400（请求错误）、401（未授权）、404（不存在）、500（服务器错误）

### 2. 统一响应格式
所有API响应使用统一的JSON结构：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  },
  "timestamp": "2025-10-10T10:00:00Z"
}
```

错误响应：
```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": "INVALID_PARAMETER",
  "details": ["title不能为空"],
  "timestamp": "2025-10-10T10:00:00Z"
}
```

### 3. 分页参数标准化
- `page`: 页码，从1开始
- `pageSize`: 每页数量，默认20，最大100
- 响应包含：`total`（总数）、`page`、`pageSize`、`totalPages`

### 4. 认证授权
- 管理员接口（`/api/admin/*`）需要JWT Token
- Token通过HTTP Header传递：`Authorization: Bearer <token>`
- Token过期时间：30分钟
- 刷新机制：前端在Token即将过期时调用`/api/auth/refresh`

### 5. 时间格式
- 统一使用ISO 8601格式：`2025-10-10T10:00:00Z`
- 后端使用UTC时间，前端根据用户时区转换

### 6. 错误处理
- 使用语义化的错误码：`UNAUTHORIZED`, `NOT_FOUND`, `DUPLICATE_TITLE`等
- 错误信息使用中文，便于前端直接展示
- 敏感错误（如数据库异常）不暴露详细信息给客户端

---

## 开发工具

### 1. 查看和测试API
推荐使用以下工具：
- **Swagger UI**: 访问 `http://localhost:8080/swagger-ui.html`
- **Postman**: 导入OpenAPI规范文件
- **Insomnia**: 支持OpenAPI 3.0导入
- **curl**: 命令行快速测试

### 2. 生成客户端代码
使用OpenAPI Generator生成前端API客户端：
```bash
npm install @openapitools/openapi-generator-cli -g

# 生成TypeScript Axios客户端
openapi-generator-cli generate \
  -i contracts/news-api.yaml \
  -g typescript-axios \
  -o frontend/src/api/generated
```

### 3. Mock Server
使用Prism创建Mock API服务器：
```bash
npm install -g @stoplight/prism-cli

# 启动Mock服务
prism mock contracts/news-api.yaml -p 4010
```

---

## 契约版本管理

### 版本策略
- 使用语义化版本：`major.minor.patch`
- MAJOR：不兼容的API变更
- MINOR：向后兼容的功能新增
- PATCH：向后兼容的问题修复

### 变更记录
| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-10-10 | 初始版本，包含认证和新闻管理核心API |

---

## 后续任务

- [ ] 完成category-api.yaml
- [ ] 完成crawler-api.yaml
- [ ] 完成classification-api.yaml
- [ ] 完成ai-api.yaml
- [ ] 完成audit-api.yaml
- [ ] 集成Swagger UI到Spring Boot
- [ ] 使用OpenAPI Generator生成前端TypeScript客户端
- [ ] 编写API集成测试

