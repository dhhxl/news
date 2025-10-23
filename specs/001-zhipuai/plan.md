# Implementation Plan: 新闻管理系统

**Branch**: `001-zhipuai` | **Date**: 2025-10-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-zhipuai/spec.md`

**Note**: This plan uses Spring Boot + Vue + MySQL technology stack.

## Summary

新闻管理系统是一个全栈Web应用，提供新闻内容的自动采集、智能管理和用户浏览功能。系统支持从新浪网、央视网、网易新闻三个来源自动采集新闻，管理员可进行增删改查操作，集成智谱AI提供新闻摘要生成，前端采用类似网易新闻的现代化界面设计。

**技术方案**：
- 后端：Spring Boot 3.x 提供 RESTful API 服务
- 前端：Vue 3 + Element Plus 构建响应式管理和用户界面
- 数据库：MySQL 8.0 存储新闻数据和系统配置
- 爬虫：Spring Boot + JSoup 实现多源新闻采集
- AI集成：RestTemplate 调用智谱AI API
- 任务调度：Spring Scheduler 实现定时采集
- 缓存：Redis 优化新闻列表查询性能

## Technical Context

**Language/Version**: 
- 后端：Java 17 (LTS)
- 前端：TypeScript 5.x + Vue 3.4.x

**Primary Dependencies**:
- 后端核心：Spring Boot 3.2.x, Spring Data JPA, Spring Security
- Web爬虫：JSoup 1.17.x, Apache HttpClient 5.x
- 数据库：MySQL 8.0.x, MyBatis-Plus 3.5.x (作为JPA的补充，优化复杂查询)
- 缓存：Spring Data Redis, Jedis 5.x
- 任务调度：Spring Scheduler, Quartz (复杂定时任务)
- AI集成：Spring RestTemplate, OkHttp3
- 前端核心：Vue 3.4.x, Vue Router 4.x, Pinia (状态管理)
- UI组件库：Element Plus 2.5.x
- HTTP客户端：Axios 1.6.x
- 构建工具：Vite 5.x

**Storage**: 
- MySQL 8.0 (主数据库)
- Redis 7.x (缓存层)
- 文件系统（日志和临时文件）

**Testing**:
- 后端：JUnit 5, Mockito, Spring Boot Test, Testcontainers (MySQL集成测试)
- 前端：Vitest, Vue Test Utils, Playwright (E2E测试)
- API测试：REST Assured

**Target Platform**: 
- 服务器：Linux (Ubuntu 22.04 LTS 或 CentOS 8)
- 浏览器：Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- 移动端：iOS 14+, Android 10+ (响应式Web)

**Project Type**: Web应用（前后端分离架构）

**Performance Goals**:
- API响应时间：P95 < 200ms, P99 < 500ms
- 页面加载：LCP < 2.5s, FCP < 1.5s
- 并发用户：支持 100+ 并发用户
- 新闻采集：10分钟内完成三源采集，每源≥10条新闻
- AI摘要生成：90%请求在30秒内完成

**Constraints**:
- 数据库连接池：最大50个连接
- 单个新闻内容：最大1MB
- 采集并发：同时最多3个采集任务
- Redis缓存：热点新闻列表缓存5分钟
- 文件上传（如有）：最大10MB

**Scale/Scope**:
- 预期用户：100-1000并发用户
- 新闻数据：预计每天新增500-1000条新闻
- 数据保留：永久保留（除非管理员删除）
- 分类数量：系统默认6个 + 管理员自定义最多50个

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

This feature must comply with the project constitution (`.specify/memory/constitution.md`):

### I. 代码质量检查
- [x] 确认编码规范和风格指南
  - 后端：Google Java Style Guide + Spring Boot 最佳实践
  - 前端：Vue 3 风格指南 + ESLint (Standard规则)
  - 工具：Checkstyle (Java), ESLint + Prettier (Vue/TypeScript)
- [x] 确认代码审查流程已设置
  - 使用 GitHub Pull Request 工作流
  - 要求至少1人审查批准
- [x] 确认静态代码分析工具已配置
  - 后端：SonarQube + SpotBugs
  - 前端：ESLint + TypeScript编译检查
- [x] 函数复杂度限制：≤50行，圈复杂度≤10
  - SonarQube规则配置强制执行

### II. 测试标准检查
- [x] 确认测试优先（Test-First）开发流程
  - 按照用户故事编写测试用例
  - 集成测试覆盖所有API端点
- [x] 单元测试覆盖率目标：≥80%
  - 后端：JaCoCo Maven插件监控
  - 前端：Vitest覆盖率报告
- [x] 关键业务逻辑覆盖率目标：100%
  - 新闻采集、分类规则、智能排序算法
  - 用户认证、权限检查
- [x] 确认集成测试和端到端测试计划
  - 集成测试：使用Testcontainers启动MySQL容器
  - E2E测试：Playwright覆盖5个用户故事

### III. 用户体验一致性检查
- [x] 确认使用统一的设计系统和组件库
  - 使用 Element Plus 作为基础组件库
  - 自定义主题配色参考网易新闻
  - 组件封装遵循原子设计原则
- [x] 响应式设计计划：移动端/平板端/桌面端
  - 使用 Element Plus 栅格系统 (xs/sm/md/lg/xl断点)
  - 移动端优先设计
  - 测试覆盖：320px, 768px, 1024px, 1920px
- [x] 可访问性（WCAG 2.1 AA）合规计划
  - 语义化HTML标签
  - ARIA标签覆盖所有交互元素
  - 键盘导航支持
  - 使用 axe-core 进行可访问性审计
- [x] 国际化（i18n）支持计划
  - 使用 Vue I18n 库
  - 初期仅支持中文，架构预留多语言扩展

### IV. 性能要求检查
- [x] 页面加载性能目标：FCP<1.5s, LCP<2.5s, FID<100ms, CLS<0.1
  - Vite代码分割和懒加载
  - 图片懒加载和WebP格式
  - 使用 Lighthouse CI 监控
- [x] API 响应时间目标：P95<200ms, P99<500ms
  - MySQL查询优化（索引、查询计划分析）
  - Redis缓存热点数据
  - 使用 Spring Boot Actuator + Micrometer 监控
- [x] 资源优化计划：图片压缩、代码分割、懒加载
  - Vite build优化：Tree Shaking, Minification
  - 路由级代码分割
  - 图片CDN（如需要）
- [x] 性能监控工具集成计划
  - 后端：Spring Boot Actuator + Prometheus + Grafana
  - 前端：Lighthouse CI + Web Vitals
  - APM：可选集成 SkyWalking

**Constitution Compliance**: ✅ 所有检查项已通过，无需豁免申请

## Project Structure

### Documentation (this feature)

```
specs/001-zhipuai/
├── plan.md              # This file (implementation plan)
├── spec.md              # Feature specification
├── research.md          # Phase 0: Technology research
├── data-model.md        # Phase 1: Database schema
├── quickstart.md        # Phase 1: Development guide
├── contracts/           # Phase 1: API contracts
│   ├── auth-api.yaml       # 认证授权 API
│   ├── news-api.yaml       # 新闻管理 API
│   ├── category-api.yaml   # 分类管理 API
│   ├── crawler-api.yaml    # 采集任务 API
│   └── ai-api.yaml         # AI摘要 API
└── checklists/          # Quality checklists
    └── requirements.md     # Requirements checklist
