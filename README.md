# 新闻管理系统

基于 Spring Boot + Vue 3 + MySQL 的新闻管理系统，支持从新浪、央视、网易自动采集新闻，集成AI智能摘要。

## 🌟 主要功能

- ✅ **用户认证**: JWT Token + Spring Security
- ✅ **新闻管理**: CRUD操作，智能排序，分页搜索
- ✅ **分类管理**: 默认分类 + 自定义分类
- ✅ **智能排序**: 时间衰减算法 + 浏览量综合排序
- ✅ **新闻爬虫**: 自动从新浪/央视/网易采集新闻
- ✅ **自动分类**: 来源规则 + 关键词匹配
- ✅ **去重检测**: 基于标题完全匹配
- ⏳ **AI摘要**: 集成ZhipuAI生成新闻摘要（待实施）
- ⏳ **前端界面**: Vue 3响应式界面（待实施）

---

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (前端)

### 1. 启动数据库

```bash
docker-compose up -d
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

应用将在 `http://localhost:8080/api` 启动

### 3. 测试API

```powershell
# Windows PowerShell
.\test_news_api.ps1      # 测试基础API
.\test_crawler_api.ps1   # 测试爬虫API
```

### 4. 启动前端（待实施）

```bash
cd frontend
npm install
npm run dev
```

---

## 🔑 默认账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |

⚠️ **生产环境请立即修改默认密码！**

---

## 📚 API文档

### 登录获取Token

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# 响应
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

### 获取新闻列表（智能排序）

```bash
GET /api/news?page=0&size=20
# 或按分类
GET /api/news?categoryId=5&page=0&size=20
```

### 搜索新闻

```bash
GET /api/news/search?keyword=科技&page=0&size=10
```

### 触发爬虫

```bash
POST /api/crawler/trigger/SINA?maxCount=10
Authorization: Bearer {token}
```

### 获取所有分类

```bash
GET /api/categories
```

完整API文档请参考: `specs/001-zhipuai/contracts/`

---

## 🗂️ 数据库

### 连接信息

```
Host: localhost
Port: 3306
Database: news_management_db
Username: newsadmin
Password: newspass123
```

### 表结构

- **users** - 用户表
- **categories** - 分类表（6个默认分类）
- **news** - 新闻表
- **summaries** - AI摘要表
- **classification_rules** - 分类规则表
- **crawl_tasks** - 爬取任务表
- **audit_logs** - 审计日志表

---

## ⚙️ 配置

### 后端配置文件

`backend/src/main/resources/application.yml`

关键配置项：
```yaml
# JWT配置
jwt:
  secret: your-256-bit-secret-key-change-in-production
  expiration: 1800000  # 30分钟

# 爬虫配置
crawler:
  schedule:
    enabled: true
    cron: "0 0 * * * ?"  # 每小时执行
  max-count-per-source: 10

# ZhipuAI配置
zhipuai:
  api-key: your-api-key
  model: glm-4
```

---

## 🧪 测试

### 运行单元测试

```bash
cd backend
mvn test
```

### API集成测试

使用提供的PowerShell脚本：
- `test_news_api.ps1` - 测试新闻管理API
- `test_crawler_api.ps1` - 测试爬虫API

---

## 📊 技术特点

### 智能排序算法

```
Score = ViewCount / (1 + (当前时间 - 发布时间) / 24小时)
```

### 自动分类策略

1. **来源预设规则** (优先级最高)
   - SINA → 社会
   - CCTV → 政治
   - NETEASE → 科技

2. **关键词匹配** (次优先级)
   - 根据标题和内容匹配关键词

3. **默认分类** (兜底)
   - 未匹配规则 → 社会

### 去重策略

- 标题完全匹配
- 原始URL匹配

---

## 📝 开发进度

- [x] Phase 1: 项目基础设置
- [x] Phase 2: 核心基础设施
- [x] Phase 3: 用户认证
- [x] Phase 4: 新闻内容管理
- [x] Phase 5: 新闻爬虫
- [ ] Phase 6: AI智能摘要
- [ ] Phase 7: 前端界面

---

## 🤝 贡献指南

请参考项目宪章: `.specify/memory/constitution.md`

核心原则：
- 代码质量：函数≤50行，圈复杂度≤10
- 测试覆盖：≥80%
- 代码审查：强制执行
- 性能要求：API响应≤500ms

---

## 📄 许可证

MIT License

---

## 📧 联系方式

项目文档: `specs/001-zhipuai/`

---

**当前版本**: v0.7.0 (Phase 5 完成)  
**最后更新**: 2025-10-10

