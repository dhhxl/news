# 📰 新闻管理系统

> 基于 Spring Boot + Vue 3 + MySQL 的完整新闻管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.3-blue)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-latest-409EFF)](https://element-plus.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)](https://www.mysql.com/)

---

## 📋 项目简介

这是一个功能完整的新闻管理系统，支持新闻发布、编辑、审核、评论、点赞等功能。系统采用前后端分离架构，具有三种用户角色（管理员、编辑员、普通用户），支持权限管理、内容审核、数据统计等功能。

---

## ✨ 核心功能

### 👤 用户功能
- ✅ 用户注册/登录（支持角色选择）
- ✅ 个人资料管理（头像上传、信息修改）
- ✅ 密码修改
- ✅ 新闻浏览（分类、搜索、排序）
- ✅ 评论和点赞
- ✅ AI智能摘要

### ✍️ 编辑员功能
- ✅ 新闻创建和编辑
- ✅ 图片上传管理
- ✅ 新闻提交审核
- ✅ 查看审核状态
- ✅ 修改被退回的稿件
- ✅ 数据统计

### 🔐 管理员功能
- ✅ 用户管理（查看、禁用、删除、重置密码）
- ✅ 新闻审核（通过/退回）
- ✅ 新闻管理（查看、编辑、删除）
- ✅ 分类管理
- ✅ 新闻采集（爬虫）
- ✅ 批量生成摘要
- ✅ 审计日志
- ✅ 系统统计

### 🤖 AI功能
- ✅ 智能新闻摘要（ZhipuAI）
- ✅ 批量摘要生成
- ✅ 自动分类建议

---

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + Redis
- **ORM**: Spring Data JPA
- **迁移**: Flyway
- **日志**: SLF4J + Logback
- **文档**: Swagger/OpenAPI

### 前端
- **框架**: Vue 3 + TypeScript
- **UI库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **构建工具**: Vite
- **HTTP客户端**: Axios

### 数据库
- **MySQL 8.0**: 主数据库
- **Redis**: 缓存和会话

### DevOps
- **容器**: Docker + Docker Compose
- **构建**: Maven + NPM
- **版本控制**: Git

---

## 🚀 快速开始

### 前置要求
- Java 17+
- Node.js 18+
- MySQL 8.0
- Docker Desktop
- Maven 3.6+

### 1. 克隆项目
```bash
git clone [你的仓库地址]
cd news-main
```

### 2. 启动数据库
```bash
docker-compose up -d
```

### 3. 配置后端
```bash
cd backend
# 配置 application.yml 中的数据库连接
# 配置 ZhipuAI API Key（可选）
```

### 4. 一键启动
```bash
# 返回项目根目录
cd ..

# 使用启动脚本
启动系统.bat
```

或者分别启动：
```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端（新终端）
cd frontend
npm install
npm run dev
```

### 5. 访问系统
- **前端**: http://localhost:5173
- **后端API**: http://localhost:8080

### 6. 默认账号
```
管理员:
  用户名: admin
  密码: admin123

编辑员:
  用户名: editor
  密码: editor123
```

---

## 📁 项目结构

```
news-main/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java源代码
│   │   │   │   └── com/news/
│   │   │   │       ├── config/        # 配置类
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── model/         # 模型/实体
│   │   │   │       ├── repository/    # 数据访问层
│   │   │   │       ├── security/      # 安全配置
│   │   │   │       └── exception/     # 异常处理
│   │   │   └── resources/
│   │   │       ├── application.yml    # 应用配置
│   │   │       └── db/migration/      # Flyway迁移脚本
│   │   └── test/              # 测试代码
│   ├── uploads/               # 用户上传文件
│   └── pom.xml               # Maven配置
│
├── frontend/                  # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/              # API接口
│   │   ├── components/       # 组件
│   │   │   └── layout/      # 布局组件
│   │   ├── views/           # 页面视图
│   │   │   ├── admin/       # 管理员页面
│   │   │   ├── editor/      # 编辑员页面
│   │   │   └── user/        # 用户页面
│   │   ├── stores/          # Pinia状态管理
│   │   ├── router/          # 路由配置
│   │   ├── utils/           # 工具函数
│   │   └── main.ts          # 入口文件
│   ├── public/              # 静态资源
│   └── package.json         # NPM配置
│
├── specs/                    # 项目规范文档
├── docker-compose.yml        # Docker配置
│
├── 文档/                     # 项目文档
│   ├── QUICK_START.md       # 快速开始
│   ├── 最终测试指南.md       # 测试指南
│   ├── 功能说明/            # 各功能详细说明
│   └── 完成报告/            # 各模块完成报告
│
└── 脚本/                     # 常用脚本
    ├── 启动系统.bat         # 一键启动
    ├── 启动前端.bat
    ├── 启动后端.bat
    └── 停止所有服务.bat
```

---

## 🎯 主要功能模块

### 1. 用户认证与授权
- JWT Token认证
- 角色权限控制（RBAC）
- 密码加密存储

### 2. 新闻管理
- 新闻CRUD操作
- 富文本编辑
- 图片上传与管理
- 分类管理

### 3. 编辑工作流
- 新闻创建与编辑
- 提交审核
- 审核通过/退回
- 版本管理

### 4. 内容审核
- 新闻审核队列
- 审核员分配
- 审核意见反馈
- 审核历史记录

### 5. 用户管理
- 用户列表查看
- 用户信息管理
- 禁用/启用用户
- 重置密码

### 6. 爬虫系统
- 定时采集新闻
- 多源聚合
- 自动分类
- 去重处理

### 7. AI智能功能
- 新闻摘要生成
- 批量处理
- 智能分类

### 8. 数据统计
- 用户统计
- 新闻统计
- 访问统计
- 审核统计

### 9. 审计日志
- 操作记录
- 审计查询
- 日志导出

---

## 📱 功能截图

（这里可以添加系统截图）

---

## 🧪 测试

### 后端测试
```bash
cd backend
mvn test
```

### 前端测试
```bash
cd frontend
npm run test
```

### 完整测试
请参考 `最终测试指南.md`

---

## 📚 文档

### 使用文档
- [快速开始](QUICK_START.md)
- [最终测试指南](最终测试指南.md)

### 功能说明
- [ZhipuAI配置说明](ZhipuAI配置说明.md)
- [爬虫功能使用指南](爬虫功能使用指南.md)
- [批量生成摘要使用说明](批量生成摘要使用说明.md)
- [个人资料模块使用说明](个人资料模块使用说明.md)
- [用户管理功能说明](用户管理功能说明.md)
- [评论和点赞功能说明](评论和点赞功能说明.md)

### 完成报告
- [功能增强完成报告](功能增强完成报告.md)
- [个人资料模块完成报告](个人资料模块完成报告.md)
- [用户管理功能完成报告](用户管理功能完成报告.md)

---

## 🔧 配置说明

### 后端配置
编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/news_db
    username: root
    password: your_password

zhipuai:
  api-key: your_api_key  # ZhipuAI API密钥
```

### 前端配置
编辑 `frontend/.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 🐛 常见问题

### Q1: 后端启动失败？
**A**: 检查：
- Java版本是否为17+
- MySQL是否启动
- 数据库连接配置是否正确

### Q2: 前端访问404？
**A**: 检查：
- 后端是否启动成功
- 前端API地址配置是否正确
- 浏览器缓存（强制刷新）

### Q3: 图片上传失败？
**A**: 检查：
- uploads目录权限
- 文件大小限制
- 文件类型限制

详细问题解决请查看各功能说明文档。

---

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

## 📄 许可证

本项目仅用于学习和毕业设计，未经许可不得用于商业用途。

---

## 👥 联系方式

如有问题，请联系：
- Email: [your-email]
- GitHub: [your-github]

---

## 🙏 致谢

感谢以下开源项目：
- Spring Boot
- Vue 3
- Element Plus
- ZhipuAI

---

**⭐ 如果这个项目对你有帮助，请给一个Star！**