```

### Source Code (repository root)

```
news-website/
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/news/
│   │   │   │   ├── NewsApplication.java
│   │   │   │   ├── config/           # 配置类
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── RedisConfig.java
│   │   │   │   │   └── SchedulerConfig.java
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── NewsController.java
│   │   │   │   │   ├── CategoryController.java
│   │   │   │   │   ├── CrawlerController.java
│   │   │   │   │   └── AiSummaryController.java
│   │   │   │   ├── service/          # 业务逻辑层
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── NewsService.java
│   │   │   │   │   ├── CategoryService.java
│   │   │   │   │   ├── CrawlerService.java
│   │   │   │   │   ├── ClassificationService.java
│   │   │   │   │   ├── AiSummaryService.java
│   │   │   │   │   └── SchedulerService.java
│   │   │   │   ├── repository/       # 数据访问层
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── NewsRepository.java
│   │   │   │   │   ├── CategoryRepository.java
│   │   │   │   │   ├── ClassificationRuleRepository.java
│   │   │   │   │   ├── SummaryRepository.java
│   │   │   │   │   ├── CrawlTaskRepository.java
│   │   │   │   │   └── AuditLogRepository.java
│   │   │   │   ├── model/            # 实体类
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   ├── News.java
│   │   │   │   │   │   ├── Category.java
│   │   │   │   │   │   ├── ClassificationRule.java
│   │   │   │   │   │   ├── Summary.java
│   │   │   │   │   │   ├── CrawlTask.java
│   │   │   │   │   │   └── AuditLog.java
│   │   │   │   │   └── dto/          # 数据传输对象
│   │   │   │   │       ├── LoginRequest.java
│   │   │   │   │       ├── NewsDTO.java
│   │   │   │   │       └── ...
│   │   │   │   ├── crawler/          # 爬虫模块
│   │   │   │   │   ├── NewsCrawler.java
│   │   │   │   │   ├── SinaCrawler.java
│   │   │   │   │   ├── CctvCrawler.java
│   │   │   │   │   └── NeteaseCrawler.java
│   │   │   │   ├── security/         # 安全模块
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   └── CustomUserDetailsService.java
│   │   │   │   ├── exception/        # 异常处理
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   └── BusinessException.java
│   │   │   │   └── util/             # 工具类
│   │   │   │       ├── DateUtil.java
│   │   │   │       └── StringUtil.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/
│   │   │           └── migration/    # Flyway数据库迁移
│   │   │               ├── V1__init_schema.sql
│   │   │               ├── V2__add_categories.sql
│   │   │               └── V3__add_classification_rules.sql
│   │   └── test/
│   │       ├── java/com/news/
│   │       │   ├── controller/       # Controller集成测试
│   │       │   ├── service/          # Service单元测试
│   │       │   ├── repository/       # Repository集成测试
│   │       │   └── crawler/          # 爬虫测试
│   │       └── resources/
│   │           └── application-test.yml
│   ├── pom.xml                    # Maven配置
│   └── Dockerfile

├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── main.ts
│   │   ├── App.vue
│   │   ├── views/              # 页面组件
│   │   │   ├── Login.vue
│   │   │   ├── admin/          # 管理后台页面
│   │   │   │   ├── Dashboard.vue
│   │   │   │   ├── NewsList.vue
│   │   │   │   ├── NewsEdit.vue
│   │   │   │   ├── CategoryManage.vue
│   │   │   │   ├── CrawlerManage.vue
│   │   │   │   ├── ClassificationRules.vue
│   │   │   │   └── AuditLog.vue
│   │   │   └── user/           # 用户端页面
│   │   │       ├── Home.vue
│   │   │       ├── NewsDetail.vue
│   │   │       └── CategoryNews.vue
│   │   ├── components/         # 复用组件
│   │   │   ├── layout/
│   │   │   │   ├── AdminLayout.vue
│   │   │   │   ├── UserLayout.vue
│   │   │   │   ├── Header.vue
│   │   │   │   ├── Sidebar.vue
│   │   │   │   └── Footer.vue
│   │   │   ├── news/
│   │   │   │   ├── NewsCard.vue
│   │   │   │   ├── NewsListItem.vue
│   │   │   │   └── NewsSummary.vue
│   │   │   └── common/
│   │   │       ├── Loading.vue
│   │   │       ├── Pagination.vue
│   │   │       └── ErrorMessage.vue
│   │   ├── router/             # 路由配置
│   │   │   └── index.ts
│   │   ├── stores/             # Pinia状态管理
│   │   │   ├── user.ts
│   │   │   ├── news.ts
│   │   │   └── category.ts
│   │   ├── api/                # API接口封装
│   │   │   ├── auth.ts
│   │   │   ├── news.ts
│   │   │   ├── category.ts
│   │   │   ├── crawler.ts
│   │   │   └── ai.ts
│   │   ├── utils/              # 工具函数
│   │   │   ├── request.ts      # Axios封装
│   │   │   ├── date.ts
│   │   │   └── storage.ts
│   │   ├── types/              # TypeScript类型定义
│   │   │   ├── user.ts
│   │   │   ├── news.ts
│   │   │   └── api.ts
│   │   └── assets/             # 静态资源
│   │       ├── styles/
│   │       │   ├── main.css
│   │       │   └── variables.css
│   │       └── images/
│   ├── tests/
│   │   ├── unit/               # 单元测试
│   │   ├── integration/        # 组件集成测试
│   │   └── e2e/                # Playwright E2E测试
│   ├── public/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── .eslintrc.js
│   └── Dockerfile

├── docker-compose.yml          # 本地开发环境
├── .gitignore
└── README.md
```

**Structure Decision**: 采用 Option 2: Web应用架构（前后端分离）

选择理由：
1. **前后端分离**：后端提供RESTful API，前端独立部署，便于扩展和维护
2. **Spring Boot后端**：使用标准的三层架构（Controller-Service-Repository），符合企业级开发规范
3. **Vue 3前端**：组件化开发，清晰的路由和状态管理，代码组织符合Vue 3最佳实践
4. **Docker化部署**：两个独立的Dockerfile，支持容器化部署和水平扩展

## Complexity Tracking

*当前无宪章违规项需要记录*

所有架构决策均符合宪章要求，无需复杂度豁免。
